# ============================================================
# seed-all.ps1 - Seed tat ca databases vao K8s PostgreSQL
#
# QUAN TRONG: Script nay dung Start-Process -RedirectStandardInput
# thay vi pipe (|) de tranh loi mat UTF-8 encoding tren Windows.
#
# Nguyen nhan loi UTF-8:
#   PowerShell pipe (|) tu dong convert text sang IBM437/OEM encoding
#   truoc khi gui vao stdin cua kubectl, khien tieng Viet bi corrupt.
#   Start-Process doc bytes thu o tu file va gui thang, giu nguyen UTF-8.
# ============================================================

$ErrorActionPreference = "Continue"
$Namespace = "furnisight-infras"
$Pod = "postgres-0"
$Container = "postgres"
$ScriptDir = $PSScriptRoot

# ---- Ham chay SQL file vao postgres (UTF-8 safe) ----
function Invoke-SqlFile {
    param(
        [string]$SqlFile,
        [string]$Database
    )
    if (-not (Test-Path $SqlFile)) {
        Write-Host "  [SKIP] File khong ton tai: $SqlFile" -ForegroundColor Yellow
        return
    }
    $fileName = Split-Path $SqlFile -Leaf
    Write-Host "  Dang chay $fileName -> $Database ..." -ForegroundColor Gray

    $proc = Start-Process `
        -FilePath "D:\DevTools\kubectl.exe" `
        -ArgumentList "exec -n $Namespace $Pod -c $Container -i -- psql -U postgres -d $Database -v ON_ERROR_STOP=0" `
        -RedirectStandardInput $SqlFile `
        -NoNewWindow -Wait -PassThru

    if ($proc.ExitCode -eq 0) {
        Write-Host "  [OK] $fileName -> $Database" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] $fileName -> $Database (exit $($proc.ExitCode))" -ForegroundColor Yellow
    }
}

# ---- Ham xoa du lieu truoc khi seed lai ----
function Clear-Tables {
    param([string]$Database, [string]$TruncateSql)
    Write-Host "  Xoa du lieu cu trong $Database ..." -ForegroundColor Gray
    D:\DevTools\kubectl.exe exec -n $Namespace $Pod -c $Container -- `
        psql -U postgres -d $Database -c $TruncateSql 2>&1 | Where-Object { $_ -match 'ERROR' } | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
}

# ---- Kiem tra postgres pod dang chay ----
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host " FurniSight - Seed All Databases" -ForegroundColor Cyan
Write-Host " NOTE: Dung Start-Process de giu UTF-8" -ForegroundColor DarkCyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$podStatus = D:\DevTools\kubectl.exe get pod -n $Namespace $Pod -o jsonpath='{.status.phase}' 2>&1
if ($podStatus -ne "Running") {
    Write-Host "[ERROR] Pod $Pod chua Running (status: $podStatus)" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Pod $Pod dang Running" -ForegroundColor Green
Write-Host ""

# ============================================================
# 1. CATALOG DB
# ============================================================
Write-Host "[1/4] furnisight_catalog_db" -ForegroundColor Cyan
Clear-Tables -Database "furnisight_catalog_db" -TruncateSql `
    "TRUNCATE TABLE product_variant_images, product_variants, products, categories, room_types CASCADE;"
Invoke-SqlFile -SqlFile "$ScriptDir\catalog-seed.sql" -Database "furnisight_catalog_db"

# ============================================================
# 2. PROMOTION DB
# ============================================================
Write-Host ""
Write-Host "[2/4] furnisight_promotion_db" -ForegroundColor Cyan
Clear-Tables -Database "furnisight_promotion_db" -TruncateSql `
    "TRUNCATE TABLE promotion_combo_items, promotion_combos, promotions CASCADE;"
Invoke-SqlFile -SqlFile "$ScriptDir\seed-data.sql" -Database "furnisight_promotion_db"

# ============================================================
# 3. REVIEW DB
# ============================================================
Write-Host ""
Write-Host "[3/4] furnisight_review_db" -ForegroundColor Cyan
Clear-Tables -Database "furnisight_review_db" -TruncateSql `
    "TRUNCATE TABLE reviews CASCADE;"
Invoke-SqlFile -SqlFile "$ScriptDir\seed-reviews.sql" -Database "furnisight_review_db"

# ============================================================
# 4. USER DB (accounts)
# ============================================================
Write-Host ""
Write-Host "[4/4] furnisight_user_db" -ForegroundColor Cyan
Invoke-SqlFile -SqlFile "$ScriptDir\seed-two-accounts.sql" -Database "furnisight_user_db"

# ============================================================
# Verify
# ============================================================
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Ket qua" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

$checks = @(
    @{db="furnisight_catalog_db";    q="SELECT name FROM products LIMIT 1"},
    @{db="furnisight_promotion_db";  q="SELECT name FROM promotions LIMIT 1"},
    @{db="furnisight_review_db";     q="SELECT user_name FROM reviews LIMIT 1"},
    @{db="furnisight_user_db";       q="SELECT display_name FROM user_profiles LIMIT 1"}
)
foreach ($c in $checks) {
    $result = D:\DevTools\kubectl.exe exec -n $Namespace $Pod -c $Container -- `
        psql -U postgres -d $c.db -t -c $c.q 2>&1
    $sample = ($result | Where-Object { $_ -match '\S' } | Select-Object -First 1).Trim()
    Write-Host "  $($c.db.PadRight(30)) -> $sample" -ForegroundColor $(
        if ($sample -and $sample -notmatch 'ERROR') { 'Green' } else { 'Red' }
    )
}

Write-Host ""
Write-Host "[DONE] Seed hoan tat!" -ForegroundColor Green
