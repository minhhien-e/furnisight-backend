import torch
from .utils import get_class_name
from typing import Dict, Any

def postprocess_output(output: torch.Tensor, image_type: str = "normal") -> Dict[str, Any]:
    """
    Applies Softmax to get probabilities and maps the highest confidence index
    to the human-readable label.
    """
    
    # Calculate probabilities across classes
    probabilities = torch.nn.functional.softmax(output[0], dim=0)
    
    # Get the class index with highest probability
    confidence, class_idx = torch.max(probabilities, 0)
    
    confidence_value = confidence.item()
    label = get_class_name(class_idx.item(), image_type)
    
    return {
        "label": label,
        "confidence": confidence_value
    }
