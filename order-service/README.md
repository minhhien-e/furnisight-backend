# Order Service

Service này được xây dựng theo kiến trúc **Multi-Module**, chịu trách nhiệm xử lý chuyển phát tin nhắn đa kênh (SMS/Email/Push) một cách tin cậy và minh bạch.

## Kiến Trúc (Architecture)

Order Service áp dụng kiến trúc **Hexagonal Architecture (Ports and Adapters)** và được chia thành 4 module chính:

### Cây Thư Mục (Directory Structure)

```text
order-service/
├── build.gradle                   # File cấu hình build chung cho toàn project
├── settings.gradle                # File khai báo các module con (domain, application, adapter, bootstrap)
├── order-domain/           # [CORE] Cốt lõi nghiệp vụ
│   ├── src/main/java/com/furnisight/order/domain/
│   │   ├── entity/                # Entity nghiệp vụ lõi (vd: Order)
│   │   ├── exception/             # Lỗi Domain
│   │   └── value/                 # Value Objects
│   └── build.gradle
├── order-application/      # [USE CASES] Điều phối các thao tác
│   ├── src/main/java/com/furnisight/order/application/
│   │   ├── exception/             # Lỗi Business Logic
│   │   ├── port/in/               # Interface cho API / Controller gọi vào
│   │   ├── port/out/              # Interface cho Repository / API ngoài (do service định nghĩa)
│   │   └── service/               # Thực thi Port In, gọi Port Out
│   └── build.gradle
├── order-adapter/          # [INFRASTRUCTURE] Giao tiếp với thế giới bên ngoài
│   ├── src/main/java/com/furnisight/order/adapter/
│   │   ├── in/                    # Nhận Request (Controller, Listener, DTO, Mapper)
│   │   ├── out/                   # Gọi ra ngoài (Repository Impl, Model, API Client)
│   │   └── config/                # Config Spring Boot, Security, Kafka, DB...
│   └── build.gradle
└── order-bootstrap/        # [ENTRYPOINT] Nơi khởi chạy Application
    ├── src/main/java/com/furnisight/order/bootstrap/
    │   └── OrderApplication.java # Chứa hàm Main
    └── build.gradle
```

### Chi Tiết Các Module

#### 1. `order-domain` (Core)

**Vai trò**: Chứa **Domain Layer** (Entities, Value Objects). Tập trung business rules cốt lõi; độc lập với các layer khác.
**Tại sao cần tách riêng?**: Đảm bảo tính thuần khiết của nghiệp vụ, dễ dàng tái sử dụng và kiểm thử.

#### 2. `order-application` (Use Cases)

**Vai trò**: Chứa Port (in/out) và Use Case Service. Nó chỉ depends vào `order-domain`.
**Tại sao cần tách riêng?**: Điều phối nghiệp vụ, không phụ thuộc framework bên ngoài.

#### 3. `order-adapter` (Infrastructure)

**Vai trò**: Giao tiếp với thế giới bên ngoài. Chứa REST Controllers, Data Access Repositories, và cấu hình Config (Spring Web, Data JPA, Kafka,...). Nó depends vào `application` và `domain`.
**Tại sao cần tách riêng?**: Là ranh giới kỹ thuật nơi framework, protocol, database thực sự can thiệp. Thay đổi DB từ MySQL sang Mongo chỉ cần sửa trong module này.

#### 4. `order-bootstrap` (Entrypoint)

**Vai trò**: Đóng vai trò làm Entrypoint lắp ráp chương trình với hàm `main()`. Phụ thuộc vào cả 3 module trên để wire chúng lại và khởi động Spring Application.
**Tại sao cần tách riêng?**: Loại bỏ sự phụ thuộc chéo vòng vèo. Nếu cần biến app thành Serverless Function, chỉ cần viết lại module bootstrap này.

## Hướng dẫn Test Thanh toán (VNPay Sandbox)

Để test chức năng thanh toán qua VNPay trên môi trường Sandbox, bạn **KHÔNG ĐƯỢC** dùng app ngân hàng thật (như VCB, Techcombank, MoMo...) để quét mã QR vì app thật không thể nhận diện được mã 가맹 điểm (TmnCode) thử nghiệm.

### Các bước test chuẩn:
1. Khi giao diện thanh toán VNPay hiện ra, chọn phương thức: **Thẻ ATM và Tài khoản ngân hàng** (Thẻ nội địa).
2. Chọn ngân hàng: **NCB** (Ngân hàng Quốc dân).
3. Nhập thông tin thẻ test của VNPay:
   - **Số thẻ**: `9704198526191432198`
   - **Tên chủ thẻ**: `NGUYEN VAN A` (viết hoa không dấu)
   - **Ngày phát hành**: `07/15`
   - **Mật khẩu (nếu có)**: `123456`
4. Bấm **Xác thực / Thanh toán**.
5. Màn hình yêu cầu OTP hiện ra, nhập OTP là `123456` và bấm xác nhận.
6. Hệ thống sẽ báo thanh toán thành công và tự động redirect về trang callback của frontend (`/payment/success`).

_Lưu ý: Nếu giao dịch bị hủy, bạn vẫn có thể nhấn nút thử lại thanh toán ở frontend vì trạng thái đơn hàng sẽ được đánh dấu là `PAYMENT_FAILED` và hàng hóa vẫn được giữ chỗ._
