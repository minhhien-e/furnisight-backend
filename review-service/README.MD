# Review Service

Service này đóng vai trò là Trust Execution Engine (Bộ máy thực thi niềm tin) trong hệ thống FurniSight, chịu trách nhiệm quản lý vòng đời đánh giá (UGC) và thực thi các lệnh phạt (Shadow Ban) để đảm bảo tính trung thực của nền tảng
## Kiến Trúc (Architecture)

Review Service áp dụng mô hình Pragmatic CQRS kết hợp với Event-Driven Architecture
### Cây Thư Mục (Directory Structure)

```
review-service/
├── review-bootstrap/               # [THE ENABLER] - Khởi chạy & Cấu hình tập trung
│   ├── src/main/java/com/furnisight/review/
│   │   └── BootstrapApplication.java# Main class quét toàn bộ hệ thống
│   └── src/main/resources/
│       └── application.yml         # Quản lý Port 5433, ES Uris, Kafka Brokers
│
├── review-api/                     # [DRIVING ADAPTER - REST]
│   └── src/main/java/com/furnisight/review/api/
│       ├── controller/             # Tiếp nhận REST Request
│       ├── dto/                    # RequestDTO, Command, Query
│       └── mapper/                 # Map Request -> Command (Dùng MapStruct)
│
├── review-core/                    # [THE HEART] - Tinh hoa nghiệp vụ (Hexagon Nội khu)
│   └── src/main/java/com/furnisight/review/core/
│       ├── application/            # [INPUT PORTS] Định nghĩa Use Cases
│       │   ├── usecase/            # Interface: PostReviewUseCase, SearchReviewUseCase
│       │   └── service/            # Implementation của logic (Điều phối Aggregate/Policy)
│       ├── domain/                 # [DOMAIN] Tầng nghiệp vụ thuần khiết
│       │   ├── aggregate/          # Review Aggregate Root + Local Entities
│       │   ├── event/              #  bắn qua kafka
│       │   ├── valueobject/        # Rating, ReviewContent, TrustScore
│       │   └── policy/             # ShadowBanPolicy, VisibilityPolicy
│       ├── repository/             # [OUTPUT PORTS] Hợp đồng dữ liệu
│       │   ├── ReviewWritePort     # Cổng ghi (Dành cho SQL)
│       │   └── ReviewReadPort      # Cổng đọc (Dành cho Elasticsearch)
│       └── projection/             # Read Model: Dữ liệu tối ưu để ES trả về
│
├── review-worker/                  # [DRIVING ADAPTER - EVENT]
│   └── src/main/java/com/furnisight/review/worker/
│       └── consumer/               # Nghe Kafka để trigger logic đồng bộ
│           └── strategy/           # Logic xử lý Spam/Trust Score hậu kỳ
│
└── review-infrastructure/          # [DRIVEN ADAPTERS] - Công cụ thực thi
    └── src/main/java/com/furnisight/review/infrastructure/
        └── repository/             # [ADAPTER IMPL] Triển khai Port
            ├── impl/                # Thực thi (implements) ReviewWritePort từ lớp Core.
            ├── jpa/                #Kế thừa JpaRepository của Spring Data.
            └── es/                 # EsReviewAdapter (Thực thi ReadPort)
```

### Chi Tiết Các Module

**Review Service** là trung tâm xử lý đánh giá (UGC) cho hệ thống FurniSight, tích hợp các chính sách kiểm duyệt tự động như Shadow Ban và Trust Scoring dựa trên kiến trúc Hexagonal và mô hình CQRS.

---

### Chi Tiết Các Module

#### 1. review-core (The Heart & Trust Engine)
* **Vai trò**: Trung tâm xử lý nghiệp vụ, độc lập hoàn toàn với công nghệ.
* **Đặc điểm**:
    * **Domain Layer**: Chứa **Aggregate Root** (Review) và **Value Objects** (Rating, Content) để thực thi các luật chơi bất biến.
    * **Application Layer**: Điều phối các **Use Case** (PostReview, SearchReview) và kiểm tra chính sách nghiệp vụ (**ShadowBanPolicy**).
    * **Ports Definition**: Định nghĩa các cổng giao tiếp (Interfaces) như **ReviewWritePort** và **ReviewReadPort**.



#### 2. review-api (Driving Adapter - REST)
* **Vai trò**: Cổng tiếp nhận yêu cầu từ người dùng (Inbound Adapter).
* **Đặc điểm**:
    * **CQRS Orchestration**: Tách biệt luồng Ghi (Command) và luồng Đọc (Query - lấy trực tiếp từ Elasticsearch).
    * **Mapping**: Chuyển đổi REST Request sang ngôn ngữ của Core (Commands/Queries).
    * **Idempotency**: Chặn đánh giá trùng lặp ngay tại cửa ngõ thông qua `order_item_id`.

#### 3. review-worker (Driving Adapter - Event)
* **Vai trò**: "Người làm vườn" (Gardener) giữ cho Read Model luôn tươi mới.
* **Đặc điểm**:
    * **Event Consumer**: Tiêu thụ sự kiện từ **Kafka Hub** để xử lý hậu kỳ.
    * **ES Sync**: Đồng bộ hóa và "làm giàu" dữ liệu (**Data Enrichment**) từ PostgreSQL sang Elasticsearch.
    * **Performance**: Đảm bảo tốc độ hiển thị cho Frontend bằng cách chuẩn bị sẵn các bản chiếu (**Projections**).



#### 4. review-infrastructure (Driven Adapters)
* **Vai trò**: Hệ thống "ống dẫn", hiện thực hóa các yêu cầu của Core.
* **Đặc điểm**:
    * **Adapters Impl**: Triển khai thực tế các cổng Ghi (**JPA/PostgreSQL**) và cổng Đọc (**Elasticsearch**).
    * **Isolation**: Cô lập các Annotation công nghệ (`@Entity`, `@Document`) khỏi tầng nghiệp vụ.
    * **Messaging**: Quản trị giao thức giao tiếp (**Avro Schema**) giữa các service.

#### 5. review-bootstrap (The Wiring Station)
* **Vai trò**: Điểm khởi chạy và cấu hình tập trung của toàn bộ Service.
* **Đặc điểm**:
    * **Dependency Wiring**: Sử dụng **Spring IoC** để "hàn" Adapters (Infra) vào đúng Ports (Core).
    * **Configuration**: Quản lý tập trung thông số kết nối (Postgres, ES, Kafka) trong `application.yml`.

---
