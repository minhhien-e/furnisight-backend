import numpy as np
import torch
import yaml
from easydict import EasyDict
import argparse
import os
import cv2
import copy
from collections import OrderedDict

from pipelines.noncuboid.datasets import NYU303, CustomDataset, Structured3D
from pipelines.noncuboid.models import (ConvertLayout, Detector, DisplayLayout, display2Dseg, Loss,
                    Reconstruction, _validate_colormap, post_process)
from scipy.optimize import linear_sum_assignment

EXPORT_MIRROR_X = True


def tensor_image_to_bgr_u8(img_tensor):
    """
    Convert normalized CHW tensor image to uint8 BGR image.
    """
    if torch.is_tensor(img_tensor):
        img_np = img_tensor.detach().cpu().numpy().transpose(1, 2, 0)
    else:
        img_np = np.asarray(img_tensor).transpose(1, 2, 0)
    mean = np.array([0.485, 0.456, 0.406], dtype=np.float32)
    std = np.array([0.229, 0.224, 0.225], dtype=np.float32)
    rgb = np.clip((img_np * std + mean) * 255.0, 0, 255).astype(np.uint8)
    bgr = rgb[:, :, ::-1]
    return bgr


def _mirror_vertices_faces_x(vertices_np, faces_np):
    """
    Mirror model along X axis and keep face orientation consistent.
    """
    if not EXPORT_MIRROR_X:
        return vertices_np, faces_np
    v = np.asarray(vertices_np, dtype=np.float32).copy()
    f = np.asarray(faces_np, dtype=np.int32).copy()
    v[:, 0] *= -1.0
    if f.size > 0:
        f = f[:, [0, 2, 1]]
    return v, f


def export_layout_glb(polys, img_bgr, save_path):
    """
    Export reconstructed layout polygons as a colored GLB mesh.
    Colors are sampled from polygon regions on input image.
    """
    try:
        import trimesh
    except ImportError as exc:
        raise ImportError('Missing trimesh for colored GLB export. Install with: pip install trimesh') from exc

    if polys is None or len(polys) != 2:
        return None, 'invalid polygons'
    # Normalize source image to uint8 BGR in [0, 255] for stable color sampling.
    img_bgr = np.asarray(img_bgr)
    if img_bgr.dtype != np.uint8:
        if img_bgr.max() <= 1.0 and img_bgr.min() >= 0.0:
            img_bgr = np.clip(img_bgr * 255.0, 0, 255).astype(np.uint8)
        else:
            img_bgr = np.clip(img_bgr, 0, 255).astype(np.uint8)
    poly_2d, poly_3d = polys
    if poly_2d is None or poly_3d is None:
        return None, 'empty polygon sets'

    vertices = []
    faces = []
    colors = []
    h, w = img_bgr.shape[:2]

    for pts2d, pts3d in zip(poly_2d, poly_3d):
        v2 = np.array(pts2d, dtype=np.float32)
        v3 = np.array(pts3d, dtype=np.float32)
        if len(v2) < 3 or len(v3) < 3:
            continue

        # Remove duplicated closing point if present.
        if np.allclose(v2[0], v2[-1]) and np.allclose(v3[0], v3[-1]):
            v2 = v2[:-1]
            v3 = v3[:-1]
        if len(v2) < 3 or len(v3) < 3:
            continue

        # Use average color inside the 2D polygon for robust coloring.
        poly_i32 = np.round(v2).astype(np.int32)
        poly_i32[:, 0] = np.clip(poly_i32[:, 0], 0, w - 1)
        poly_i32[:, 1] = np.clip(poly_i32[:, 1], 0, h - 1)
        mask = np.zeros((h, w), dtype=np.uint8)
        cv2.fillPoly(mask, [poly_i32], color=1)
        if np.any(mask):
            mean_bgr = img_bgr[mask == 1].mean(axis=0)
        else:
            px = poly_i32[0]
            mean_bgr = img_bgr[px[1], px[0]]

        # Small brightness lift helps avoid overly dark output in 3D viewers.
        mean_rgb = np.clip(mean_bgr[::-1] * 1.2, 0, 255).astype(np.uint8)
        face_rgb = [int(mean_rgb[0]), int(mean_rgb[1]), int(mean_rgb[2])]

        base = len(vertices)
        for _, p3 in zip(v2, v3):
            vertices.append(p3.tolist())
            colors.append(face_rgb)

        for i in range(1, len(v3) - 1):
            faces.append([base, base + i, base + i + 1])

    if len(vertices) < 3 or len(faces) == 0:
        return None, 'not enough geometry'

    vertices_np = np.asarray(vertices, dtype=np.float32)
    faces_np = np.asarray(faces, dtype=np.int32)
    faces_np = faces_np[:, [0, 2, 1]]
    vertices_np, faces_np = _mirror_vertices_faces_x(vertices_np, faces_np)
    colors_np = np.asarray(colors, dtype=np.uint8)
    os.makedirs(os.path.dirname(save_path), exist_ok=True)

    mesh = trimesh.Trimesh(
        vertices=vertices_np,
        faces=faces_np,
        process=False,
    )
    rgba = np.concatenate([colors_np, np.full((colors_np.shape[0], 1), 255, dtype=np.uint8)], axis=1)
    mesh.visual = trimesh.visual.ColorVisuals(mesh=mesh, vertex_colors=rgba)
    # Ensure glTF material does not darken vertex colors.
    mesh.visual.material = trimesh.visual.material.PBRMaterial(
        baseColorFactor=[255, 255, 255, 255],
        metallicFactor=0.0,
        roughnessFactor=1.0,
    )
    cmin = colors_np.min(axis=0).tolist()
    cmax = colors_np.max(axis=0).tolist()
    print(f'GLB vertex color range RGB(min/max in [0..255]): {cmin} / {cmax}')
    mesh.export(save_path)
    return save_path, 'glb'


def _enhance_rgb_for_display(rgb_u8, gamma=0.9, gain=1.12, saturation=1.08):
    """
    Improve perceived color vividness in common 3D viewers.
    Input/Output: uint8 RGB array [..., 3].
    """
    rgb = np.asarray(rgb_u8, dtype=np.float32) / 255.0
    rgb = np.power(np.clip(rgb, 0.0, 1.0), gamma)
    gray = np.mean(rgb, axis=-1, keepdims=True)
    rgb = gray + (rgb - gray) * saturation
    rgb = np.clip(rgb * gain, 0.0, 1.0)
    return (rgb * 255.0 + 0.5).astype(np.uint8)


def _subdivide_triangle_2d3d(v2, v3, levels=1):
    """
    Recursively subdivide one 2D/3D triangle pair.
    v2: (3,2), v3: (3,3) -> returns list[(tri2, tri3)].
    """
    tris = [(v2.astype(np.float32), v3.astype(np.float32))]
    for _ in range(max(int(levels), 0)):
        out = []
        for t2, t3 in tris:
            m2_01 = 0.5 * (t2[0] + t2[1]); m3_01 = 0.5 * (t3[0] + t3[1])
            m2_12 = 0.5 * (t2[1] + t2[2]); m3_12 = 0.5 * (t3[1] + t3[2])
            m2_20 = 0.5 * (t2[2] + t2[0]); m3_20 = 0.5 * (t3[2] + t3[0])
            out.extend([
                (np.array([t2[0], m2_01, m2_20], dtype=np.float32),
                 np.array([t3[0], m3_01, m3_20], dtype=np.float32)),
                (np.array([m2_01, t2[1], m2_12], dtype=np.float32),
                 np.array([m3_01, t3[1], m3_12], dtype=np.float32)),
                (np.array([m2_20, m2_12, t2[2]], dtype=np.float32),
                 np.array([m3_20, m3_12, t3[2]], dtype=np.float32)),
                (np.array([m2_01, m2_12, m2_20], dtype=np.float32),
                 np.array([m3_01, m3_12, m3_20], dtype=np.float32)),
            ])
        tris = out
    return tris


def _rotation_matrix_from_vectors(a, b):
    """
    Compute rotation matrix R such that R @ a ~= b.
    """
    a = np.asarray(a, dtype=np.float64)
    b = np.asarray(b, dtype=np.float64)
    a = a / (np.linalg.norm(a) + 1e-12)
    b = b / (np.linalg.norm(b) + 1e-12)
    v = np.cross(a, b)
    c = float(np.dot(a, b))
    s = float(np.linalg.norm(v))
    if s < 1e-12:
        if c > 0:
            return np.eye(3, dtype=np.float64)
        # 180-degree rotation around an arbitrary axis orthogonal to a
        axis = np.array([1.0, 0.0, 0.0], dtype=np.float64)
        if abs(a[0]) > 0.9:
            axis = np.array([0.0, 1.0, 0.0], dtype=np.float64)
        axis = axis - np.dot(axis, a) * a
        axis = axis / (np.linalg.norm(axis) + 1e-12)
        K = np.array([
            [0.0, -axis[2], axis[1]],
            [axis[2], 0.0, -axis[0]],
            [-axis[1], axis[0], 0.0]
        ], dtype=np.float64)
        return np.eye(3, dtype=np.float64) + 2.0 * (K @ K)
    K = np.array([
        [0.0, -v[2], v[1]],
        [v[2], 0.0, -v[0]],
        [-v[1], v[0], 0.0]
    ], dtype=np.float64)
    R = np.eye(3, dtype=np.float64) + K + (K @ K) * ((1.0 - c) / (s * s + 1e-12))
    return R


def export_layout_aligned_glb(polys, img_bgr, seg_map, inv_depth, K, save_path, stride=2):
    """
    Export comparison mesh with EXACT layout geometry (same ratio as layout.glb)
    and per-plane stretched texture patches from source image.
    seg_map is used to get cleaner per-face patches from model result.
    inv_depth/K/stride are accepted for API compatibility.
    """
    try:
        import trimesh
        from PIL import Image
    except ImportError as exc:
        raise ImportError('Missing trimesh/Pillow for aligned GLB export.') from exc

    if polys is None or len(polys) != 2:
        return None, 'invalid polygons'

    img_bgr = np.asarray(img_bgr)
    if img_bgr.dtype != np.uint8:
        if img_bgr.max() <= 1.0 and img_bgr.min() >= 0.0:
            img_bgr = np.clip(img_bgr * 255.0, 0, 255).astype(np.uint8)
        else:
            img_bgr = np.clip(img_bgr, 0, 255).astype(np.uint8)

    poly_2d, poly_3d = polys
    if poly_2d is None or poly_3d is None:
        return None, 'empty polygon sets'
    seg_map = None if seg_map is None else np.asarray(seg_map)

    h, w = img_bgr.shape[:2]
    scene = trimesh.Scene()
    plane_count = 0
    rgb_full = img_bgr[:, :, ::-1]

    # Estimate room center from all layout vertices.
    all_room_vertices = []
    for pts3d in poly_3d:
        vv = np.asarray(pts3d, dtype=np.float32)
        if len(vv) == 0:
            continue
        if len(vv) > 1 and np.allclose(vv[0], vv[-1]):
            vv = vv[:-1]
        if len(vv) > 0:
            all_room_vertices.append(vv)
    if len(all_room_vertices) == 0:
        return None, 'empty 3d polygons'
    room_center = np.mean(np.concatenate(all_room_vertices, axis=0), axis=0)

    for pid, (pts2d, pts3d) in enumerate(zip(poly_2d, poly_3d)):
        v2 = np.asarray(pts2d, dtype=np.float32)
        v3 = np.asarray(pts3d, dtype=np.float32)
        if len(v2) < 3 or len(v3) < 3:
            continue

        # Remove duplicated closing point independently and keep arrays aligned.
        if len(v2) > 1 and np.allclose(v2[0], v2[-1]):
            v2 = v2[:-1]
        if len(v3) > 1 and np.allclose(v3[0], v3[-1]):
            v3 = v3[:-1]
        n = min(len(v2), len(v3))
        v2 = v2[:n]
        v3 = v3[:n]
        if n < 3:
            continue

        # Crop plane patch from source image and stretch it to this polygon.
        x_min = int(np.floor(np.min(v2[:, 0])))
        x_max = int(np.ceil(np.max(v2[:, 0])))
        y_min = int(np.floor(np.min(v2[:, 1])))
        y_max = int(np.ceil(np.max(v2[:, 1])))
        x_min = max(0, min(x_min, w - 1))
        x_max = max(0, min(x_max, w - 1))
        y_min = max(0, min(y_min, h - 1))
        y_max = max(0, min(y_max, h - 1))
        patch_valid = (x_max > x_min) and (y_max > y_min)
        patch = None
        if patch_valid:
            # Prefer segmentation-guided patch extraction (from result faces).
            poly_i32 = np.round(v2).astype(np.int32)
            poly_i32[:, 0] = np.clip(poly_i32[:, 0], 0, w - 1)
            poly_i32[:, 1] = np.clip(poly_i32[:, 1], 0, h - 1)
            poly_mask = np.zeros((h, w), dtype=np.uint8)
            cv2.fillPoly(poly_mask, [poly_i32], color=1)

            guided_mask = None
            if seg_map is not None and seg_map.shape[:2] == (h, w):
                vals = seg_map[poly_mask == 1]
                vals = vals[np.isfinite(vals)] if vals.size else vals
                if vals.size:
                    vals_i = vals.astype(np.int32)
                    uniq, cnt = np.unique(vals_i, return_counts=True)
                    # Ignore invalid background label if present.
                    valid_idx = np.where(uniq != -1)[0]
                    if valid_idx.size > 0:
                        uniq = uniq[valid_idx]
                        cnt = cnt[valid_idx]
                    if uniq.size > 0:
                        label = int(uniq[np.argmax(cnt)])
                        guided_mask = ((seg_map.astype(np.int32) == label) & (poly_mask == 1))

            mask = guided_mask if guided_mask is not None and np.any(guided_mask) else (poly_mask == 1)
            ys_m, xs_m = np.where(mask)
            if ys_m.size > 0:
                gx_min = int(xs_m.min()); gx_max = int(xs_m.max())
                gy_min = int(ys_m.min()); gy_max = int(ys_m.max())
                patch = rgb_full[gy_min:gy_max + 1, gx_min:gx_max + 1].copy()
                patch_valid = patch.size > 0
                if patch_valid:
                    du = max(float(gx_max - gx_min), 1.0)
                    dv = max(float(gy_max - gy_min), 1.0)
                    uv = np.zeros((len(v2), 2), dtype=np.float32)
                    uv[:, 0] = np.clip((v2[:, 0] - float(gx_min)) / du, 0.0, 1.0)
                    uv[:, 1] = np.clip(1.0 - (v2[:, 1] - float(gy_min)) / dv, 0.0, 1.0)
                else:
                    uv = np.zeros((len(v2), 2), dtype=np.float32)
            else:
                patch_valid = False
                uv = np.zeros((len(v2), 2), dtype=np.float32)
        else:
            uv = np.zeros((len(v2), 2), dtype=np.float32)

        if patch_valid:
            patch = _enhance_rgb_for_display(patch, gamma=0.9, gain=1.15, saturation=1.12)
        else:
            # Fallback: keep this plane with a 1x1 texture (do not drop geometry).
            poly_i32 = np.round(v2).astype(np.int32)
            poly_i32[:, 0] = np.clip(poly_i32[:, 0], 0, w - 1)
            poly_i32[:, 1] = np.clip(poly_i32[:, 1], 0, h - 1)
            mask = np.zeros((h, w), dtype=np.uint8)
            cv2.fillPoly(mask, [poly_i32], color=1)
            if np.any(mask):
                rgb_mean = np.mean(rgb_full[mask == 1], axis=0, dtype=np.float32)
            else:
                p = poly_i32[0]
                rgb_mean = rgb_full[p[1], p[0]].astype(np.float32)
            rgb_mean = _enhance_rgb_for_display(rgb_mean[None, :].astype(np.uint8))[0]
            patch = np.full((1, 1, 3), rgb_mean, dtype=np.uint8)

        tex_img = Image.fromarray(patch, mode='RGB')

        if not patch_valid:
            # Collapse UV to center for 1x1 fallback texture.
            uv[:, 0] = 0.5
            uv[:, 1] = 0.5

        faces = []
        for i in range(1, len(v3) - 1):
            faces.append([0, i + 1, i])
        if len(faces) == 0:
            continue

        # Auto-orient triangles so normals point inward (toward room center).
        oriented_faces = []
        for f in faces:
            i0, i1, i2 = f
            p0 = v3[i0]
            p1 = v3[i1]
            p2 = v3[i2]
            n = np.cross(p1 - p0, p2 - p0)
            face_center = (p0 + p1 + p2) / 3.0
            to_center = room_center - face_center
            # If normal points away from room center, flip winding.
            if float(np.dot(n, to_center)) < 0:
                oriented_faces.append([i0, i2, i1])
            else:
                oriented_faces.append([i0, i1, i2])

        vertices_np = np.asarray(v3, dtype=np.float32)
        faces_np = np.asarray(oriented_faces, dtype=np.int32)
        vertices_np, faces_np = _mirror_vertices_faces_x(vertices_np, faces_np)
        material = trimesh.visual.texture.SimpleMaterial(image=tex_img)
        visual = trimesh.visual.texture.TextureVisuals(uv=uv, image=tex_img, material=material)
        mesh = trimesh.Trimesh(vertices=vertices_np, faces=faces_np, process=False, visual=visual)
        scene.add_geometry(mesh, node_name=f'plane_{pid}')
        plane_count += 1

    if plane_count == 0:
        return None, 'not enough geometry'

    os.makedirs(os.path.dirname(save_path), exist_ok=True)
    scene.export(save_path)
    print(f'Exported aligned textured planes: {plane_count} -> {save_path}')
    return save_path, 'aligned_glb'


def export_colored_pointcloud_ply(img_bgr, inv_depth, K, save_path, stride=2):
    """
    Export colored point cloud (.ply) from image + inverse depth map.
    """
    try:
        import open3d as o3d
    except ImportError as exc:
        raise ImportError('Missing open3d. Install with: pip install open3d') from exc

    img_bgr = np.asarray(img_bgr)
    if img_bgr.dtype != np.uint8:
        if img_bgr.max() <= 1.0 and img_bgr.min() >= 0.0:
            img_bgr = np.clip(img_bgr * 255.0, 0, 255).astype(np.uint8)
        else:
            img_bgr = np.clip(img_bgr, 0, 255).astype(np.uint8)

    inv_depth = np.asarray(inv_depth, dtype=np.float32)
    h, w = inv_depth.shape
    ys = np.arange(0, h, stride, dtype=np.int32)
    xs = np.arange(0, w, stride, dtype=np.int32)
    xx, yy = np.meshgrid(xs, ys)
    xx = xx.reshape(-1)
    yy = yy.reshape(-1)
    inv_d = inv_depth[yy, xx]

    valid = np.isfinite(inv_d) & (inv_d > 1e-4) & (inv_d < 1e4)
    if not np.any(valid):
        return None, 'no valid points'

    xx = xx[valid].astype(np.float32)
    yy = yy[valid].astype(np.float32)
    inv_d = inv_d[valid]
    z = 1.0 / inv_d

    K_inv = np.linalg.inv(K).astype(np.float32)
    pix = np.stack([xx, yy, np.ones_like(xx)], axis=0)  # 3xN
    rays = K_inv @ pix
    pts = (rays * z[None, :]).T  # Nx3
    if EXPORT_MIRROR_X:
        pts[:, 0] *= -1.0

    rgb_u8 = img_bgr[yy.astype(np.int32), xx.astype(np.int32)][:, ::-1].astype(np.uint8)
    rgb_u8 = _enhance_rgb_for_display(rgb_u8)
    rgb = rgb_u8.astype(np.float32) / 255.0

    cloud = o3d.geometry.PointCloud()
    cloud.points = o3d.utility.Vector3dVector(pts)
    cloud.colors = o3d.utility.Vector3dVector(rgb)

    os.makedirs(os.path.dirname(save_path), exist_ok=True)
    ok = o3d.io.write_point_cloud(save_path, cloud)
    if not ok:
        return None, 'open3d failed to write point cloud'
    return save_path, 'ply'


def export_dense_colored_glb_from_inv_depth(
    img_bgr,
    inv_depth,
    K,
    save_path,
    stride=2,
    layout_polys=None,
):
    """
    Export dense triangle mesh (.glb) with per-vertex colors from image + inverse depth.
    This mirrors the core idea used in LGNet visualization: geometry from depth, color from panorama.
    """
    try:
        import trimesh
    except ImportError as exc:
        raise ImportError('Missing trimesh for dense GLB export. Install with: pip install trimesh') from exc

    img_bgr = np.asarray(img_bgr)
    if img_bgr.dtype != np.uint8:
        if img_bgr.max() <= 1.0 and img_bgr.min() >= 0.0:
            img_bgr = np.clip(img_bgr * 255.0, 0, 255).astype(np.uint8)
        else:
            img_bgr = np.clip(img_bgr, 0, 255).astype(np.uint8)

    inv_depth = np.asarray(inv_depth, dtype=np.float32)
    if inv_depth.ndim != 2:
        return None, 'inv_depth must be 2D'

    h, w = inv_depth.shape
    ys = np.arange(0, h, stride, dtype=np.int32)
    xs = np.arange(0, w, stride, dtype=np.int32)
    hh = len(ys)
    ww = len(xs)
    if hh < 2 or ww < 2:
        return None, 'not enough samples for mesh'

    xxg, yyg = np.meshgrid(xs, ys)
    inv_d = inv_depth[yyg, xxg]
    valid = np.isfinite(inv_d) & (inv_d > 1e-4) & (inv_d < 1e4)
    if not np.any(valid):
        return None, 'no valid depth samples'

    # Back-project with camera intrinsics for physically consistent room scale.
    K_inv = np.linalg.inv(K).astype(np.float32)
    pix = np.stack(
        [xxg.reshape(-1).astype(np.float32), yyg.reshape(-1).astype(np.float32), np.ones(hh * ww, dtype=np.float32)],
        axis=0
    )  # 3xN
    rays = K_inv @ pix
    z = (1.0 / np.clip(inv_d.reshape(-1), 1e-6, None)).astype(np.float32)
    pts = (rays * z[None, :]).T  # Nx3

    # Color each vertex from source image (RGB for glTF/GLB).
    rgb = img_bgr[yyg, xxg][:, :, ::-1].reshape(-1, 3).astype(np.uint8)

    # Build two triangles per cell; keep only fully-valid quads.
    pid = np.arange(hh * ww, dtype=np.int32).reshape(hh, ww)
    faces = []
    for r in range(hh - 1):
        for c in range(ww - 1):
            p00 = pid[r, c]
            p10 = pid[r + 1, c]
            p01 = pid[r, c + 1]
            p11 = pid[r + 1, c + 1]
            if not (valid[r, c] and valid[r + 1, c] and valid[r, c + 1] and valid[r + 1, c + 1]):
                continue
            faces.append([p00, p10, p01])
            faces.append([p10, p11, p01])

    if len(faces) == 0:
        return None, 'no valid faces after depth filtering'

    # Remove isolated geometry caused by strong depth discontinuities.
    faces_np = np.asarray(faces, dtype=np.int32)
    faces_np = faces_np[:, [0, 2, 1]]
    mesh = trimesh.Trimesh(vertices=pts, faces=faces_np, process=False)
    mesh.remove_unreferenced_vertices()
    if len(mesh.vertices) < 3 or len(mesh.faces) == 0:
        return None, 'mesh collapsed after cleanup'

    # Cache per-vertex color in original camera coordinate system BEFORE geometry alignment.
    # This avoids color loss caused by re-projecting transformed vertices with the original K.
    verts_cam = np.asarray(mesh.vertices, dtype=np.float32)
    proj_cam = (K @ verts_cam.T).T
    proj_cam_z = np.clip(proj_cam[:, 2:3], 1e-6, None)
    uv_cam = proj_cam[:, :2] / proj_cam_z
    u_cam = np.clip(np.round(uv_cam[:, 0]).astype(np.int32), 0, w - 1)
    v_cam = np.clip(np.round(uv_cam[:, 1]).astype(np.int32), 0, h - 1)
    rgb_clean = img_bgr[v_cam, u_cam][:, ::-1].astype(np.uint8)

    # ---- Dense floor leveling: keep dense colors but stabilize placement ----
    # 1) Estimate floor plane from lowest-Y vertices and rotate it to horizontal.
    verts = np.asarray(mesh.vertices, dtype=np.float64)
    y = verts[:, 1]
    yq = np.quantile(y, 0.12)
    floor_pts = verts[y <= yq]
    if floor_pts.shape[0] >= 50:
        floor_center = np.mean(floor_pts, axis=0)
        A = floor_pts - floor_center[None, :]
        _, _, vh = np.linalg.svd(A, full_matrices=False)
        normal = vh[-1]
        if normal[1] < 0:
            normal = -normal
        R = _rotation_matrix_from_vectors(normal, np.array([0.0, 1.0, 0.0], dtype=np.float64))
        verts = ((R @ (verts - floor_center).T).T + floor_center)

    # 2) Snap dense mesh floor to layout floor Y if layout is available.
    target_floor_y = None
    if layout_polys is not None and len(layout_polys) == 2 and layout_polys[1] is not None:
        layout_vs = []
        for p3 in layout_polys[1]:
            arr = np.asarray(p3, dtype=np.float64)
            if arr.shape[0] > 1 and np.allclose(arr[0], arr[-1]):
                arr = arr[:-1]
            if arr.shape[0] > 0:
                layout_vs.append(arr)
        if layout_vs:
            all_layout = np.concatenate(layout_vs, axis=0)
            target_floor_y = float(np.min(all_layout[:, 1]))
    if target_floor_y is None:
        target_floor_y = 0.0

    dense_floor_y = float(np.min(verts[:, 1]))
    verts[:, 1] += (target_floor_y - dense_floor_y)
    mesh.vertices = verts.astype(np.float32)
    if EXPORT_MIRROR_X:
        mv = np.asarray(mesh.vertices, dtype=np.float32)
        mv[:, 0] *= -1.0
        mesh.vertices = mv
        mesh.faces = np.asarray(mesh.faces, dtype=np.int32)[:, [0, 2, 1]]

    rgba = np.concatenate([rgb_clean, np.full((rgb_clean.shape[0], 1), 255, dtype=np.uint8)], axis=1)
    mesh.visual = trimesh.visual.ColorVisuals(mesh=mesh, vertex_colors=rgba)
    mesh.visual.material = trimesh.visual.material.PBRMaterial(
        baseColorFactor=[255, 255, 255, 255],
        metallicFactor=0.0,
        roughnessFactor=1.0,
    )

    os.makedirs(os.path.dirname(save_path), exist_ok=True)
    mesh.export(save_path)
    cmin = rgb_clean.min(axis=0).tolist()
    cmax = rgb_clean.max(axis=0).tolist()
    print(f'Dense GLB vertex color range RGB(min/max in [0..255]): {cmin} / {cmax}')
    print(f'Dense GLB floor aligned to Y={target_floor_y:.4f}')
    return save_path, 'dense_glb'


def resolve_pretrained_path(pretrained, model_name, checkpoints_dir):
    if pretrained:
        return pretrained

    if not model_name:
        raise ValueError('Please provide --pretrained or --model_name')

    alias_map = {
        'model1': 'model1.pt',
        'model2': 'model2.pt',
    }
    filename = alias_map.get(model_name.lower(), model_name)
    if not filename.lower().endswith('.pt'):
        filename = f'{filename}.pt'
    model_path = os.path.join(checkpoints_dir, filename)

    if not os.path.isfile(model_path):
        # Fallback: automatically pick an available checkpoint in checkpoints_dir.
        # This keeps "run.py" usable even when alias file names differ.
        if os.path.isdir(checkpoints_dir):
            candidates = []
            for name in os.listdir(checkpoints_dir):
                p = os.path.join(checkpoints_dir, name)
                if os.path.isfile(p) and name.lower().endswith('.pt'):
                    candidates.append(p)
            if candidates:
                candidates.sort(key=lambda p: os.path.getmtime(p), reverse=True)
                picked = candidates[0]
                print(
                    f'Checkpoint alias "{filename}" not found. '
                    f'Using latest available checkpoint: "{picked}"'
                )
                return picked
        raise FileNotFoundError(
            f'Checkpoint not found: "{model_path}". '
            f'Create it, put a .pt file in "{checkpoints_dir}", or pass --pretrained explicitly.'
        )
    return model_path


def match_by_Hungarian(gt, pred):
    n = len(gt)
    m = len(pred)
    gt = np.array(gt)
    pred = np.array(pred)
    valid = (gt.sum(0) > 0).sum()
    if m == 0:
        raise IOError
    else:
        gt = gt[:, np.newaxis, :, :]
        pred = pred[np.newaxis, :, :, :]
        cost = np.sum((gt+pred) == 2, axis=(2, 3))  # n*m
        row, col = linear_sum_assignment(-1 * cost)
        inter = cost[row, col].sum()
        PE = inter / valid
        return 1 - PE


def evaluate(gtseg, gtdepth, preseg, predepth, evaluate_2D=True, evaluate_3D=True):
    image_iou, image_pe, merror_edge, rmse, us_rmse = 0, 0, 0, 0, 0
    if evaluate_2D:
        # Parse GT polys
        gt_polys_masks = []
        h, w = gtseg.shape
        gt_polys_edges_mask = np.zeros((h, w))
        edge_thickness = 1
        gt_valid_seg = np.ones((h, w))
        labels = np.unique(gtseg)
        for i, label in enumerate(labels):
            gt_poly_mask = gtseg == label
            if label == -1:
                gt_valid_seg[gt_poly_mask] = 0  # zero pad region
            else:
                contours_, hierarchy = cv2.findContours(gt_poly_mask.astype(
                    np.uint8), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
                cv2.polylines(gt_polys_edges_mask, contours_, isClosed=True, color=[
                              1.], thickness=edge_thickness)
                gt_polys_masks.append(gt_poly_mask.astype(np.int32))

        def sortPolyBySize(mask):
            return mask.sum()
        gt_polys_masks.sort(key=sortPolyBySize, reverse=True)

        # Parse predictions
        pred_polys_masks = []
        pred_polys_edges_mask = np.zeros((h, w))
        pre_invalid_seg = np.zeros((h, w))
        labels = np.unique(preseg)
        for i, label in enumerate(labels):
            pred_poly_mask = np.logical_and(preseg == label, gt_valid_seg == 1)
            if pred_poly_mask.sum() == 0:
                continue
            if label == -1:
                # zero pad and infinity region
                pre_invalid_seg[pred_poly_mask] = 1
            else:
                contours_, hierarchy = cv2.findContours(pred_poly_mask.astype(
                    np.uint8), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)  # cv2.CHAIN_APPROX_SIMPLE
                cv2.polylines(pred_polys_edges_mask, contours_, isClosed=True, color=[
                              1.], thickness=edge_thickness)
                pred_polys_masks.append(pred_poly_mask.astype(np.int32))
        if len(pred_polys_masks) == 0.:
            pred_polys_edges_mask[edge_thickness:-
                                  edge_thickness, edge_thickness:-edge_thickness] = 1
            pred_polys_edges_mask = 1 - pred_polys_edges_mask
            pred_poly_mask = np.ones((h, w))
            pred_polys_masks = [pred_poly_mask]

        pred_polys_masks_cand = copy.copy(pred_polys_masks)
        # Assign predictions to ground truth polygons
        ordered_preds = []
        for gt_ind, gt_poly_mask in enumerate(gt_polys_masks):
            best_iou_score = 0.3
            best_pred_ind = None
            best_pred_poly_mask = None
            if len(pred_polys_masks_cand) == 0:
                break
            for pred_ind, pred_poly_mask in enumerate(pred_polys_masks_cand):
                gt_pred_add = gt_poly_mask + pred_poly_mask
                inter = np.equal(gt_pred_add, 2.).sum()
                union = np.greater(gt_pred_add, 0.).sum()
                iou_score = inter / union

                if iou_score > best_iou_score:
                    best_iou_score = iou_score
                    best_pred_ind = pred_ind
                    best_pred_poly_mask = pred_poly_mask
            ordered_preds.append(best_pred_poly_mask)

            pred_polys_masks_cand = [pred_poly_mask for pred_ind, pred_poly_mask in enumerate(pred_polys_masks_cand)
                                     if pred_ind != best_pred_ind]
            if best_pred_poly_mask is None:
                continue

        ordered_preds += pred_polys_masks_cand
        class_num = max(len(ordered_preds), len(gt_polys_masks))
        colormap = _validate_colormap(None, class_num + 1)

        # Generate GT poly mask
        gt_layout_mask = np.zeros((h, w))
        gt_layout_mask_colored = np.zeros((h, w, 3))
        for gt_ind, gt_poly_mask in enumerate(gt_polys_masks):
            gt_layout_mask = np.maximum(
                gt_layout_mask, gt_poly_mask * (gt_ind + 1))
            gt_layout_mask_colored += gt_poly_mask[:,
                                                   :, None] * colormap[gt_ind + 1]

        # Generate pred poly mask
        pred_layout_mask = np.zeros((h, w))
        pred_layout_mask_colored = np.zeros((h, w, 3))
        for pred_ind, pred_poly_mask in enumerate(ordered_preds):
            if pred_poly_mask is not None:
                pred_layout_mask = np.maximum(
                    pred_layout_mask, pred_poly_mask * (pred_ind + 1))
                pred_layout_mask_colored += pred_poly_mask[:,
                                                           :, None] * colormap[pred_ind + 1]

        # Calc IOU
        ious = []
        for layout_comp_ind in range(1, len(gt_polys_masks) + 1):
            inter = np.logical_and(np.equal(gt_layout_mask, layout_comp_ind),
                                   np.equal(pred_layout_mask, layout_comp_ind)).sum()
            fp = np.logical_and(np.not_equal(gt_layout_mask, layout_comp_ind),
                                np.equal(pred_layout_mask, layout_comp_ind)).sum()
            fn = np.logical_and(np.equal(gt_layout_mask, layout_comp_ind),
                                np.not_equal(pred_layout_mask, layout_comp_ind)).sum()
            union = inter + fp + fn
            iou = inter / union
            ious.append(iou)

        image_iou = sum(ious) / class_num

        # Calc PE
        image_pe = 1 - np.equal(gt_layout_mask[gt_valid_seg == 1],
                                pred_layout_mask[gt_valid_seg == 1]).sum() / (np.sum(gt_valid_seg == 1))
        # Calc PE by Hungarian
        image_pe_hung = match_by_Hungarian(gt_polys_masks, pred_polys_masks)
        # Calc edge error
        # ignore edges at image borders
        img_bound_mask = np.zeros_like(pred_polys_edges_mask)
        img_bound_mask[10:-10, 10:-10] = 1

        pred_dist_trans = cv2.distanceTransform((img_bound_mask * (1 - pred_polys_edges_mask)).astype(np.uint8),
                                                cv2.DIST_L2, 3)
        gt_dist_trans = cv2.distanceTransform((img_bound_mask * (1 - gt_polys_edges_mask)).astype(np.uint8),
                                              cv2.DIST_L2, 3)

        chamfer_dist = pred_polys_edges_mask * gt_dist_trans + \
            gt_polys_edges_mask * pred_dist_trans
        merror_edge = 0.5 * np.sum(chamfer_dist) / np.sum(
            np.greater(img_bound_mask * (gt_polys_edges_mask), 0))

    # Evaluate in 3D
    if evaluate_3D:
        max_depth = 50
        gt_layout_depth_img_mask = np.greater(gtdepth, 0.)
        gt_layout_depth_img = 1. / gtdepth[gt_layout_depth_img_mask]
        gt_layout_depth_img = np.clip(gt_layout_depth_img, 0, max_depth)
        gt_layout_depth_med = np.median(gt_layout_depth_img)
        # max_depth = np.max(gt_layout_depth_img)
        # may be max_depth should be max depth of all scene
        predepth[predepth == 0] = 1 / max_depth
        pred_layout_depth_img = 1. / predepth[gt_layout_depth_img_mask]
        pred_layout_depth_img = np.clip(pred_layout_depth_img, 0, max_depth)
        pred_layout_depth_med = np.median(pred_layout_depth_img)

        # Calc MSE
        ms_error_image = (pred_layout_depth_img - gt_layout_depth_img) ** 2
        rmse = np.sqrt(np.sum(ms_error_image) /
                       np.sum(gt_layout_depth_img_mask))

        # Calc up to scale MSE
        if np.isnan(pred_layout_depth_med) or pred_layout_depth_med == 0:
            d_scale = 1.
        else:
            d_scale = gt_layout_depth_med / pred_layout_depth_med
        us_ms_error_image = (
            d_scale * pred_layout_depth_img - gt_layout_depth_img) ** 2
        us_rmse = np.sqrt(np.sum(us_ms_error_image) /
                          np.sum(gt_layout_depth_img_mask))

    return image_iou, image_pe, merror_edge, rmse, us_rmse, image_pe_hung


def test_structured3d(model, criterion, dataloader, device, cfg):
    model.eval()
    results = []
    for iters, inputs in enumerate(dataloader):
        print(f'{iters}/{len(dataloader)}')
        # set device
        for key, value in inputs.items():
            inputs[key] = value.to(device)

        # forward
        x = model(inputs['img'])
        loss, loss_stats = criterion(x, **inputs)

        # post process on output feature map size and extract plane and line detection results
        dt_planes, dt_lines, dt_params3d_instance, dt_params3d_pixelwise = post_process(x, Mnms=1)

        for i in range(1):
            # generate layout with a post-process according to detection results
            (_ups, _downs, _attribution, _params_layout), (ups, downs, attribution, params_layout), (pfloor, pceiling) = Reconstruction(
                dt_planes[i],
                dt_params3d_instance[i],
                dt_lines[i],
                K=inputs['intri'][i].cpu().numpy(),
                size=(720, 1280),
                threshold=(0.3, 0.3, 0.3, 0.3))

            # convert no opt results to segmentation and depth map and evaluate results
            _seg, _depth, _, _polys = ConvertLayout(
                inputs['img'][i], _ups, _downs, _attribution,
                K=inputs['intri'][i].cpu().numpy(), pwalls=_params_layout,
                pfloor=pfloor, pceiling=pceiling,
                ixy1map=inputs['ixy1map'][i].cpu().numpy(),
                valid=inputs['iseg'][i].cpu().numpy(),
                oxy1map=inputs['oxy1map'][i].cpu().numpy(), pixelwise=None)

            _res = evaluate(inputs['iseg'][i].cpu().numpy(),
                            inputs['idepth'][i].cpu().numpy(), _seg, _depth)

            # convert opt results to segmentation and depth map and evaluate results
            seg, depth, img, polys = ConvertLayout(
                inputs['img'][i], ups, downs, attribution,
                K=inputs['intri'][i].cpu().numpy(), pwalls=params_layout,
                pfloor=pfloor, pceiling=pceiling,
                ixy1map=inputs['ixy1map'][i].cpu().numpy(),
                valid=inputs['iseg'][i].cpu().numpy(),
                oxy1map=inputs['oxy1map'][i].cpu().numpy(), pixelwise=None)

            res = evaluate(inputs['iseg'][i].cpu().numpy(),
                           inputs['idepth'][i].cpu().numpy(), seg, depth)

            # print metric results
            results.append([_res, res])
            print(np.mean(np.array(results), axis=0))

            if cfg.visual:
                # display layout
                DisplayLayout(img, seg, depth, polys, _seg, _depth, _polys, inputs['iseg'][i].cpu(
                ).numpy(), inputs['ilbox'][i].cpu().numpy(), iters)


def test_nyu303(model, criterion, dataloader, device, cfg):
    model.eval()
    results = []
    for iters, inputs in enumerate(dataloader):
        print(f'{iters}/{len(dataloader)}')
        for key, value in inputs.items():
            inputs[key] = value.to(device)
        # forward
        x = model(inputs['img'])
        loss, loss_stats = criterion(x)

        # post process on output feature map size
        dt_planes, dt_lines, dt_params3d_instance, dt_params3d_pixelwise = post_process(x, Mnms=1)
        # convert sunrgbd crop image to fullres img
        dt_planes[:, :, :4] = dt_planes[:, :, :4] + \
            np.array([41, 45, 41, 45]) / 4.
        dt_lines[:, :, 1] = dt_lines[:, :, 1] + \
            41/4. - dt_lines[:, :, 0] * 45/4.

        # reconstruction
        for i in range(1):
            (_ups, _downs, _attribution, _params_layout), (ups, downs, attribution, params_layout), (pfloor, pceiling) = Reconstruction(
                dt_planes[i], 
                dt_params3d_instance[i], 
                dt_lines[i],
                K=inputs['full_intri'][i].cpu().numpy(), 
                size=(480, 640), 
                threshold=(0.12, 0.1, 0.1, 0.2), 
                downsample=4)

            # no opt
            _seg, _depth, img, _ = ConvertLayout(inputs['fullimg'][i], _ups, _downs, _attribution,
                                                 K=inputs['full_intri'][i].cpu().numpy(), pwalls=_params_layout,
                                                 pfloor=pfloor, pceiling=pceiling,
                                                 ixy1map=inputs['ixy1map'][i].cpu().numpy(), valid=inputs['iseg'][i].cpu().numpy())
            _res = evaluate(inputs['iseg'][i].cpu().numpy(), inputs['idepth'][i].cpu().numpy(), _seg, _depth)
            
            # opt
            seg, depth, _, _ = ConvertLayout(inputs['fullimg'][i], ups, downs, attribution,
                                             K=inputs['full_intri'][i].cpu().numpy(), pwalls=params_layout,
                                             pfloor=pfloor, pceiling=pceiling,
                                             ixy1map=inputs['ixy1map'][i].cpu().numpy(), valid=inputs['iseg'][i].cpu().numpy())
            res = evaluate(inputs['iseg'][i].cpu().numpy(), inputs['idepth'][i].cpu().numpy(), seg, depth)

            results.append([_res, res])
            print(np.mean(np.array(results), axis=0)[:,-1])
            
            if cfg.visual:
                display2Dseg(img=inputs['fullimg'][i], segs_pred=seg, segs_gt=inputs['iseg'][i].cpu().numpy(), label=inputs['ilbox'][0].cpu().numpy(),
                            iters=f'{iters}', method='opt_nyu303', draw_gt=1)
            if cfg.exam:
                return


def test_custom(model, criterion, dataloader, device, cfg):
    model.eval()
    total = len(dataloader)
    for iters, inputs in enumerate(dataloader):
        print(f'{iters + 1}/{total}')
        # set device
        for key, value in inputs.items():
            inputs[key] = value.to(device)
        # forward
        x = model(inputs['img'])
        loss, loss_stats = criterion(x)

        # post process on output feature map size, and extract planes, lines, plane params instance and plane params pixelwise
        dt_planes, dt_lines, dt_params3d_instance, dt_params3d_pixelwise = post_process(x, Mnms=1)

        # reconstruction according to detection results
        for i in range(1):
            src_bgr = tensor_image_to_bgr_u8(inputs['img'][i])
            (_ups, _downs, _attribution, _params_layout), (ups, downs, attribution, params_layout), ( pfloor, pceiling) = Reconstruction(
                dt_planes[i],
                dt_params3d_instance[i],
                dt_lines[i],
                K=inputs['intri'][i].cpu().numpy(),
                size=(720, 1280),
                threshold=(0.3, 0.05, 0.05, 0.3))

            # convert intersection points to segmentation for visual
            # no opt results
            _seg, _depth, _, _polys = ConvertLayout(
                inputs['img'][i], _ups, _downs, _attribution, K=inputs['intri'][i].cpu().numpy(),
                pwalls=_params_layout, pfloor=pfloor, pceiling=pceiling,
                ixy1map=inputs['ixy1map'][i].cpu().numpy(),
                valid=inputs['iseg'][i].cpu().numpy(),
                oxy1map=inputs['oxy1map'][i].cpu().numpy(),
                pixelwise=None
            )
            # opt results
            seg, depth, img, polys = ConvertLayout(
                inputs['img'][i], ups, downs, attribution, K=inputs['intri'][i].cpu().numpy(),
                pwalls=params_layout, pfloor=pfloor, pceiling=pceiling,
                ixy1map=inputs['ixy1map'][i].cpu().numpy(),
                valid=inputs['iseg'][i].cpu().numpy(),
                oxy1map=inputs['oxy1map'][i].cpu().numpy(),
                pixelwise=None
            )

            # Always save results for CUSTOM inference to make output discoverable.
            DisplayLayout(
                img, seg, depth, polys, _seg, _depth, _polys,
                inputs['iseg'][i].cpu().numpy(),
                inputs['ilbox'][i].cpu().numpy(),
                iters,
            )
            mesh_name = f'{iters}.glb'
            mesh_path = os.path.join(cfg.mesh_dir, mesh_name)
            exported_path, export_kind = export_layout_glb(polys, src_bgr, mesh_path)
            if exported_path is not None:
                print(f'Exported mesh ({export_kind}): {exported_path}')
            else:
                print(f'Skipped mesh export for sample {iters}: {export_kind}')
            aligned_name = f'{iters}_aligned.glb'
            aligned_path = os.path.join(cfg.mesh_dir, aligned_name)
            aligned_exported, aligned_kind = export_layout_aligned_glb(
                polys,
                src_bgr,
                seg,
                depth,
                inputs['intri'][i].cpu().numpy(),
                aligned_path,
                stride=2,
            )
            if aligned_exported is not None:
                print(f'Exported aligned mesh ({aligned_kind}): {aligned_exported}')
            else:
                print(f'Skipped aligned mesh export for sample {iters}: {aligned_kind}')
            pcd_name = f'{iters}_cloud.ply'
            pcd_path = os.path.join(cfg.mesh_dir, pcd_name)
            pcd_exported, pcd_kind = export_colored_pointcloud_ply(
                src_bgr,
                depth,
                inputs['intri'][i].cpu().numpy(),
                pcd_path,
                stride=2,
            )
            if pcd_exported is not None:
                print(f'Exported point cloud ({pcd_kind}): {pcd_exported}')
            else:
                print(f'Skipped point cloud export for sample {iters}: {pcd_kind}')
            dense_mesh_name = f'{iters}_dense.glb'
            dense_mesh_path = os.path.join(cfg.mesh_dir, dense_mesh_name)
            dense_exported, dense_kind = export_dense_colored_glb_from_inv_depth(
                src_bgr,
                depth,
                inputs['intri'][i].cpu().numpy(),
                dense_mesh_path,
                stride=2,
                layout_polys=polys,
            )
            if dense_exported is not None:
                print(f'Exported dense mesh ({dense_kind}): {dense_exported}')
            else:
                print(f'Skipped dense mesh export for sample {iters}: {dense_kind}')
            if cfg.visual:
                # keep the same behavior flag; files are already saved above.
                pass
    print(f'Finished CUSTOM inference: {total}/{total}')
    print('Saved outputs to: results/')
    print(f'Saved meshes to: {cfg.mesh_dir}')

def parse():
    parser = argparse.ArgumentParser()

    parser.add_argument('--data', type=str, default='Structured3D', choices=['Structured3D', 'NYU303', 'CUSTOM'])
    parser.add_argument('--pretrained', type=str, default=None, help='full checkpoint path')
    parser.add_argument('--model_name', type=str, default='model1', help='checkpoint alias in checkpoints dir, e.g. model1/model2')
    parser.add_argument('--checkpoints_dir', type=str, default='checkpoints', help='checkpoint folder')
    parser.add_argument('--images_dir', type=str, default='input_images', help='folder containing custom images')
    parser.add_argument('--all_images', action='store_true', help='run all images in images_dir (default only first image)')
    parser.add_argument('--mesh_dir', type=str, default='results_mesh', help='output folder for GLB files')
    parser.add_argument('--visual', action='store_true', help='whether to visual the results')
    parser.add_argument('--exam', action='store_true', help='test one example on nyu303 dataset')
    parser.add_argument('--num_workers', type=int, default=0)

    args = parser.parse_args()
    return args

if __name__ == '__main__':
    with open('cfg.yaml', 'r') as f:
        config = yaml.safe_load(f)
        cfg = EasyDict(config)
    args = parse()
    cfg.update(vars(args))

    if cfg.exam:
        assert cfg.data == 'NYU303', 'provide one example of nyu303 to test'
    #  dataset
    if cfg.data == 'Structured3D':
        dataset = Structured3D(cfg.Dataset.Structured3D, 'test')
    elif cfg.data == 'NYU303':
        dataset = NYU303(cfg.Dataset.NYU303, 'test', exam=cfg.exam)
    elif cfg.data == 'CUSTOM':
        dataset = CustomDataset(
            cfg.Dataset.CUSTOM,
            'test',
            files=cfg.images_dir,
            use_first_image=not cfg.all_images,
        )
    else:
        raise NotImplementedError

    dataloader = torch.utils.data.DataLoader(dataset, num_workers=cfg.num_workers)

    # create network
    model = Detector()
    # compute loss
    criterion = Loss(cfg.Weights)

    # set data parallel
    # if cfg.num_gpus > 1 and torch.cuda.is_available():
    #     model = torch.nn.DataParallel(model)

    # reload weights
    pretrained_path = resolve_pretrained_path(
        cfg.pretrained,
        cfg.model_name,
        cfg.checkpoints_dir,
    )
    if pretrained_path:
        state_dict = torch.load(pretrained_path,
                                map_location=torch.device('cpu'))
        model.load_state_dict(state_dict)

    # set device
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    model.to(device)
    criterion.to(device)

    if cfg.data == 'Structured3D':
        test_structured3d(model, criterion, dataloader, device, cfg)
    elif cfg.data == 'NYU303':
        test_nyu303(model, criterion, dataloader, device, cfg)
    elif cfg.data == 'CUSTOM':
        test_custom(model, criterion, dataloader, device, cfg)
    else:
        raise NotImplementedError


