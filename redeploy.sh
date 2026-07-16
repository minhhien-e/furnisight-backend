#!/bin/bash

# Script tiện ích để deploy/cập nhật lại code lên Kubernetes nhanh chóng
# Cách dùng: ./redeploy.sh [tên-service]
# Ví dụ: ./redeploy.sh admin-service (chỉ cập nhật admin)
#        ./redeploy.sh (cập nhật toàn bộ)

SERVICE_NAME=$1

if [ -z "$SERVICE_NAME" ]; then
  echo "🚀 Kiểm tra và build lại các service (sử dụng cache)..."
  docker compose build
  
  echo "📦 Nạp images vào K3d cluster..."
  docker images --format "{{.Repository}}:{{.Tag}}" | grep "furnisight-" | xargs -I {} k3d image import {} -c thesis-cluster

  echo "🔄 Đang khởi động lại toàn bộ microservices..."
  kubectl rollout restart deployment -n furnisight-apps

else
  echo "🚀 Đang build lại $SERVICE_NAME..."
  docker compose build $SERVICE_NAME
  
  echo "📦 Nạp image vào K3d cluster..."
  # Tên image thường có dạng furnisight-[service_name]:latest
  IMAGE_NAME="furnisight-${SERVICE_NAME}:latest"
  k3d image import $IMAGE_NAME -c thesis-cluster

  echo "🔄 Đang khởi động lại deployment $SERVICE_NAME..."
  kubectl rollout restart deployment $SERVICE_NAME -n furnisight-apps
fi

echo "✅ Hoàn tất! Dùng lệnh sau để xem trạng thái:"
echo "kubectl get pods -n furnisight-apps -w"
