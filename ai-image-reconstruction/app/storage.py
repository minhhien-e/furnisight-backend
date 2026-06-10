import os
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Protocol
from urllib.parse import quote


@dataclass(frozen=True)
class StoredModel:
    url: str
    should_cleanup_local: bool


class StorageBackend(Protocol):
    def save_model(self, model_path: Path, public_name: str, base_url: str) -> StoredModel:
        ...


class StorageConfigurationError(RuntimeError):
    pass


class LocalStorageBackend:
    def save_model(self, model_path: Path, public_name: str, base_url: str) -> StoredModel:
        model_name = Path(model_path).name
        url = base_url.rstrip("/") + f"/static/{quote(model_name)}"
        return StoredModel(url=url, should_cleanup_local=False)


class CloudinaryStorageBackend:
    def __init__(self, folder: str | None = None):
        self.folder = (folder or "").strip().strip("/")

    def save_model(self, model_path: Path, public_name: str, base_url: str) -> StoredModel:
        cloud_name = _required_env("CLOUDINARY_CLOUD_NAME")
        api_key = _required_env("CLOUDINARY_API_KEY")
        api_secret = _required_env("CLOUDINARY_API_SECRET")

        try:
            import cloudinary
            import cloudinary.uploader
        except ImportError as exc:
            raise StorageConfigurationError(
                "Cloudinary storage requires the 'cloudinary' Python package."
            ) from exc

        cloudinary.config(
            cloud_name=cloud_name,
            api_key=api_key,
            api_secret=api_secret,
            secure=True,
        )

        model_path = Path(model_path)
        public_id = _safe_public_id(public_name or model_path.name)
        options = {
            "resource_type": "raw",
            "public_id": public_id,
            "overwrite": True,
            "unique_filename": False,
        }
        if self.folder:
            options["folder"] = self.folder

        result = cloudinary.uploader.upload(str(model_path), **options)
        url = result.get("secure_url") or result.get("url")
        if not url:
            raise RuntimeError("Cloudinary upload did not return a URL.")

        return StoredModel(url=url, should_cleanup_local=True)


def get_storage_backend() -> StorageBackend:
    backend = os.getenv("STORAGE_BACKEND", "cloudinary").strip().lower()
    if backend == "local":
        return LocalStorageBackend()
    if backend == "cloudinary":
        return CloudinaryStorageBackend(folder=os.getenv("CLOUDINARY_FOLDER"))
    raise StorageConfigurationError(
        "Unsupported STORAGE_BACKEND. Expected one of: cloudinary, local."
    )


def should_retain_local_outputs() -> bool:
    return os.getenv("RETAIN_LOCAL_OUTPUTS", "false").strip().lower() in {
        "1",
        "true",
        "yes",
        "on",
    }


def _required_env(name: str) -> str:
    value = os.getenv(name)
    if value is None or not value.strip():
        raise StorageConfigurationError(f"Missing required environment variable: {name}")
    return value.strip()


def _safe_public_id(name: str) -> str:
    path = Path(name)
    stem = re.sub(r"[^A-Za-z0-9_-]+", "-", path.stem).strip("-")
    if not stem:
        stem = "reconstruction"
    suffix = re.sub(r"[^A-Za-z0-9.]+", "", path.suffix)
    return f"{stem}{suffix}"
