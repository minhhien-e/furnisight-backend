# Hệ Thống Hướng Dẫn Kubernetes (Furnisight Backend)

Chào mừng bạn đến với bộ tài liệu Kubernetes chuẩn mực dành cho dự án **Furnisight**. Bộ tài liệu này được thiết kế và quy hoạch bài bản, chia thành 5 chương tuần tự, giúp từ sinh viên bảo vệ đồ án đến Kỹ sư Vận hành (DevOps) có thể hiểu rõ và tự tin làm chủ toàn bộ vòng đời của hệ thống.

## Mục Lục Hướng Dẫn

Vui lòng đọc theo trình tự từ trên xuống dưới:

- 📖 **[Chương 1: Lý Thuyết Kubernetes & Kiến Trúc Hệ Thống](./01-architecture-and-flow.md)**
  *Kiến trúc tổng quan, Master/Worker Node, giải thích các khái niệm cốt lõi (Pod, Service, Ingress), và luồng đi của dữ liệu (Request Flow) từ người dùng đến CSDL.*

- 🛠️ **[Chương 2: Hướng Dẫn Viết File Cấu Hình (Manifests)](./02-manifest-guide.md)**
  *Cấu trúc chuẩn của một file YAML (Deployment, Service), cách hệ thống này đang tối ưu hóa tính dư thừa bằng cách tận dụng `shared-env.yaml` cho gRPC và Config.*

- ⚙️ **[Chương 3: Hướng Dẫn Cài Đặt (Installation)](./03-installation-guide.md)**
  *Giải pháp cài đặt K3s (Rancher) cho môi trường máy chủ On-Premise. Các lưu ý tử huyệt về Storage (PVC) và cách xử lý Nginx Ingress Controller.*

- 🚀 **[Chương 4: Hướng Dẫn Khởi Chạy (Startup & Deployment)](./04-startup-and-deployment.md)**
  *Quy trình và thứ tự khởi chạy nghiêm ngặt của dự án (Từ tạo Namespace -> Đổ Script/Config -> Bật Hạ tầng -> Khởi chạy Services -> Kích hoạt Cổng Ingress).*

- 🩺 **[Chương 5: Quản Lý & Vận Hành (Management & Ops)](./05-management-and-ops.md)**
  *Cẩm nang "bắt bệnh" và gỡ lỗi bằng các câu lệnh `kubectl` thông dụng, hướng dẫn Scale hệ thống chịu tải, đọc Log (CrashLoopBackOff), và chui vào Database nội bộ.*

---
> **Lưu ý dành cho Hội Đồng / Nhà tuyển dụng:** Toàn bộ hệ thống Microservices này đã được áp dụng triệt để nguyên lý DRY (Don't Repeat Yourself) đối với ConfigMap, đồng thời sử dụng cấu trúc Service Registry cho hệ thống nội mạng gRPC (chuẩn dns:///).
