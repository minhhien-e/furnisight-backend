#!/bin/bash
set -e

echo "🚀 Xoá các container image furnisight cũ..."
# Remove all images that have 'furnisight' in the repository name to ensure a fresh start
docker rmi $(docker images --format "{{.Repository}}:{{.Tag}}" | grep "furnisight") -f || true

echo "🚀 Build lại toàn bộ backend services..."
cd "/run/media/minhhien/New Volume/Workspace/Projects/Thesis/be"
docker compose build --no-cache

echo "📦 Nạp backend images vào K3d cluster..."
docker images --format "{{.Repository}}:{{.Tag}}" | grep "furnisight-" | xargs -I {} k3d image import {} -c thesis-cluster

echo "🔄 Khởi động lại backend deployments..."
kubectl rollout restart deployment -n furnisight-apps

echo "🚀 Build lại frontend service..."
cd "/run/media/minhhien/New Volume/Workspace/Projects/Work/FE/interior-3d"
docker build --no-cache -t furnisight-frontend:latest .

echo "📦 Nạp frontend image vào K3d cluster..."
k3d image import furnisight-frontend:latest -c thesis-cluster

echo "🔄 Khởi động lại frontend deployment..."
kubectl rollout restart deployment furnisight-frontend -n furnisight-frontend

echo "✅ Hoàn tất!"
