import io
from PIL import Image, UnidentifiedImageError
from .preprocess import preprocess_image
from .postprocess import postprocess_output
from .model import model_gateway


class PredictionService:
    """
    Service trung gian điều phối toàn bộ luồng xử lý dự đoán ảnh.
    Nhận dữ liệu thô từ Controller/Router, xử lý logic nghiệp vụ và trả về kết quả thuần.
    """

    @staticmethod
    def _parse_image(contents: bytes) -> Image.Image:
        try:
            return Image.open(io.BytesIO(contents))
        except UnidentifiedImageError:
            raise ValueError("Invalid image file format.")

    @staticmethod
    def process_image(contents: bytes, image_type: str = "normal") -> dict:
        """
        Pipeline xử lý ảnh tuần tự:
          1. Parse bytes → PIL Image
          2. Preprocess → tensor
          3. Inference → output tensor
          4. Postprocess → label + confidence
        """
        image = PredictionService._parse_image(contents)

        pipeline = [
            (lambda img: preprocess_image(img), "Preprocess failed"),
            (lambda tensor: model_gateway.predict_classification(tensor, image_type), "Inference failed"),
            (lambda output: postprocess_output(output, image_type), "Postprocess failed"),
        ]

        data = image
        for step, error_msg in pipeline:
            try:
                data = step(data)
            except Exception as e:
                raise RuntimeError(f"{error_msg}: {e}") from e

        return data


# Cung cấp singleton pattern instance để router import
prediction_service = PredictionService()
