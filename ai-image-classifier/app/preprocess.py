from PIL import Image
import torchvision.transforms as transforms
import torch
from .config import settings
from .model import model_gateway

def preprocess_image(image: Image.Image) -> torch.Tensor:
    """
    Prepares a PIL image for the classification model.
    Includes YOLOv8 + LaMa object removal, resizing, center cropping, 
    converting to tensor, and normalization using ImageNet standards.
    """
    
    # Bươc 1: Tiền xử lý xoá vật thể (Object Removal)
    remover = model_gateway.get_object_remover()
    cleaned_image = remover(image)
    
    # Bước 2: ResNet50 Transforms Standard
    if cleaned_image.mode != "RGB":
        cleaned_image = cleaned_image.convert("RGB")
        
    preprocess_transform = transforms.Compose([
        transforms.Resize(256),
        transforms.CenterCrop(settings.IMAGE_SIZE),
        transforms.ToTensor(),
        transforms.Normalize(
            mean=[0.485, 0.456, 0.406],
            std=[0.229, 0.224, 0.225]
        )
    ])
    
    input_tensor = preprocess_transform(cleaned_image)
    input_batch = input_tensor.unsqueeze(0)
    
    return input_batch
