"""
Pipeline gá»n Ä‘á»ƒ port sang há»‡ thá»‘ng khÃ¡c:
1) nháº­n áº£nh 2D
2) cháº¡y model -> ra sá»‘ (depth/ratio)
3) háº­u xá»­ lÃ½ layout
4) dá»±ng 3D + gÃ¡n mÃ u tá»« áº£nh
5) export thÃ nh file mesh (.glb/.gltf/.obj)
"""

import os
from typing import Dict, Optional

import cv2
import numpy as np
import torch

from pipelines.lgnet.postprocessing.post_process import post_process
from pipelines.lgnet.preprocessing.pano_lsd_align import panoEdgeDetection, rotatePanorama
from pipelines.lgnet.utils.boundary import corners2boundaries, layout2depth
from pipelines.lgnet.utils.conversion import depth2xyz
from pipelines.lgnet.utils.misc import tensor2np
from pipelines.lgnet.visualization.obj3d import create_3d_obj


class haotest:
    """
    Class gom toÃ n bá»™ luá»“ng tá»« output model (sá»‘) -> mesh 3D cÃ³ mÃ u.

    YÃªu cáº§u model Ä‘áº§u ra dáº¡ng dict tá»‘i thiá»ƒu:
    - dt["depth"]: tensor [B, N] (repo nÃ y dÃ¹ng N=256)
    - dt["ratio"]: tensor [B, 1]
    """

    def __init__(
        self,
        model,
        device: str = "cuda",
        post_processing: str = "manhattan",
        use_preprocess: bool = True,
    ):
        self.model = model
        self.device = device
        self.post_processing = post_processing
        self.use_preprocess = use_preprocess

        if "cuda" in device and not torch.cuda.is_available():
            self.device = "cpu"

        self.model = self.model.to(self.device)
        self.model.eval()

    def preprocess_image(self, img_rgb: np.ndarray, vp_cache_path: Optional[str] = None):
        """
        Tiá»n xá»­ lÃ½ panorama:
        - Optional VP alignment (xoay áº£nh theo vanishing points)
        - Normalize [0, 255] -> [0, 1]
        """
        img = img_rgb
        vp = None

        if self.use_preprocess:
            if vp_cache_path is not None and os.path.exists(vp_cache_path):
                with open(vp_cache_path, "r", encoding="utf-8") as f:
                    vp = [[float(v) for v in line.rstrip().split(" ")] for line in f.readlines()]
                    vp = np.array(vp)
            else:
                _, vp, _, _, _, _, _ = panoEdgeDetection(img, qError=0.7, refineIter=3)

            img = rotatePanorama(img, vp[2::-1])

            if vp_cache_path is not None:
                with open(vp_cache_path, "w", encoding="utf-8") as f:
                    for i in range(3):
                        f.write("%.6f %.6f %.6f\n" % (vp[i, 0], vp[i, 1], vp[i, 2]))

        img_norm = (img / 255.0).astype(np.float32)
        return img_norm, vp

    @torch.no_grad()
    def infer_numbers(self, img_norm: np.ndarray) -> Dict:
        """
        Cháº¡y model Ä‘á»ƒ láº¥y sá»‘ hÃ¬nh há»c.
        """
        x = torch.from_numpy(img_norm.transpose(2, 0, 1)[None]).to(self.device)
        dt = self.model(x)
        return dt

    def postprocess_layout(self, dt: Dict):
        """
        Tá»« depth/ratio -> cÃ¡c corner 3D cá»§a layout.
        """
        if self.post_processing != "original":
            dt["processed_xyz"] = post_process(tensor2np(dt["depth"]), type_name=self.post_processing)

        output_xyz = dt["processed_xyz"][0] if "processed_xyz" in dt else depth2xyz(tensor2np(dt["depth"][0]))
        ratio = tensor2np(dt["ratio"][0])[0]
        return output_xyz, ratio

    def build_layout_depth(self, output_xyz: np.ndarray, ratio: float, mesh_resolution: int = 256):
        """
        Chuyá»ƒn corner layout -> boundary -> depth map Ä‘á»ƒ dá»±ng mesh.
        """
        dt_boundaries = corners2boundaries(
            ratio,
            corners_xyz=output_xyz,
            step=None,
            length=mesh_resolution,
            visible=True,
        )
        dt_layout_depth = layout2depth(dt_boundaries, show=False)
        return dt_layout_depth

    def export_mesh(
        self,
        img_norm: np.ndarray,
        dt_layout_depth: np.ndarray,
        save_path: str,
    ):
        """
        Dá»±ng mesh vÃ  gÃ¡n mÃ u:
        - MÃ u láº¥y trá»±c tiáº¿p tá»« áº£nh 2D Ä‘áº§u vÃ o (vertex colors)
        - Export sang .glb/.gltf/.obj tÃ¹y Ä‘uÃ´i file
        """
        create_3d_obj(
            cv2.resize(img_norm, dt_layout_depth.shape[::-1]),
            dt_layout_depth,
            save_path=save_path,
            mesh=True,
            show=False,
        )
        return save_path

    def run(
        self,
        img_rgb: np.ndarray,
        save_path: str,
        mesh_resolution: int = 256,
        vp_cache_path: Optional[str] = None,
    ) -> Dict:
        """
        HÃ m cháº¡y end-to-end:
        input áº£nh -> sá»‘ tá»« model -> 3D mesh cÃ³ mÃ u -> file output.
        """
        img_norm, vp = self.preprocess_image(img_rgb, vp_cache_path=vp_cache_path)
        dt = self.infer_numbers(img_norm)
        output_xyz, ratio = self.postprocess_layout(dt)
        dt_layout_depth = self.build_layout_depth(output_xyz, ratio, mesh_resolution=mesh_resolution)
        mesh_path = self.export_mesh(img_norm, dt_layout_depth, save_path=save_path)

        # Tráº£ láº¡i cáº£ sá»‘ trung gian Ä‘á»ƒ bÃªn kia tá»± debug/Ä‘á»‘i chiáº¿u.
        return {
            "mesh_path": mesh_path,
            "ratio": float(ratio),
            "depth": tensor2np(dt["depth"][0]),
            "output_xyz": output_xyz,
            "vp": vp,
        }

