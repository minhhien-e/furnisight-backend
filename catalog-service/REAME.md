# 🛍 Catalog Service

Catalog Service là **Source of Truth (Nguồn dữ liệu gốc)** chịu trách nhiệm quản lý toàn bộ vòng đời sản phẩm, danh mục và thương hiệu cho hệ thống FurniSight Clone.

---

# 🚀 Architecture

Catalog Service áp dụng:

- **Clean Architecture**
- **Domain-Driven Design (DDD)**
- **CQRS (Command Query Responsibility Segregation)**

Mục tiêu:
- Đảm bảo tính toàn vẹn dữ liệu
- Tách biệt rõ ràng Business Logic
- Tối ưu hiệu suất đọc/ghi
- Dễ mở rộng và bảo trì

---

# 📂 Directory Structure
```
catalog-service/
├── build.gradle                   # Cấu hình build chung (quản lý version Spring Boot)
├── settings.gradle                # Khai báo 5 module con
├── catalog-domain/                # [CORE] Chứa Business Logic cốt lõi (Entities, Aggregates)
│   └── src/main/java/com/furnisight/catalog/domain/
│       ├── aggregate/             # Aggregate Root (Ví dụ: Product.java)
│       └── valueobject/           # Các đối tượng giá trị (SeoInfo, Dimensions)
├── catalog-application/           # [USE CASES] Điều phối nghiệp vụ theo mô hình CQRS
│   └── src/main/java/com/furnisight/catalog/application/
│       ├── command/               # Xử lý các thao tác thay đổi dữ liệu (Write)
│       └── query/                 # Xử lý các thao tác truy vấn dữ liệu (Read)
├── catalog-infrastructure/        # [ADAPTERS] Kết nối Database, Cache và Message Broker
│   └── src/main/java/com/furnisight/catalog/infrastructure/
│       ├── persistence/           # PostgreSQL (JPA) và Elasticsearch
│       └── messaging/             # Kafka Producers (Transactional Outbox)
├── catalog-presentation/          # [API] Tiếp nhận và phản hồi request từ bên ngoài
│   └── src/main/java/com/furnisight/catalog/presentation/
│       └── controller/            # REST API Endpoints
└── catalog-bootstrap/             # [CONFIG] Điểm khởi chạy ứng dụng Spring Boot
└── src/main/java/com/furnisight/catalog/bootstrap/
└── CatalogApplication.java
```

---

## 🛠 Chi Tiết Các Module
#### 1. `catalog-domain` (The Core)
   - Vai trò: Chứa các quy tắc nghiệp vụ quan trọng nhất.
   - Đặc điểm: Là Java thuần (Pure Java), không phụ thuộc vào bất kỳ Framework nào để đảm bảo tính linh hoạt và dễ kiểm thử.

#### 2. `catalog-application` (Use Case Layer)
   - Vai trò: Triển khai các kịch bản nghiệp vụ dựa trên yêu cầu từ người dùng.
   - CQRS: Chia tách rõ ràng giữa luồng xử lý lệnh (Command) và luồng truy vấn (Query) để tối ưu hóa hiệu năng hệ thống.

#### 3. `catalog-infrastructure` (External Tools)
   - Vai trò: Thực thi các giao tiếp với hạ tầng kỹ thuật.
   - Công nghệ:
     - PostgreSQL: Lưu trữ dữ liệu Master (Sử dụng JSONB cho thuộc tính động).
     - Elasticsearch: Phục vụ tìm kiếm và lọc sản phẩm tốc độ cao.
     - Kafka: Đồng bộ sự kiện thay đổi sản phẩm sang các service khác.

#### 4. `catalog-presentation` (Web/API)
   - Vai trò: Định nghĩa các RESTful API, thực hiện validate dữ liệu đầu vào và chuyển đổi DTO.

#### 5. `catalog-bootstrap` (The Assembler)
   - Vai trò: Kết nối (wiring) tất cả các module lại với nhau bằng Spring Dependency Injection và kích hoạt ứng dụng.

## 📈 State Machine (Trạng thái sản phẩm)
Sản phẩm trong hệ thống tuân theo các trạng thái sau:

- `DRAFT`: Mới tạo, đang hoàn thiện thông tin.

- `ACTIVE`: Đã duyệt và đang hiển thị bán.

- `INACTIVE`: Tạm ẩn bởi người bán.

- `SUSPENDED`: Bị khóa do vi phạm chính sách.

- `DELETED`: Xóa mềm (Soft-delete).
