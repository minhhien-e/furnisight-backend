from .config import settings


def get_class_name(index: int, image_type: str = "normal") -> str:
    """
    Map class index sang tên label dựa trên class_names được cấu hình trong config.json.
    """
    if image_type == "360":
        class_names = settings.MODEL_360_CLASS_NAMES
    else:
        class_names = settings.NORMAL_CLASS_NAMES

    if class_names and 0 <= index < len(class_names):
        return class_names[index]
    return f"class_{index}"
