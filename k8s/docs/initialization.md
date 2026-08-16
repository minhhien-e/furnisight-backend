# K8s Initialization Commands

This document records the commands used to initialize and deploy the FurniSight backend to a local Kubernetes cluster using `k3d`.

## 1. Create the Cluster
```bash
k3d cluster create thesis-cluster --api-port 6550 -p "8080:80@loadbalancer"
```

## 2. Import Local Images
Since you already have the containers built locally, import them into the `k3d` cluster so they can be pulled without needing an external registry:
```bash
k3d image import furnisight-admin-service:latest furnisight-ai-image-classifier:latest furnisight-ai-image-reconstruction:latest furnisight-ai-review-sentiment:latest furnisight-cart-service:latest furnisight-catalog-service:latest furnisight-gateway:latest furnisight-media-service:latest furnisight-message-service:latest furnisight-notification-service:latest furnisight-order-service:latest furnisight-promotion-service:latest furnisight-review-service:latest furnisight-user-service:latest interior-3d-frontend:latest libretranslate/libretranslate:latest -c thesis-cluster
```

## 3. Apply Namespace
```bash
kubectl apply -f k8s/namespace.yaml
```

## 4. Apply ConfigMaps and Secrets
```bash
kubectl apply -f k8s/config/
```

## 5. Apply Infrastructure (Databases, Message Brokers, etc.)
```bash
kubectl apply -f k8s/infrastructure/
```

## 6. Apply Services
```bash
kubectl apply -f k8s/services/
```

## 7. Apply Ingress
```bash
kubectl apply -f k8s/ingress/
```

## 8. Verify Deployment
To check the status of all pods, run:
```bash
kubectl get pods --all-namespaces
```
To get the services and their exposed ports:
```bash
kubectl get svc -n furnisight-infras
kubectl get svc -n furnisight-apps
kubectl get svc -n furnisight-frontend
```

## 9. Start/Stop Cluster (Tiết kiệm tài nguyên)
Khi không sử dụng, bạn có thể tắt cluster để giải phóng RAM/CPU mà không làm mất dữ liệu:
```bash
k3d cluster stop thesis-cluster
```
Khi cần code tiếp, chỉ cần bật lại:
```bash
k3d cluster start thesis-cluster
```
*(Lưu ý: Không dùng lệnh `k3d cluster delete` trừ khi bạn muốn xóa sạch toàn bộ data và cài lại từ đầu)*
