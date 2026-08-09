# Chương 3: Hướng Dẫn Cài Đặt (Installation)

Tài liệu này hướng dẫn cách setup Kubernetes trên máy chủ **On-Premise** (Linux) và môi trường phát triển **Local (Windows)**.

## 1. Môi trường Local (Dành cho Windows)

Mặc dù K8s sinh ra để chạy chủ yếu trên lõi Linux, nhưng Microsoft và Docker đã phối hợp rất tốt để đưa K8s lên Windows thông qua **WSL2** (Windows Subsystem for Linux). Cách đơn giản và chuẩn xác nhất để lập trình K8s trên Windows là sử dụng **Docker Desktop**.

**Bước 1: Cài đặt WSL2 (Bắt buộc)**
Mở terminal (PowerShell dưới quyền Admin) và gõ:
```powershell
wsl --install
```
*Sau khi chạy lệnh, hãy khởi động lại máy tính.*

**Bước 2: Cài đặt Docker Desktop**
- Tải và cài đặt [Docker Desktop cho Windows](https://docs.docker.com/desktop/install/windows-install/).
- Trong lúc cài, hãy chắc chắn tích chọn **"Use WSL 2 instead of Hyper-V"**.

**Bước 3: Kích hoạt Kubernetes**
- Mở ứng dụng Docker Desktop.
- Vào biểu tượng bánh răng **Settings** ở góc phải trên.
- Chọn tab **Kubernetes** ở menu bên trái.
- Tích vào ô **"Enable Kubernetes"** và nhấn **Apply & restart**.
- Docker sẽ tải K8s cluster về (mất khoảng vài phút).

**Bước 4: Kiểm tra kết nối**
Mở PowerShell (hoặc VSCode Terminal) và gõ:
```powershell
kubectl get nodes
```
Nếu hiện ra node `docker-desktop` ở trạng thái `Ready`, bạn đã cài xong K8s trên Windows!

---

## 2. Môi trường Server Linux (Dành cho Production)

Giải pháp được khuyên dùng cho hệ thống Furnisight trên Linux Server là **K3s** (Bản phân phối K8s nhẹ, ổn định của Rancher).

### 2.1 Tại sao dùng K3s thay vì Kubeadm / Minikube?

- **Kubeadm:** Rất nặng, cài đặt phức tạp, không tích hợp sẵn Storage Provisioner (rất khó để cài Database).
- **Minikube:** Chỉ dùng cho việc test cá nhân, không phù hợp cho Production chạy liên tục.
- **K3s:** Đóng gói trong 1 file nhị phân duy nhất, tốn chưa tới 1GB RAM để chạy toàn bộ hệ thống K8s. Tích hợp sẵn **`local-path-provisioner`**, giúp K8s có thể tự động cấp phát ổ cứng (Persistent Volume) cho Database (Postgres/MongoDB).

### 2.2 Các Bước Cài Đặt K3s (Master Node)

Mở SSH vào máy chủ Linux (Ubuntu/CentOS) của bạn và chạy lệnh sau:

```bash
curl -sfL https://get.k3s.io | sh -
```

Kiểm tra xem hệ thống đã hoạt động chưa:
```bash
sudo k3s kubectl get nodes
```

Để có thể dùng lệnh `kubectl` mà không cần quyền `sudo`:
```bash
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
```

### 2.3 Quản Lý Storage cho Database

Do K3s mặc định đi kèm `local-path`, các `PersistentVolumeClaim` (PVC) trong các file `k8s/infrastructure/*.yaml` của bạn sẽ được kích hoạt tự động.

Dữ liệu vật lý của Database sẽ nằm tại thư mục:
```text
/var/lib/rancher/k3s/storage/
```

> [!WARNING]
> Lưu trữ `local-path` có nghĩa là dữ liệu gắn chặt với Node hiện tại. Nếu bạn triển khai Multi-Node (nhiều máy chủ), bạn bắt buộc phải thay thế bằng các giải pháp phân tán như **Longhorn**. Đối với Single-Node, bạn không cần quan tâm cảnh báo này.

### 2.4 Xử Lý Ingress Controller (Traefik)

Hệ thống K3s được cài đặt mặc định đi kèm với **Traefik Ingress Controller**. Hệ thống Furnisight tương thích hoàn toàn và sử dụng trực tiếp Traefik làm Ingress Controller mặc định mà không cần cài đặt hay cấu hình thêm phần mềm nào khác.

Để kiểm tra xem Traefik đã hoạt động hay chưa, bạn có thể chạy lệnh sau:

```bash
kubectl get pods -n kube-system | grep traefik
```

Khi thấy Pod Traefik hiển thị trạng thái `Running`, có nghĩa là hệ thống định tuyến của bạn đã sẵn sàng!
