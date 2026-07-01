from __future__ import annotations

import logging

from fastapi import FastAPI

from app.grpc_server import start_grpc_server

logging.basicConfig(level=logging.INFO)

app = FastAPI(title="AI Review Sentiment", version="1.0.0")
grpc_server = start_grpc_server()


@app.get("/health")
def health():
    return {"status": "ok"}
