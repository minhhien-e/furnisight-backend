FROM python:3.8-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ARG TORCH_WHEEL=""
ENV PORT=8001

WORKDIR /app

# Install minimal system deps needed by some packages (opencv, open3d may require libs)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential git libgl1 libglib2.0-0 && \
    rm -rf /var/lib/apt/lists/*

COPY requirements.txt ./

# Install torch wheel first if provided, then other requirements
RUN pip install --upgrade pip && \
    if [ -n "$TORCH_WHEEL" ]; then pip install $TORCH_WHEEL; fi && \
    pip install -r requirements.txt

COPY . /app

EXPOSE 8001

CMD ["uvicorn", "fastapi_app:app", "--host", "0.0.0.0", "--port", "8001"]
