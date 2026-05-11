# FurniSight Backend

Đây là repository chứa mã nguồn Backend cho dự án **FurniSight** - một nền tảng web dành cho thiết kế và trang trí nội thất.

## Công nghệ sử dụng

Dự án này sử dụng kiến trúc kết hợp giữa hai framework mạnh mẽ để tối ưu hóa hiệu năng và khả năng mở rộng:

- **Spring Boot (Java)**: Đảm nhận vai trò lõi xử lý logic nghiệp vụ chính, bao gồm các service:
  - `gateway`: API Gateway điều phối các request.
  - `user-service`: Quản lý tài khoản, định danh và hồ sơ người dùng.
  - `notification-service`: Xử lý thông báo đa kênh (Email, SMS, In-app).
- **FastAPI (Python)**: Đảm nhận các tác vụ chuyên sâu liên quan đến AI (trí tuệ nhân tạo) như:
  - `ai-image-classifier`: Xử lý và phân loại hình ảnh nội thất.

## Yêu cầu hệ thống

- **Java**: Phiên bản 17+
- **Python**: Phiên bản 3.9+
- **Môi trường**: Khuyến nghị sử dụng `.env` riêng biệt cho từng service.
