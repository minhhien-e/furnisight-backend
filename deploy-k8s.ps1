# Script tu dong hoa trien khai K8s cho Thesis
$ErrorActionPreference = "Continue"
$BeDir = $PSScriptRoot

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "1. Kiem tra / Khoi tao K3d Cluster" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan

$clusterExists = (D:\DevTools\k3d.exe cluster list | Select-String "thesis-cluster")
if (-not $clusterExists) {
    D:\DevTools\k3d.exe cluster create thesis-cluster --image "rancher/k3s:v1.28.14-k3s1" --port "80:80@loadbalancer" --port "8081:8080@loadbalancer"
} else {
    Write-Host "Cluster 'thesis-cluster' already running." -ForegroundColor Green
}

# Merge va Cau hinh Kubeconfig cho Windows
D:\DevTools\k3d.exe kubeconfig merge thesis-cluster --kubeconfig-switch-context
$k3dPath = "$env:USERPROFILE\.config\k3d\kubeconfig-thesis-cluster.yaml"
$kubePath = "$env:USERPROFILE\.kube\config"
if (Test-Path $k3dPath) {
    Copy-Item -Path $k3dPath -Destination $kubePath -Force
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "2. Import tat ca 12 Docker Images vao K3d Cluster" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan

$allImages = @(
    "furnisight-frontend:latest",
    "furnisight-gateway:latest",
    "furnisight-user-service:latest",
    "furnisight-catalog-service:latest",
    "furnisight-order-service:latest",
    "furnisight-cart-service:latest",
    "furnisight-notification-service:latest",
    "furnisight-promotion-service:latest",
    "furnisight-review-service:latest",
    "furnisight-media-service:latest",
    "furnisight-message-service:latest",
    "furnisight-admin-service:latest"
)

Write-Host "Dang import 12 Docker Images vao thesis-cluster..." -ForegroundColor Yellow
D:\DevTools\k3d.exe image import $allImages -c thesis-cluster

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "3. Trien khai K8s Manifests" -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan
D:\DevTools\kubectl.exe apply -f "$BeDir\k8s\namespace.yaml" --validate=false
D:\DevTools\kubectl.exe apply -R -f "$BeDir\k8s\configs" --validate=false
D:\DevTools\kubectl.exe apply -R -f "$BeDir\k8s\infras" --validate=false
D:\DevTools\kubectl.exe apply -R -f "$BeDir\k8s\services" --validate=false

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "4. Kiem tra danh sach Pods K8s" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
D:\DevTools\kubectl.exe get pods -A
