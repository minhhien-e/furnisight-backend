"""
@author: Zhigang Jiang
@time: 2022/05/25
@description: reference: https://github.com/sunset1995/PanoPlane360/blob/main/vis_planes.py
"""
import open3d
import numpy as np
from pipelines.lgnet.utils.conversion import pixel2lonlat


def create_3d_obj(img, depth, save_path=None, mesh=True, mesh_show_back_face=False, show=False):
    assert img.shape[0] == depth.shape[0], ""
    h = img.shape[0]
    w = img.shape[1]
    # Project to 3d
    lon = pixel2lonlat(np.array(range(w)), w=w, axis=0)[None].repeat(h, axis=0)
    lat = pixel2lonlat(np.array(range(h)), h=h, axis=1)[..., None].repeat(w, axis=1)

    z = depth * np.sin(lat)
    x = depth * np.cos(lat) * np.cos(lon)
    y = depth * np.cos(lat) * np.sin(lon)
    pts_xyz = np.stack([x, -z, y], -1).reshape(-1, 3)
    pts_rgb = img.reshape(-1, 3)

    # Ghi chÃº (VN):
    # - áº¢nh 2D phÃ­a trÃªn lÃ  áº£nh gá»‘c nÃªn mÃ u "Ä‘Ãºng" theo cáº£m nháº­n.
    # - Khi render 3D (Ä‘áº·c biá»‡t glTF/GLB), viewer thÆ°á»ng dÃ¹ng PBR + lighting máº·c Ä‘á»‹nh,
    #   nÃªn mesh cÃ³ thá»ƒ bá»‹ tá»‘i hÆ¡n áº£nh gá»‘c dÃ¹ vertex color giá»‘ng nhau.
    # - Workaround nhanh: (1) tÃ­nh normals Ä‘á»ƒ shading á»•n Ä‘á»‹nh hÆ¡n, (2) boost nháº¹ mÃ u
    #   riÃªng cho glTF/GLB Ä‘á»ƒ gáº§n giá»‘ng áº£nh gá»‘c trong nhiá»u viewer (Gradio/Three.js).
    #
    # Náº¿u báº¡n tháº¥y váº«n tá»‘i hoáº·c quÃ¡ sÃ¡ng, hÃ£y chá»‰nh 2 háº±ng sá»‘ dÆ°á»›i Ä‘Ã¢y.
    GLTF_GLB_BRIGHTNESS = 1.35  # há»‡ sá»‘ tÄƒng sÃ¡ng cho .gltf/.glb (thá»­ 1.15 ~ 1.60)
    GLTF_GLB_GAMMA = 1.0        # gamma bá»• sung (1.0 = táº¯t). Thá»­ 0.9~1.2 náº¿u cáº§n.

    if save_path is not None:
        ext = str(save_path).lower()
        if ext.endswith('.gltf') or ext.endswith('.glb'):
            # TÄƒng sÃ¡ng cho glTF/GLB Ä‘á»ƒ bÃ¹ viá»‡c viewer render cÃ³ lighting.
            # LÆ°u Ã½: Ä‘Ã¢y lÃ  "hack" theo render pipeline phá»• biáº¿n, khÃ´ng pháº£i thay Ä‘á»•i model.
            pts_rgb = np.clip(pts_rgb * GLTF_GLB_BRIGHTNESS, 0.0, 1.0)
            if GLTF_GLB_GAMMA != 1.0:
                pts_rgb = np.clip(np.power(pts_rgb, GLTF_GLB_GAMMA), 0.0, 1.0)

    if mesh:
        pid = np.arange(len(pts_xyz)).reshape(h, w)
        faces = np.concatenate([
            np.stack([
                pid[:-1, :-1], pid[1:, :-1], np.roll(pid, -1, axis=1)[:-1, :-1],
            ], -1),
            np.stack([
                pid[1:, :-1], np.roll(pid, -1, axis=1)[1:, :-1], np.roll(pid, -1, axis=1)[:-1, :-1],
            ], -1)
        ]).reshape(-1, 3).tolist()
        scene = open3d.geometry.TriangleMesh()
        scene.vertices = open3d.utility.Vector3dVector(pts_xyz)
        scene.vertex_colors = open3d.utility.Vector3dVector(pts_rgb)
        scene.triangles = open3d.utility.Vector3iVector(faces)
        # TÃ­nh normals giÃºp shading á»•n Ä‘á»‹nh hÆ¡n trong cÃ¡c viewer 3D.
        scene.compute_vertex_normals()
        scene.compute_triangle_normals()

    else:
        scene = open3d.geometry.PointCloud()
        scene.points = open3d.utility.Vector3dVector(pts_xyz)
        scene.colors = open3d.utility.Vector3dVector(pts_rgb)
    if save_path:
        open3d.io.write_triangle_mesh(save_path, scene, write_triangle_uvs=True)
    if show:
        open3d.visualization.draw_geometries([scene], mesh_show_back_face=mesh_show_back_face)

