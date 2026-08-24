import math
import os
import uuid
from argparse import Namespace
from pathlib import Path
from urllib.parse import quote

import numpy as np
import torch

def get_cpu_quota():
    try:
        if os.path.exists("/sys/fs/cgroup/cpu.max"):
            with open("/sys/fs/cgroup/cpu.max") as f:
                quota, period = f.read().split()
                if quota != "max":
                    return math.ceil(int(quota) / int(period))
        elif os.path.exists("/sys/fs/cgroup/cpu/cpu.cfs_quota_us"):
            with open("/sys/fs/cgroup/cpu/cpu.cfs_quota_us") as f:
                quota = int(f.read())
            if quota > -1:
                with open("/sys/fs/cgroup/cpu/cpu.cfs_period_us") as f:
                    period = int(f.read())
                return math.ceil(quota / period)
    except Exception:
        pass
    try:
        return len(os.sched_getaffinity(0))
    except AttributeError:
        return os.cpu_count() or 1

# Optimize PyTorch CPU Threading (Prevent core contention on Docker/Kubernetes)
# Auto-detect CPU limits from Docker's cgroup to avoid hardcoding threads.
cpu_threads = int(os.environ.get("OMP_NUM_THREADS", get_cpu_quota()))
torch.set_num_threads(max(1, cpu_threads))
torch.set_num_interop_threads(1)

from fastapi import FastAPI, File, Form, HTTPException, Request, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse, JSONResponse
from fastapi.staticfiles import StaticFiles
from PIL import Image
import mimetypes

mimetypes.add_type("model/gltf-binary", ".glb")
mimetypes.add_type("model/gltf+json", ".gltf")

from app.storage import (
    StorageConfigurationError,
    get_storage_backend,
    should_retain_local_outputs,
)
from pipelines.lgnet.config.defaults import get_config
from pipelines.lgnet.inference import preprocess, run_one_inference
from pipelines.lgnet.models.build import build_model
from pipelines.lgnet.utils.logger import get_logger


logger = get_logger()
app = FastAPI(title="LGNet + NonCuboid FastAPI Inference")


STATIC_DIR = Path("outputs/api")
STATIC_DIR.mkdir(parents=True, exist_ok=True)
app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")

MODELS = {}
NONCUBOID = {}


def get_model(cfg_path: str, device: str):
    args = Namespace(cfg=cfg_path, mode="test", device=device)
    config = get_config(args)
    best_pkl = os.path.join(config.CKPT.DIR, "best.pkl")
    if not os.path.exists(best_pkl):
        raise FileNotFoundError(
            f"Missing checkpoint: {best_pkl}. Please place best.pkl before starting FastAPI."
        )

    if "cuda" in device and not torch.cuda.is_available():
        logger.info(f"CUDA not available. Falling back to CPU for {cfg_path}.")
        device = "cpu"

    config.defrost()
    config.TRAIN.DEVICE = device
    config.freeze()

    model, _, _, _ = build_model(config, logger)
    return model


def load_models(device: str = "cuda"):
    MODELS["zind"] = get_model("pipelines/lgnet/configs/zind.yaml", device=device)


def get_noncuboid_model():
    if "model" not in NONCUBOID:
        from pipelines.noncuboid.inference import load_model

        model, cfg, device, pretrained_path = load_model()
        NONCUBOID.update(
            {
                "model": model,
                "cfg": cfg,
                "device": device,
                "pretrained_path": pretrained_path,
            }
        )
        logger.info(f"NonCuboid model loaded: {pretrained_path}")
    return NONCUBOID


@app.on_event("startup")
def on_startup():
    load_models(device="cuda")
    logger.info("FastAPI startup complete. LG-Net model loaded: zind")


def _store_mesh_or_raise(request: Request, mesh_path, name: str):
    try:
        storage = get_storage_backend()
        stored = storage.save_model(
            Path(mesh_path),
            public_name=Path(mesh_path).name,
            base_url=str(request.base_url),
        )
    except StorageConfigurationError as exc:
        raise HTTPException(
            status_code=500,
            detail=f"Inference completed but storage is not configured: {exc}",
        ) from exc
    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail=f"Inference completed but storage upload failed: {exc}",
        ) from exc

    if stored.should_cleanup_local and not should_retain_local_outputs():
        _cleanup_local_outputs(name)

    return stored.url


def _cleanup_local_outputs(name: str):
    for path in STATIC_DIR.glob(f"{name}*"):
        if not path.is_file():
            continue
        try:
            path.unlink()
        except OSError as exc:
            logger.warning(f"Could not remove local output {path}: {exc}")


@app.get("/viewer")
def viewer():
    viewer_path = Path(__file__).with_name("viewer.html")
    if not viewer_path.exists():
        raise HTTPException(status_code=404, detail="viewer html not found")
    return FileResponse(viewer_path)


@app.get("/api/meshes")
def list_meshes():
    valid_ext = {".glb", ".gltf", ".obj", ".ply"}
    meshes = []
    for path in STATIC_DIR.iterdir():
        if not path.is_file() or path.suffix.lower() not in valid_ext:
            continue
        stat = path.stat()
        meshes.append(
            {
                "name": path.name,
                "size": stat.st_size,
                "mtime": int(stat.st_mtime),
                "url": f"/mesh/{quote(path.name)}",
            }
        )
    meshes.sort(key=lambda item: item["mtime"], reverse=True)
    return {"mesh_dir": str(STATIC_DIR), "meshes": meshes}


@app.get("/mesh/{name:path}")
def get_mesh(name: str):
    target = (STATIC_DIR / name).resolve()
    root = STATIC_DIR.resolve()
    if root != target and root not in target.parents:
        raise HTTPException(status_code=400, detail="invalid mesh path")
    if not target.is_file():
        raise HTTPException(status_code=404, detail="mesh not found")
    return FileResponse(target)


@app.get("/api/run_status")
def run_status():
    return {"state": "idle", "message": "FastAPI viewer is listing outputs/api"}


@app.post("/api/run_inference")
def run_inference_from_viewer():
    return JSONResponse(
        status_code=409,
        content={
            "ok": False,
            "message": "Use POST /predict with file, image_type, and mesh_quality/mesh_resolution.",
        },
    )


@app.post("/predict")
def predict(
    request: Request,
    file: UploadFile = File(...),
    image_type: str = Form("360"),
    mesh_resolution: int = Form(256),
    mesh_quality: str = Form("medium"),
):
    image_type = (image_type or "360").lower()
    if image_type not in {"360", "normal"}:
        raise HTTPException(status_code=400, detail="image_type must be one of: 360, normal")

    file_id = uuid.uuid4().hex
    stem = Path(file.filename or "input").stem
    name = f"{stem}_{file_id}"
    image_path = STATIC_DIR / f"{name}_input.png"

    try:
        raw = file.file.read()
        with open(image_path, "wb") as f:
            f.write(raw)
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"Cannot save image file: {exc}") from exc

    if image_type == "normal":
        mesh_quality = (mesh_quality or "medium").lower()
        if mesh_quality not in {"low", "medium", "high"}:
            raise HTTPException(status_code=400, detail="mesh_quality must be one of: low, medium, high")

        try:
            normal = get_noncuboid_model()
            from pipelines.noncuboid.inference import run_one_image

            mesh_path, _output_type = run_one_image(
                image_path=image_path,
                model=normal["model"],
                cfg=normal["cfg"],
                device=normal["device"],
                name=name,
                output_dir=STATIC_DIR,
                mesh_quality=mesh_quality,
            )
        except Exception as exc:
            raise HTTPException(status_code=500, detail=f"NonCuboid inference failed: {exc}") from exc

        return {
            "model_url": _store_mesh_or_raise(request, mesh_path, name),
        }

    allowed_resolutions = {128, 256, 512, 1024}
    if mesh_resolution not in allowed_resolutions:
        raise HTTPException(status_code=400, detail="mesh_resolution must be one of: 128, 256, 512, 1024")

    try:
        img = np.array(Image.open(image_path).resize((1024, 512), Image.Resampling.BICUBIC))[..., :3]
    except Exception as exc:
        raise HTTPException(status_code=400, detail=f"Cannot read image file: {exc}") from exc

    vp_cache_path = str(STATIC_DIR / f"{name}_vp.txt")
    img, _ = preprocess(img, vp_cache_path=vp_cache_path)
    img = (img / 255.0).astype(np.float32)

    args = Namespace(
        device="cuda" if torch.cuda.is_available() else "cpu",
        output_dir=str(STATIC_DIR),
        visualize_3d=False,
        output_3d=True,
        post_processing="manhattan",
    )

    try:
        mesh_path = run_one_inference(
            img,
            MODELS["zind"],
            args,
            name,
            logger=logger,
            mesh_format=".glb",
            mesh_resolution=int(mesh_resolution),
        )
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"LG-Net inference failed: {exc}") from exc

    if mesh_path is None:
        raise HTTPException(status_code=500, detail="Mesh export failed")

    return {
        "model_url": _store_mesh_or_raise(request, mesh_path, name),
    }

@app.get("/health")
def health():
    return {"status": "UP"}
