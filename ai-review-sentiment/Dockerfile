FROM python:3.11-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

RUN apt-get update \
    && apt-get install -y --no-install-recommends gcc \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --default-timeout=1000 --no-cache-dir -r requirements.txt

COPY ./app /app/app
COPY ./v1 /app/v1

EXPOSE 8080
EXPOSE 50051

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]
