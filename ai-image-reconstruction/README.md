# LGT-Net Reconstruction API

PyTorch inference API for:

- LG-Net panorama 360 room layout reconstruction.
- NonCuboid normal 2D room image reconstruction.

This fork is focused on FastAPI inference. Training, dataset preparation, and old demo UI flows are not part of the main runtime path.

## Install

```shell
pip install -r requirements.txt
```

Install PyTorch with CUDA separately from https://pytorch.org/ when GPU inference is needed.

## Checkpoints

LG-Net checkpoints:

```text
checkpoints/SWG_Transformer_LGT_Net/<mp3d|zind|s2d3d|pano>/best.pkl
```

The FastAPI 360 route currently uses:

```text
checkpoints/SWG_Transformer_LGT_Net/zind/best.pkl
```

NonCuboid checkpoint:

```text
checkpoints/Structured3D/best.pt
```

The normal-image route currently uses this Structured3D checkpoint.

## Run FastAPI

```shell
uvicorn fastapi_app:app --host 0.0.0.0 --port 8000
```

Equivalent package entrypoint:

```shell
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## API

`POST /predict` accepts `multipart/form-data` and returns `model_url` for a `.glb` mesh.

### Panorama 360

```shell
curl -X POST http://localhost:8000/predict \
  -F "file=@/path/to/panorama.jpg" \
  -F "image_type=360" \
  -F "mesh_resolution=256"
```

Accepted `mesh_resolution` values:

```text
128, 256, 512, 1024
```

### Normal 2D Image

```shell
curl -X POST http://localhost:8000/predict \
  -F "file=@/path/to/image.jpg" \
  -F "image_type=normal" \
  -F "mesh_quality=medium"
```

Accepted `mesh_quality` values:

```text
low, medium, high
```

Mapping:

```text
low    -> stride 4
medium -> stride 2
high   -> stride 1
```

## CLI LG-Net Inference

```shell
python inference.py --cfg pipelines/lgnet/configs/zind.yaml --img_glob "path/to/your_pano.png" --output_dir outputs/lgnet --post_processing manhattan --output_3d
```

## Output

API outputs are saved under:

```text
outputs/api
```

The returned `model_url` points to:

```text
/static/<generated_mesh>.glb
```

For normal 2D images, the API also writes the NonCuboid overlay image:

```text
outputs/api/<name>_normal_select.png
```

## Project Layout

```text
app/                    FastAPI app and viewer
pipelines/lgnet/         LG-Net panorama 360 pipeline
pipelines/noncuboid/     NonCuboid normal-image pipeline
checkpoints/             model checkpoints
outputs/api/             runtime API outputs
```

## Upstream

- LG-Net: https://github.com/zhigangjiang/LGT-Net
- LGT-Net paper: https://arxiv.org/abs/2203.01824
