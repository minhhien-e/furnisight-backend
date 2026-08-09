# Media Service

Service này được xây dựng theo kiến trúc **Multi-Module**, chịu trách nhiệm xử lý upload media, convert/resize và quét virus.

## Kiến Trúc (Architecture)

Media Service áp dụng mô hình phân lớp rõ ràng và được chia thành 5 module chính:

### Cây Thư Mục (Directory Structure)

```text
media-service/
├── build.gradle                   # File cấu hình build chung cho toàn project
├── settings.gradle                # File khai báo các module con
├── media-bootstrap/               # [ENTRY POINT] Khởi chạy ứng dụng và File Cấu Hình
│   ├── src/main/java/com/furnisight/media/bootstrap/
│   │   └── MediaServiceApplication.java
│   └── src/main/resources/
│       ├── application.yml        # Cấu hình tập trung cho toàn project
│       └── db/migration/          # (Chờ phát triển) Database Migration Scripts
├── media-core/                    # [DOMAIN] Chứa logic cốt lõi (Entities, Repository Interfaces, Exceptions)
│   └── src/main/java/com/furnisight/media/core/
│       ├── model/                 # Entity, Enum (MediaAsset) và Event
│       ├── repository/            # Repository Interface định nghĩa các thao tác truy xuất dữ liệu
│       └── exception/             # Các custom exception
├── media-infrastructure/          # [INFRASTRUCTURE] Triển khai các interface từ core (Database, External Services)
│   └── src/main/java/com/furnisight/media/infrastructure/
│       ├── repository/impl/       # Implementation của Spring Data JPA Repositories
│       ├── antivirus/             # Tích hợp ClamAV qua Socket Client
│       └── storage/local/         # Lưu trữ Local thay thế S3 (Tự host file)
├── media-api/                     # [REST API] Nhận request HTTP và Điều phối
│   └── src/main/java/com/furnisight/media/api/
│       ├── controller/            # API Endpoints (Upload, Query, Mock S3 Stream)
│       └── service/               # Logic nghiệp vụ điều phối
└── media-worker/                  # [ASYNC WORKER] Xử lý media bất đồng bộ và định tuyến Event
    └── src/main/java/com/furnisight/media/worker/
        ├── consumer/              # Lắng nghe sự kiện (Spring Event / Kafka)
        └── processing/            # Logic xử lý file (Virus Scan, Validation)
```

### Chi Tiết Cụ Thể Từng Module

#### 1. `media-bootstrap` (Application Runner)
- **Vai trò**: Điểm bắt đầu (Entry point) khởi động ứng dụng Spring Boot. Nơi đây tập hợp tất cả dependencies từ các module khác và chứa file cấu hình chung `application.yml` duy nhất.

#### 2. `media-core` (Domain Layer)
- **Vai trò**: Chứa **Domain Layer** (Entities) và các Interface (Repository, Storage, VirusScanner). Module này không phụ thuộc vào framework nào bên ngoài (Pure Java) để đảm bảo tính độc lập của Business Logic. Thúc đẩy kiến trúc hướng sự kiện (Event-driven).

#### 3. `media-infrastructure` (Infrastructure Layer)
- **Vai trò**: Chứa các phần triển khai (Implementation) cụ thể cho các Interface định nghĩa ở `media-core`.
  - Kết nối và truy vấn Database thông qua JPA.
  - Tích hợp ClamAV daemon (`zINSTREAM` protocol).
  - Tích hợp Local Storage Provider giả lập chữ ký điện tử (Presigned URL) giống AWS S3.

#### 4. `media-api` (Orchestrator API)
- **Vai trò**: "Giao diện giao tiếp".
  - Cung cấp API (Presigned URL) cho khách hàng upload trực tiếp.
  - Triển khai endpoints tải file bằng Local Storage.
  - Nhận HTTP Webhooks khi tiến trình upload hoàn tất.

#### 5. `media-worker` (Async Processor)
- **Vai trò**: "Công nhân".
  - Lắng nghe các event (như `MediaUploadedEvent`, `MediaValidatedEvent`).
  - Xử lý các nghiệp vụ nặng: Kiểm tra chữ ký file, **Quét Virus (ClamAV)**, Update trạng thái Asset.
