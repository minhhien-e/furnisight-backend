# Chương 5: Hướng Dẫn Quản Lý & Vận Hành (Management & Ops)

Sau khi hệ thống đã đi vào hoạt động (Running), vai trò của bạn chuyển sang bảo trì (Operations). Dưới đây là các lệnh `kubectl` thiết yếu bạn phải nắm vững để xử lý sự cố hàng ngày.

## 1. Xem trạng thái Hệ Thống

Để biết hệ thống đang "khỏe mạnh" hay không, hãy kiểm tra danh sách Pods:
```bash
kubectl get pods -n furnisight-apps
kubectl get pods -n furnisight-infras
```
- Trạng thái `Running`: Hoạt động bình thường.
- Trạng thái `Pending`: K8s chưa tìm thấy Node nào đủ CPU/RAM để chạy.
- Trạng thái `CrashLoopBackOff`: Ứng dụng liên tục bị lỗi ngắt đột ngột (thường do sai cấu hình, sai password DB, hoặc thiếu code).

## 2. Debugging & Kiểm Tra Log

Khi gặp lỗi `CrashLoopBackOff`, hành động duy nhất cứu sống bạn là đọc Log của Container đó:
```bash
# Thay tên pod bằng tên thực tế
kubectl logs -f user-service-586b4f6b6-v4kpx -n furnisight-apps
```
Nếu Pod đó có nhiều container (rất hiếm trong dự án này), thêm cờ `-c <tên-container>`.

Để xem chi tiết vì sao Pod bị lỗi từ góc độ của Kubernetes (chứ không phải góc độ của Code Spring Boot), dùng lệnh describe:
```bash
kubectl describe pod user-service-586b4f6b6-v4kpx -n furnisight-apps
```
Cuộn xuống phần **Events** ở cuối cùng. Bạn sẽ thấy các lỗi như:
- `ImagePullBackOff`: Gõ sai tên image hoặc không có quyền truy cập Docker Hub.
- `Liveness probe failed`: Healthcheck bị timeout (App quá nặng hoặc bị treo hờ).

## 3. Scale (Tăng giảm tải)

Vào ngày hội mua sắm sale, lưu lượng người dùng tăng đột biến, hệ thống bị nghẽn (CPU > 90%). Bạn chỉ cần gõ duy nhất 1 dòng lệnh để K8s tự động nhân bản service đó ra làm nhiều bản sao (chạy song song):

```bash
kubectl scale deployment order-service --replicas=3 -n furnisight-apps
```
Lập tức, sẽ có 3 Pods `order-service` cùng xử lý đơn đặt hàng. (Service nội bộ của K8s sẽ tự động Cân bằng tải - Load Balance - luồng traffic tới 3 Pods này rất công bằng).

Khi qua mùa sale, bạn thu hồi tài nguyên về 1:
```bash
kubectl scale deployment order-service --replicas=1 -n furnisight-apps
```

## 4. Xóa Khẩn Cấp (Restart Pod)

Trong Docker, bạn dùng `docker restart`. Trong K8s, **bạn không thể restart một Pod**. Thay vào đó, bạn **xóa** Pod đó đi. Vì có Deployment bảo kê, K8s sẽ lập tức tạo ra 1 Pod mới hoàn toàn sạch sẽ để thế chỗ.

```bash
kubectl delete pod user-service-586b4f6b6-v4kpx -n furnisight-apps
```

## 5. Truy cập cục bộ (Port Forwarding)

Đôi khi bạn muốn chui thẳng vào Database (đang nằm kín trong K8s) bằng phần mềm DBeaver hoặc pgAdmin trên máy tính của bạn. Dùng Port Forward:

```bash
# Đẩy port 5432 của máy tính trỏ thẳng vào port 5432 của Pod postgres
kubectl port-forward svc/postgres 5432:5432 -n furnisight-infras
```
Bây giờ, bạn mở DBeaver, cấu hình kết nối tới `localhost:5432` là sẽ chui được thẳng vào DB Production!

## 6. Tạm Dừng, Khởi Động Lại & Xóa Hoàn Toàn (Stop/Restart/Teardown)

Khi bạn muốn tắt, bật lại hoặc dọn dẹp hệ thống thay vì chỉ khởi chạy (setup) lần đầu, hãy dùng các lệnh sau:

### 6.1. Khởi động lại (Restart) an toàn
Dùng khi bạn vừa cập nhật ConfigMap/Secret hoặc muốn reset trạng thái của một ứng dụng mà không gây gián đoạn (Zero Downtime).

```bash
# Restart một service cụ thể
kubectl rollout restart deployment user-service -n furnisight-apps

# Restart TOÀN BỘ các services trong namespace
kubectl rollout restart deployment --all -n furnisight-apps
```

### 6.2. Tắt tạm thời (Stop / Pause)
Thay vì xóa file, cách tốt nhất để tắt một service là giảm (scale) số lượng pod của nó về `0`. K8s sẽ dập tắt tiến trình nhưng vẫn lưu lại cấu hình.

```bash
# Tắt một service
kubectl scale deployment user-service --replicas=0 -n furnisight-apps

# Tắt toàn bộ services
kubectl scale deployment --all --replicas=0 -n furnisight-apps
```

### 6.3. Bật lại (Start / Resume)
Khi muốn bật lại service đã tắt, chỉ cần scale số lượng pod lên lại `1` (hoặc số lượng tùy ý):

```bash
kubectl scale deployment user-service --replicas=1 -n furnisight-apps
```

### 6.4. Xóa hoàn toàn (Teardown)
Nếu bạn muốn xóa sạch cấu hình khỏi K8s để chạy lại lệnh `apply` từ đầu:

```bash
# Xóa toàn bộ tầng ứng dụng (microservices)
kubectl delete -f services/

# Xóa toàn bộ tầng hạ tầng (database, kafka...)
kubectl delete -f infrastructure/
```
*(Lưu ý: Lệnh xóa `infrastructure/` có thể làm mất dữ liệu Database nếu bạn không cấu hình volume Persistent để giữ lại dữ liệu, hãy cẩn thận khi dùng ở môi trường Production).*
