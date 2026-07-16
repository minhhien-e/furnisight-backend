FROM python:3.10-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ARG TORCH_WHEEL=""
ENV PORT=8080

WORKDIR /app

# Install minimal system deps needed by some packages (opencv, open3d may require libs)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential git libgl1 libglib2.0-0 && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt ./

# Install requirements
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Pre-download TorchVision ResNet50 weights to avoid runtime download and DNS issues
RUN python -c "from torchvision.models import resnet50, ResNet50_Weights; resnet50(weights=ResNet50_Weights.IMAGENET1K_V1)"

COPY app /app/app
COPY pipelines /app/pipelines
COPY fastapi_app.py inference.py convert_ckpt.py /app/
COPY ./checkpoints /app/checkpoints

EXPOSE 8080

CMD ["uvicorn", "fastapi_app:app", "--host", "0.0.0.0", "--port", "8080"]
