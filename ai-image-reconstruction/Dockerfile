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

COPY . /app

EXPOSE 8080

CMD ["uvicorn", "fastapi_app:app", "--host", "0.0.0.0", "--port", "8080"]
