#!/bin/bash

SERVICE=$1

if [ -z "$SERVICE" ]; then
  echo "Đang triển khai lại toàn bộ dịch vụ trong k8s/services/..."
  kubectl apply -f k8s/services/
  kubectl rollout restart deployment -n furnisight-apps
  echo "Đã gửi lệnh khởi động lại toàn bộ dịch vụ."
else
  echo "Đang triển khai lại dịch vụ: $SERVICE..."
  if [ -f "k8s/services/$SERVICE.yaml" ]; then
    kubectl apply -f k8s/services/$SERVICE.yaml
    kubectl rollout restart deployment $SERVICE -n furnisight-apps
    echo "Đã triển khai lại $SERVICE."
  else
    echo "Lỗi: Không tìm thấy k8s/services/$SERVICE.yaml"
    exit 1
  fi
fi
