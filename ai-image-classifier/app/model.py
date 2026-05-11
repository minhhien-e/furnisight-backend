import torch
import torch.nn as nn
import torchvision.models as models
from .config import settings

import os
from .object_remover import ObjectRemover


def _build_resnet50(num_classes: int, dropout: float = 0.5) -> nn.Module:
    """Tái tạo đúng kiến trúc ResNet50 với fc head tùy chỉnh (khớp với lúc training)."""
    backbone = models.resnet50(weights=None)  # Không load pretrained weights
    num_ftrs = backbone.fc.in_features
    backbone.fc = nn.Sequential(
        nn.Dropout(p=dropout),
        nn.Linear(num_ftrs, num_classes)
    )
    return backbone


class ModelGateway:
    """
    Model Gateway / Portal
    Sử dụng Singleton pattern để quản lý việc tải và truy xuất các mô hình AI tập trung.
    Đảm bảo mô hình chỉ được tải vào memory/VRAM một lần duy nhất lúc khởi động.
    """
    _instance = None
    
    def __init__(self):
        self.classifier_normal = None
        self.classifier_360 = None
        self.object_remover = None
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        
    @classmethod
    def get_instance(cls):
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance
        
    def _load_classifier(self, model_name: str, num_classes: int, model_path: str) -> nn.Module:
        """
        Dựng lại kiến trúc model và load state_dict từ file .pth local.
        Tự động xử lý trường hợp state_dict được lưu từ wrapper class
        (BaseModernBackbone) với prefix 'backbone.' trong tên key.
        """
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"Model file not found: {model_path}")

        model = _build_resnet50(num_classes)

        state_dict = torch.load(model_path, map_location=self.device, weights_only=True)

        # Nếu model được save từ BaseModernBackbone wrapper, key sẽ có dạng:
        #   backbone.conv1.weight, backbone.fc.0.weight, ...
        # Cần strip prefix 'backbone.' để khớp với raw ResNet50 architecture.
        first_key = next(iter(state_dict))
        if first_key.startswith("backbone."):
            state_dict = {k.removeprefix("backbone."): v for k, v in state_dict.items()}

        model.load_state_dict(state_dict)
        model.to(self.device).eval()
        return model

    def load_all(self):
        """
        Tải tất cả các mô hình cần thiết. Chạy duy nhất một lần khi service startup.
        """
        
        # 1. Load Normal Classification Model từ file .pth local
        self.classifier_normal = self._load_classifier(
            model_name=settings.NORMAL_MODEL_NAME,
            num_classes=settings.NUM_CLASSES,
            model_path=settings.NORMAL_MODEL_PATH,
        )
        
        # 2. Load 360 Classification Model từ file .pth local
        self.classifier_360 = self._load_classifier(
            model_name=settings.MODEL_360_NAME,
            num_classes=settings.NUM_CLASSES_360,
            model_path=settings.MODEL_360_PATH,
        )
        
        # 3. Load Object Remover Model (YOLO + LaMa)
        self.object_remover = ObjectRemover(yolo_model=settings.YOLO_MODEL_PATH, device=self.device)
        
    def get_classifier(self, image_type: str = "normal"):
        if image_type == "360":
            if self.classifier_360 is None:
                raise RuntimeError("360 Classification model is not loaded.")
            return self.classifier_360
        else:
            if self.classifier_normal is None:
                raise RuntimeError("Normal Classification model is not loaded.")
            return self.classifier_normal
        
    def get_object_remover(self) -> ObjectRemover:
        if self.object_remover is None:
            raise RuntimeError("Object Remover model is not loaded.")
        return self.object_remover

    def predict_classification(self, input_tensor: torch.Tensor, image_type: str = "normal") -> torch.Tensor:
        """
        Dự đoán với tensor đầu vào (Classification).
        """
        classifier = self.get_classifier(image_type)
        input_tensor = input_tensor.to(self.device)
        
        with torch.no_grad():
            output = classifier(input_tensor)
            
        return output

# Global portal instance
model_gateway = ModelGateway.get_instance()
