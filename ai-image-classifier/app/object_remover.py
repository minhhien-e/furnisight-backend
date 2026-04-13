# ==========================================================
# Room Object Removal Pipeline
# YOLOv8 Segmentation + LaMa Inpainting
# Optimized for 4GB VRAM GPU
# ==========================================================

import cv2
import torch
import numpy as np
from PIL import Image

from ultralytics import YOLO
from simple_lama_inpainting.utils import prepare_img_and_mask, download_model
from .config import settings


class ObjectRemover:

    def __init__(
        self,
        yolo_model=None,
        confidence=None,
        device="auto",
        max_image_size=None
    ):
        yolo_model = yolo_model or settings.YOLO_MODEL_PATH
        confidence = confidence or settings.REMOVER_CONFIDENCE
        max_image_size = max_image_size or settings.REMOVER_MAX_IMAGE_SIZE

        # -------------------------------------------------
        # Device
        # -------------------------------------------------

        if device == "auto":
            device = "cuda" if torch.cuda.is_available() else "cpu"

        self.device = device
        self.conf = confidence
        self.max_image_size = max_image_size

        # -------------------------------------------------
        # Load YOLO
        # -------------------------------------------------

        self.model = YOLO(yolo_model)

        # Classes cần xóa
        self.remove_classes = settings.REMOVE_CLASSES

        # -------------------------------------------------
        # Load LaMa
        # -------------------------------------------------

        lama_url = settings.LAMA_MODEL_URL
        model_path = download_model(lama_url)

        self.lama = torch.jit.load(model_path, map_location=device)
        self.lama.eval()

    # -------------------------------------------------
    # Resize image (save VRAM)
    # -------------------------------------------------

    def resize_if_needed(self, image):

        h, w = image.shape[:2]

        if max(h, w) <= self.max_image_size:
            return image, 1.0

        scale = self.max_image_size / max(h, w)

        new_w = int(w * scale)
        new_h = int(h * scale)

        resized = cv2.resize(image, (new_w, new_h))

        return resized, scale

    # -------------------------------------------------
    # Detect objects and build mask
    # -------------------------------------------------

    def build_mask(self, image):

        results = self.model.predict(
            image,
            conf=self.conf,
            device=self.device,
            verbose=False
        )

        h, w = image.shape[:2]

        final_mask = np.zeros((h, w), dtype=np.uint8)

        r = results[0]

        if r.masks is None:
            return final_mask

        masks = r.masks.data.cpu().numpy()
        classes = r.boxes.cls.cpu().numpy()

        img_area = h * w

        for mask, cls in zip(masks, classes):

            cls = int(cls)

            if cls not in self.remove_classes:
                continue

            mask = cv2.resize(mask, (w, h))

            mask = (mask > 0.5).astype(np.uint8)

            area = mask.sum()
            area_ratio = area / img_area

            # bỏ object quá nhỏ
            if area_ratio < 0.002:
                continue

            mask = mask * 255

            final_mask = np.maximum(final_mask, mask)

        return final_mask

    # -------------------------------------------------
    # Clean mask
    # -------------------------------------------------

    def refine_mask(self, mask):

        kernel = np.ones((15, 15), np.uint8)

        mask = cv2.dilate(mask, kernel, iterations=1)
        mask = cv2.GaussianBlur(mask, (15, 15), 0)

        _, mask = cv2.threshold(mask, 127, 255, cv2.THRESH_BINARY)

        return mask

    # -------------------------------------------------
    # Inpainting
    # -------------------------------------------------

    def inpaint(self, image, mask):

        pil_image = Image.fromarray(image)
        pil_mask = Image.fromarray(mask)

        image_t, mask_t = prepare_img_and_mask(
            pil_image,
            pil_mask,
            self.device
        )

        with torch.inference_mode():

            result = self.lama(image_t, mask_t)
            result = result[0].permute(1, 2, 0).cpu().numpy()

        result = np.clip(result * 255, 0, 255).astype(np.uint8)

        return result

    # -------------------------------------------------
    # Main pipeline
    # -------------------------------------------------

    def __call__(self, pil_image: Image.Image) -> Image.Image:

        if pil_image.mode != "RGB":
            pil_image = pil_image.convert("RGB")
            
        image = np.array(pil_image)

        original_h, original_w = image.shape[:2]

        # resize
        resized_image, scale = self.resize_if_needed(image)

        # detect objects
        mask = self.build_mask(resized_image)

        if mask.max() == 0:
            return pil_image

        mask = self.refine_mask(mask)

        # inpaint
        result = self.inpaint(resized_image, mask)

        # resize back
        if scale != 1.0:
            result = cv2.resize(result, (original_w, original_h))

        return Image.fromarray(result)
