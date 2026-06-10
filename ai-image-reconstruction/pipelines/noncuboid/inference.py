import os
from pathlib import Path

import numpy as np
import torch
import yaml
from easydict import EasyDict

from pipelines.noncuboid.datasets.custom import CustomDataset
from pipelines.noncuboid.legacy_test import (
    export_dense_colored_glb_from_inv_depth,
    post_process,
    resolve_pretrained_path,
    tensor_image_to_bgr_u8,
)
from pipelines.noncuboid.models import ConvertLayout, Detector, DisplayLayout, Loss, Reconstruction


QUALITY_TO_STRIDE = {
    "low": 4,
    "medium": 2,
    "high": 1,
}


def _as_batch_tensors(sample, device):
    batch = {}
    for key, value in sample.items():
        if torch.is_tensor(value):
            tensor = value
        else:
            tensor = torch.as_tensor(value)
        batch[key] = tensor.unsqueeze(0).to(device)
    return batch


def load_config(cfg_path="pipelines/noncuboid/cfg.yaml"):
    with open(cfg_path, "r", encoding="utf-8") as f:
        return EasyDict(yaml.safe_load(f))


def load_model(
    device=None,
    cfg_path="pipelines/noncuboid/cfg.yaml",
    checkpoints_dir="checkpoints/Structured3D",
    pretrained=None,
    model_name="best",
):
    cfg = load_config(cfg_path)
    model = Detector()
    pretrained_path = resolve_pretrained_path(pretrained, model_name, checkpoints_dir)
    state_dict = torch.load(pretrained_path, map_location=torch.device("cpu"))
    model.load_state_dict(state_dict)
    device = device or ("cuda" if torch.cuda.is_available() else "cpu")
    model.to(device)
    model.eval()
    return model, cfg, device, pretrained_path


@torch.no_grad()
def run_one_image(
    image_path,
    model,
    cfg,
    device,
    name,
    output_dir,
    mesh_quality="medium",
):
    mesh_quality = (mesh_quality or "medium").lower()
    if mesh_quality not in QUALITY_TO_STRIDE:
        raise ValueError("mesh_quality must be one of: low, medium, high")
    stride = QUALITY_TO_STRIDE[mesh_quality]

    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    image_path = Path(image_path)
    dataset = CustomDataset(
        cfg.Dataset.CUSTOM,
        "test",
        files=str(image_path.parent),
        use_first_image=True,
    )
    dataset.filenames = [image_path.name]
    inputs = _as_batch_tensors(dataset[0], device)

    outputs = model(inputs["img"])
    criterion = Loss(cfg.Weights).to(device)
    criterion(outputs)
    dt_planes, dt_lines, dt_params3d_instance, _ = post_process(outputs, Mnms=1)

    src_bgr = tensor_image_to_bgr_u8(inputs["img"][0])
    (_ups, _downs, _attribution, _params_layout), (ups, downs, attribution, params_layout), (
        pfloor,
        pceiling,
    ) = Reconstruction(
        dt_planes[0],
        dt_params3d_instance[0],
        dt_lines[0],
        K=inputs["intri"][0].cpu().numpy(),
        size=(720, 1280),
        threshold=(0.3, 0.05, 0.05, 0.3),
    )

    _seg, _depth, _, _polys = ConvertLayout(
        inputs["img"][0],
        _ups,
        _downs,
        _attribution,
        K=inputs["intri"][0].cpu().numpy(),
        pwalls=_params_layout,
        pfloor=pfloor,
        pceiling=pceiling,
        ixy1map=inputs["ixy1map"][0].cpu().numpy(),
        valid=inputs["iseg"][0].cpu().numpy(),
        oxy1map=inputs["oxy1map"][0].cpu().numpy(),
        pixelwise=None,
    )
    seg, depth, layout_img, polys = ConvertLayout(
        inputs["img"][0],
        ups,
        downs,
        attribution,
        K=inputs["intri"][0].cpu().numpy(),
        pwalls=params_layout,
        pfloor=pfloor,
        pceiling=pceiling,
        ixy1map=inputs["ixy1map"][0].cpu().numpy(),
        valid=inputs["iseg"][0].cpu().numpy(),
        oxy1map=inputs["oxy1map"][0].cpu().numpy(),
        pixelwise=None,
    )

    dense_path = output_dir / f"{name}_normal_dense.glb"

    try:
        DisplayLayout(
            layout_img.copy(),
            seg,
            depth,
            polys,
            _seg,
            _depth,
            _polys,
            inputs["iseg"][0].cpu().numpy(),
            inputs["ilbox"][0].cpu().numpy(),
            f"{name}_normal",
            output_dir=str(output_dir),
        )
    except Exception as exc:
        print(f"Skipped NonCuboid select overlay export: {exc}")

    dense_exported, dense_kind = export_dense_colored_glb_from_inv_depth(
        src_bgr,
        depth,
        inputs["intri"][0].cpu().numpy(),
        str(dense_path),
        stride=stride,
        layout_polys=polys,
    )
    if dense_exported is not None:
        return dense_exported, dense_kind
    raise RuntimeError("NonCuboid mesh export failed")

