# Chương 1: Lý Thuyết Kubernetes (K8s) & Kiến Trúc Hệ Thống

Kubernetes (K8s) là một nền tảng mã nguồn mở giúp tự động hóa việc triển khai, mở rộng và quản lý các ứng dụng được đóng gói trong container. Đối với hệ thống đồ sộ như Furnisight với hàng chục microservices, K8s là bắt buộc để đảm bảo tính ổn định và khả năng chịu tải.

## 1. Kiến trúc Hệ Thống K8s (Cluster Architecture)
Một cụm (Cluster) Kubernetes bao gồm 2 thành phần chính:

- **Control Plane (Master Node - Bộ não điều khiển):** Đưa ra các quyết định hệ thống (ở đâu, khi nào thì chạy container). Gồm:
  - `kube-apiserver`: Trung tâm giao tiếp, nhận mọi lệnh từ `kubectl`.
  - `etcd`: CSDL dạng key-value siêu tốc lưu toàn bộ trạng thái của K8s.
  - `kube-scheduler`: Phân bổ Pod vào các Worker Node đang rảnh rỗi CPU/RAM.
  - `kube-controller-manager`: Quản lý các luồng chạy ngầm để đảm bảo số lượng Pod đúng như kỳ vọng.
- **Worker Node (Máy chủ thực thi):** Các máy ảo hoặc máy vật lý chịu trách nhiệm chạy các ứng dụng.
  - `kubelet`: Agent giao tiếp với Master Node, đảm bảo các container đang chạy.
  - `kube-proxy`: Quản lý mạng nội bộ, định tuyến traffic (IP).

## 2. Các Khái Niệm Cốt Lõi (K8s Resources)

Trong K8s, ta không thao tác trực tiếp với container mà thông qua các tài nguyên (Resources):

- **Pod:** Đơn vị nhỏ nhất. Một Pod bọc lấy 1 (hoặc nhiều) container. Pod là tài nguyên "dễ bay màu" (ephemeral) - nó có thể bị chết và IP thay đổi liên tục.
- **Deployment:** "Người bảo mẫu" của Pod. Bạn khai báo cần 3 bản sao (replicas) của `user-service`, Deployment sẽ giám sát và tự động tạo Pod mới nếu có Pod cũ bị chết.
- **Service:** Bộ Cân Bằng Tải (Load Balancer) nội bộ. Vì IP của Pod thay đổi liên tục, Service cung cấp một IP tĩnh và Tên miền (DNS) cố định (VD: `http://user-service:8080`) để các Pod gọi nhau ổn định.
- **Ingress:** "Cánh cửa" mở ra Internet. Nó nhận các Request từ ngoài (VD: `api.furnisight.store`) và điều hướng vào đúng Service nội bộ dựa trên URL path.
- **ConfigMap & Secret:**
  - **ConfigMap:** Lưu các cấu hình không nhạy cảm (Host, Port, Timezone...).
  - **Secret:** Lưu các thông tin nhạy cảm đã bị mã hóa Base64 (Mật khẩu DB, JWT Key, API Keys).
- **PersistentVolumeClaim (PVC):** Vì Pod bay màu thì mất dữ liệu, PVC dùng để yêu cầu K8s cấp cho một ổ cứng thực sự, giúp lưu trữ vĩnh viễn dữ liệu của Database (Postgres, Mongo) dù Pod có bị khởi động lại.

## 3. Luồng đi của Dữ Liệu (Data Flow) trong Furnisight

Khi người dùng thực hiện một Request từ Frontend vào hệ thống Furnisight:

1. **Internet / Client:** Bấm nút "Thêm vào giỏ hàng" -> Request gửi tới `https://api.furnisight.store/cart`.
2. **K8s Ingress (Nginx):** Ingress nhận Request, đọc rules và thấy đường dẫn `/cart`. Nó định tuyến Request này vào Service `gateway` (API Gateway).
3. **API Gateway (`gateway-service`):** Kiểm tra bảo mật (Authentication), xác thực JWT Token qua `user-service`. Sau đó, forward Request tới `cart-service`.
4. **Microservices Communication (gRPC / HTTP):**
   - `cart-service` nhận request. Nó cần lấy thông tin sản phẩm nên sẽ gọi gRPC nội bộ tới `catalog-service` qua địa chỉ DNS `dns:///catalog-service:9090`.
5. **Database / Infrastructure:** 
   - Sau khi có đủ dữ liệu, `cart-service` lưu giỏ hàng vào MongoDB (đã được gắn PVC).
   - Nếu có thông báo, nó đẩy Message vào Kafka (được quản lý bởi Zookeeper/KRaft).
6. **Response:** Dữ liệu trả ngược về API Gateway -> Ingress -> Trình duyệt người dùng.
