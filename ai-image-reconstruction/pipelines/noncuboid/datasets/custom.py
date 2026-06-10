import os

import cv2
import numpy as np
import torchvision.transforms as tf
from PIL import Image, ExifTags
from torch.utils import data


class CustomDataset(data.Dataset):
    def __init__(self, config, phase='test', files='example', use_first_image=True):
        self.config = config
        self.phase = phase
        self.max_objs = config.max_objs
        self.transforms = tf.Compose([
            tf.ToTensor(),
            tf.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
        ])
        self.K = np.array([[762, 0, 640], [0, -762, 360], [0, 0, 1]],
                          dtype=np.float32)
        self.K_inv = np.linalg.inv(self.K).astype(np.float32)
        # Build EXIF tag map once for robust metadata lookup.
        self._exif_tag_to_id = {name: tag_id for tag_id, name in ExifTags.TAGS.items()}

        self.files = files
        valid_ext = ('.jpg', '.jpeg', '.png', '.bmp', '.webp')
        filenames = sorted([
            name for name in os.listdir(files)
            if os.path.isfile(os.path.join(files, name)) and name.lower().endswith(valid_ext)
        ])
        if not filenames:
            raise FileNotFoundError(f'No image files found in "{files}"')

        self.filenames = filenames[:1] if use_first_image else filenames

    def _ratio_to_float(self, value):
        if value is None:
            return None
        if isinstance(value, (int, float)):
            return float(value)
        if isinstance(value, tuple) and len(value) == 2 and value[1] != 0:
            return float(value[0]) / float(value[1])
        # PIL can return IFDRational objects, float() handles them.
        try:
            return float(value)
        except Exception:
            return None

    def _estimate_intrinsics_from_exif(self, pil_img, target_w, target_h):
        # Fallback: keep old fixed intrinsics if EXIF is missing.
        fallback_K = self.K.copy()
        fallback_K_inv = self.K_inv.copy()

        try:
            exif = pil_img.getexif()
        except Exception:
            exif = None

        if not exif:
            return fallback_K, fallback_K_inv

        # Extract common EXIF fields for focal length estimation.
        focal_tag = self._exif_tag_to_id.get('FocalLength')
        focal_35_tag = self._exif_tag_to_id.get('FocalLengthIn35mmFilm')
        focal_mm = self._ratio_to_float(exif.get(focal_tag)) if focal_tag is not None else None
        focal_35 = self._ratio_to_float(exif.get(focal_35_tag)) if focal_35_tag is not None else None

        orig_w, orig_h = pil_img.size
        fx_orig = None

        # Prefer 35mm-equivalent focal if available (most stable across phones).
        if focal_35 and focal_35 > 0:
            fx_orig = orig_w * focal_35 / 36.0
        elif focal_mm and focal_mm > 0:
            # Approximate fallback sensor width (mm) when only physical focal exists.
            approx_sensor_width_mm = 6.4
            fx_orig = orig_w * focal_mm / approx_sensor_width_mm
        else:
            return fallback_K, fallback_K_inv

        # Scale intrinsics from original image size to model's resized input (1280x720).
        sx = target_w / float(orig_w)
        sy = target_h / float(orig_h)
        fx = fx_orig * sx
        fy = fx_orig * sy
        cx = target_w / 2.0
        cy = target_h / 2.0

        K = np.array([[fx, 0, cx], [0, -fy, cy], [0, 0, 1]], dtype=np.float32)
        K_inv = np.linalg.inv(K).astype(np.float32)
        return K, K_inv

    def padimage(self, image):
        outsize = [384, 640, 3]
        h, w = image.shape[0], image.shape[1]
        padimage = np.zeros(outsize, dtype=np.uint8)
        padimage[:h, :w] = image
        return padimage, outsize[0], outsize[1]

    def __getitem__(self, index):
        img_path = os.path.join(self.files, self.filenames[index])
        img = Image.open(img_path)
        # Compute camera intrinsics before resize using EXIF metadata when possible.
        K, K_inv = self._estimate_intrinsics_from_exif(img, target_w=1280, target_h=720)
        img = img.resize((1280, 720))
        inh, inw = self.config.input_h, self.config.input_w
        orih, oriw = img.size[1], img.size[0]
        ratio_w = oriw / inw
        ratio_h = orih / inh
        assert ratio_h == ratio_w == 2
        img = np.array(img)[:, :, [0, 1, 2]]
        img = cv2.resize(img, (inw, inh), interpolation=cv2.INTER_LINEAR)
        img, inh, inw = self.padimage(img)
        img = self.transforms(img)
        ret = {'img': img}
        ret['intri'] = K
        ret['intri_inv'] = K_inv

        oh, ow = inh // self.config.downsample, inw // self.config.downsample
        x = np.arange(ow * 8)
        y = np.arange(oh * 8)
        xx, yy = np.meshgrid(x, y)
        xymap = np.stack([xx, yy], axis=2).astype(np.float32)
        oxymap = cv2.resize(xymap, (ow, oh), interpolation=cv2.INTER_LINEAR)
        oxy1map = np.concatenate([
            oxymap, np.ones_like(oxymap[:, :, :1])], axis=-1).astype(np.float32)
        ret['oxy1map'] = oxy1map

        ixymap = cv2.resize(xymap, (inw, inh), interpolation=cv2.INTER_LINEAR)
        ixy1map = np.concatenate([
            ixymap, np.ones_like(ixymap[:, :, :1])], axis=-1).astype(np.float32)
        ret['ixy1map'] = ixy1map
        ret['iseg'] = np.ones([inh, inw])
        ret['ilbox'] = np.zeros(20)
        return ret

    def __len__(self):
        return len(self.filenames)


