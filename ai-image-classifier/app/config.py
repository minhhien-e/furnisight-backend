import os
import json
from pydantic_settings import BaseSettings

# Absolute path root
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CONFIG_PATH = os.path.join(BASE_DIR, "config.json")

def load_json_config():
    if os.path.exists(CONFIG_PATH):
        with open(CONFIG_PATH, "r", encoding="utf-8") as file:
            return json.load(file)
    return {}

_config = load_json_config()

class Settings(BaseSettings):
    PROJECT_NAME: str = _config.get("project_name", "AI Image Classification Microservice")
    VERSION: str = _config.get("version", "1.0.0")
    
    HOST: str = _config.get("server", {}).get("host", "0.0.0.0")
    PORT: int = _config.get("server", {}).get("port", 8000)
    
    # Model configuration (Classifier Normal)
    NORMAL_MODEL_NAME: str = _config.get("classifier_normal", {}).get("model_name", "resnet50")
    IMAGE_SIZE: int = _config.get("classifier_normal", {}).get("image_size", 224)
    NUM_CLASSES: int = _config.get("classifier_normal", {}).get("num_classes", 4)
    _normal_model_relative: str = _config.get("classifier_normal", {}).get("model_path", "models/resnet50_normal_best.pth")
    NORMAL_MODEL_PATH: str = os.path.join(BASE_DIR, _normal_model_relative)
    NORMAL_CLASS_NAMES: list = _config.get("classifier_normal", {}).get("class_names", [])

    # Model configuration (Classifier 360)
    MODEL_360_NAME: str = _config.get("classifier_360", {}).get("model_name", "resnet50")
    NUM_CLASSES_360: int = _config.get("classifier_360", {}).get("num_classes", 4)
    _model_360_relative: str = _config.get("classifier_360", {}).get("model_path", "models/resnet50_360_best.pth")
    MODEL_360_PATH: str = os.path.join(BASE_DIR, _model_360_relative)
    MODEL_360_CLASS_NAMES: list = _config.get("classifier_360", {}).get("class_names", [])
    
    # Model configuration (Object Remover)
    _yolo_relative = _config.get("object_remover", {}).get("yolo_model_path", "models/yolov8s-seg.pt")
    YOLO_MODEL_PATH: str = os.path.join(BASE_DIR, _yolo_relative)
    _lama_relative = _config.get("object_remover", {}).get("lama_model_path", "models/big-lama.pt")
    LAMA_MODEL_PATH: str = os.path.join(BASE_DIR, _lama_relative)
    REMOVER_CONFIDENCE: float = _config.get("object_remover", {}).get("confidence", 0.35)
    REMOVER_MAX_IMAGE_SIZE: int = _config.get("object_remover", {}).get("max_image_size", 1024)
    REMOVE_CLASSES: set = set(_config.get("object_remover", {}).get("remove_classes", [0, 56, 57, 58, 59, 60, 62, 63, 66, 67, 72, 73, 74, 75]))

    # Recommendation configuration
    _recommendation_config: dict = _config.get("recommendation", {})
    CATALOG_SERVICE_GRPC_ADDRESS: str = _recommendation_config.get("catalog_service_grpc_address", "dns:///catalog-service:9093")
    RECOMMENDATION_LIMIT: int = _recommendation_config.get("recommendation_limit", 6)
    RECOMMENDATION_TIMEOUT_SECONDS: float = _recommendation_config.get("recommendation_timeout_seconds", 3)
    CATEGORY_MAPPING: dict = _recommendation_config.get("category_mapping", {
        "livingroom": "living-room",
        "bedroom": "bedroom",
        "kitchen": "kitchen",
        "bathroom": "bathroom",
    })
    
    class Config:
        env_file = ".env"

settings = Settings()
