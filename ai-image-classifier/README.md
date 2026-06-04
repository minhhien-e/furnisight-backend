# 🏠 AI Image Classifier — Microservice Phân Loại Ảnh Phòng

Microservice độc lập có nhiệm vụ nhận ảnh đầu vào và phân loại phòng trong ảnh sử dụng các mô hình ResNet50 đã được huấn luyện lại (fine-tuned). Hỗ trợ cả ảnh thường lẫn ảnh panorama 360°.

---

## ✨ Tính năng

- **Phân loại ảnh thường** — sử dụng model `resnet50_normal_best.pth`
- **Phân loại ảnh panorama 360°** — sử dụng model `resnet50_360_best.pth`
- **Tiền xử lý thông minh** — tự động xóa người/đồ vật bằng YOLOv8 + LaMa trước khi phân loại
- **Gợi ý sản phẩm theo phòng** — gọi `catalog-service` để lấy sản phẩm phù hợp với danh mục dự đoán
- **Cấu hình linh hoạt** — toàn bộ tham số đọc từ `config.json`, không cần sửa code
- **Singleton ModelGateway** — models chỉ được load một lần duy nhất lúc khởi động
- **Pipeline pattern** — luồng xử lý rõ ràng, dễ mở rộng
- **Docker-ready** — đóng gói và triển khai đơn giản

---

## 🏷️ Các class phân loại

| Index | Nhãn |
|-------|------|
| 0 | `bathroom` |
| 1 | `bedroom` |
| 2 | `kitchen` |
| 3 | `livingroom` |

---

## 📁 Cấu trúc thư mục

```
ai-image-classifier/
├── app/
│   ├── main.py           # FastAPI app, routes
│   ├── config.py         # Settings đọc từ config.json
│   ├── model.py          # ModelGateway — load & quản lý tất cả models
│   ├── service.py        # PredictionService — pipeline xử lý
│   ├── preprocess.py     # Tiền xử lý ảnh (resize, normalize, ...)
│   ├── postprocess.py    # Hậu xử lý (softmax, map label)
│   ├── object_remover.py # YOLOv8 + LaMa (xóa đồ vật)
│   └── utils.py          # Tiện ích (get class name từ config)
├── models/
│   ├── resnet50_normal_best.pth   # Weights model ảnh thường
│   ├── resnet50_360_best.pth      # Weights model ảnh 360°
│   └── yolov8s-seg.pt             # Weights YOLO segmentation
├── config.json           # Cấu hình toàn bộ service
├── requirements.txt
├── Dockerfile
└── postman_collection.json
```

---

## ⚙️ Cấu hình (`config.json`)

```json
{
  "classifier_normal": {
    "model_name": "resnet50",
    "image_size": 224,
    "num_classes": 4,
    "model_path": "models/resnet50_normal_best.pth",
    "class_names": ["bathroom", "bedroom", "kitchen", "livingroom"]
  },
  "classifier_360": {
    "model_name": "resnet50",
    "image_size": 224,
    "num_classes": 4,
    "model_path": "models/resnet50_360_best.pth",
    "class_names": ["bathroom", "bedroom", "kitchen", "livingroom"]
  },
  "recommendation": {
    "catalog_base_url": "http://catalog-service:8080/api/v1",
    "recommendation_limit": 6,
    "recommendation_timeout_seconds": 3,
    "category_mapping": {
      "livingroom": "living-room",
      "bedroom": "bedroom",
      "kitchen": "kitchen",
      "bathroom": "bathroom"
    }
  }
}
```

---

## 🚀 Chạy local

### Yêu cầu
- Python 3.10+
- Các file model đã được đặt trong thư mục `models/`

```bash
# 1. Tạo và kích hoạt virtual environment
python -m venv venv
source venv/bin/activate   # Linux/macOS
# venv\Scripts\activate    # Windows

# 2. Cài đặt dependencies
pip install -r requirements.txt

# 3. Khởi động server
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Truy cập **http://localhost:8000/docs** để dùng Swagger UI tương tác.

---

## 🐳 Chạy với Docker

```bash
# 1. Build image
docker build -t ai-image-classifier .

# 2. Chạy container
docker run -p 8000:8000 ai-image-classifier
```

---

## 📡 API Endpoints

### `GET /health`
Kiểm tra service đang hoạt động và model đã được load.

```bash
curl http://localhost:8000/health
```

**Response:**
```json
{ "status": "ok" }
```

---

### `POST /predict`
Phân loại ảnh. Nhận file ảnh và trả về nhãn + độ tin cậy.

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| `file` | `file` | ✅ | File ảnh (jpg, png, webp, ...) |
| `image_type` | `string` | ❌ | `"normal"` (mặc định) hoặc `"360"` |

```bash
# Ảnh thường
curl -X POST http://localhost:8000/predict \
  -F "file=@/path/to/image.jpg" \
  -F "image_type=normal"

# Ảnh panorama 360°
curl -X POST http://localhost:8000/predict \
  -F "file=@/path/to/panorama.jpg" \
  -F "image_type=360"
```

**Response thành công (200):**
```json
{
  "label": "bedroom",
  "confidence": 0.9231,
  "recommendations": [
    {
      "id": "e0000000-0000-0000-0000-000000000004",
      "slug": "king-size-metal-bed",
      "name": "King Size Metal Bed",
      "categoryName": "Bed",
      "price": 9500000,
      "oldPrice": null,
      "image": "https://example.com/bed.jpg",
      "rating": 0,
      "ratingCount": 0,
      "soldCount": 0,
      "tags": []
    }
  ],
  "recommendationMeta": {
    "categorySlug": "bedroom",
    "source": "catalog-service",
    "reason": null
  }
}
```

Nếu không có danh mục hoặc sản phẩm phù hợp, `recommendations` là mảng rỗng và
`recommendationMeta.reason` có thể là `category_not_mapped`, `no_products_found`
hoặc `catalog_unavailable`.

**Response lỗi:**
| HTTP Code | Nguyên nhân |
|-----------|-------------|
| `400` | File không phải ảnh hoặc ảnh bị lỗi |
| `500` | Lỗi trong quá trình inference |
| `503` | Model chưa được load |

---

## 🧪 Test với Postman

Import file `postman_collection.json` vào Postman. Collection đã có sẵn 3 request mẫu cho cả 3 endpoint.

---

## 🛠️ Dependencies chính

| Thư viện | Mục đích |
|----------|---------|
| `fastapi` + `uvicorn` | Web framework & server |
| `torch` + `torchvision` | Deep learning inference |
| `ultralytics` | YOLOv8 object detection/segmentation |
| `simple-lama-inpainting` | LaMa inpainting (xóa đồ vật) |
| `Pillow` + `opencv-python-headless` | Xử lý ảnh |
| `pydantic-settings` | Quản lý cấu hình |
