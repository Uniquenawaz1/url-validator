# Production Deployment Test Script (Windows PowerShell)
# This script tests the URL validator in a Docker container

param(
    [string]$Image = "ghcr.io/uniquenawaz1/url-validator:latest",
    [string]$ContainerName = "url-validator-test",
    [int]$Port = 8080
)

$BaseUrl = "http://localhost:$Port"

function Cleanup {
    Write-Host ""
    Write-Host "Cleaning up..." -ForegroundColor Yellow
    docker stop $ContainerName 2>&1 | Out-Null
    docker rm $ContainerName 2>&1 | Out-Null
}

# Set trap to cleanup on exit
trap {
    Cleanup
    exit 1
}

Write-Host "================================" -ForegroundColor Cyan
Write-Host "URL Validator Production Test" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Pull latest image
Write-Host "1. Pulling latest Docker image..." -ForegroundColor Green
docker pull $Image
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Image pulled successfully" -ForegroundColor Green
} else {
    Write-Host "   ✗ Failed to pull image" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Run container
Write-Host "2. Starting Docker container..." -ForegroundColor Green
docker run -d `
    --name $ContainerName `
    -p "$($Port):8080" `
    $Image

if ($LASTEXITCODE -eq 0) {
    $ContainerId = docker ps -q -f "name=$ContainerName"
    Write-Host "   ✓ Container started (ID: $ContainerId)" -ForegroundColor Green
} else {
    Write-Host "   ✗ Failed to start container" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Wait for container to be ready
Write-Host "3. Waiting for app to start..." -ForegroundColor Green
$MaxAttempts = 30
for ($i = 1; $i -le $MaxAttempts; $i++) {
    try {
        $Response = Invoke-WebRequest -Uri "$BaseUrl/api/health" -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($Response.StatusCode -eq 200) {
            Write-Host "   ✓ App is ready" -ForegroundColor Green
            break
        }
    } catch {
        # App not ready yet
    }
    
    if ($i -eq $MaxAttempts) {
        Write-Host "   ✗ Timeout waiting for app to start" -ForegroundColor Red
        docker logs $ContainerName | Select-Object -Last 20
        exit 1
    }
    
    Write-Host -NoNewline "."
    Start-Sleep -Seconds 1
}
Write-Host ""

# Test health endpoint
Write-Host "4. Testing health endpoint..." -ForegroundColor Green
try {
    $Response = Invoke-WebRequest -Uri "$BaseUrl/api/health" -UseBasicParsing -TimeoutSec 10
    $Content = $Response.Content | ConvertFrom-Json
    if ($Content.status -eq "ok") {
        Write-Host "   ✓ Health check passed" -ForegroundColor Green
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Cyan
    } else {
        Write-Host "   ✗ Health check failed" -ForegroundColor Red
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "   ✗ Health check request failed: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test Google
Write-Host "5. Testing https://www.google.com (should be valid)..." -ForegroundColor Green
try {
    $Body = @{"url" = "https://www.google.com"} | ConvertTo-Json
    $Response = Invoke-WebRequest -Uri "$BaseUrl/api/check-url" -Method POST `
        -ContentType "application/json" `
        -Body $Body `
        -UseBasicParsing -TimeoutSec 60
    
    if ($Response.Content -match "Valid") {
        Write-Host "   ✓ Google URL validation passed" -ForegroundColor Green
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Cyan
    } else {
        Write-Host "   ✗ Google URL validation failed" -ForegroundColor Red
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "   ✗ Test failed: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test GoIndiGo
Write-Host "6. Testing https://www.goindigo.in (Cloudflare-protected, should be valid)..." -ForegroundColor Green
try {
    $Body = @{"url" = "https://www.goindigo.in"} | ConvertTo-Json
    $Response = Invoke-WebRequest -Uri "$BaseUrl/api/check-url" -Method POST `
        -ContentType "application/json" `
        -Body $Body `
        -UseBasicParsing -TimeoutSec 60
    
    if ($Response.Content -match "Valid") {
        Write-Host "   ✓ GoIndiGo URL validation passed" -ForegroundColor Green
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Cyan
    } else {
        Write-Host "   ✗ GoIndiGo URL validation failed" -ForegroundColor Red
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Yellow
        Write-Host "   Note: This may fail if Cloudflare blocks the request" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ⚠ Test failed (may be network/Cloudflare issue): $_" -ForegroundColor Yellow
}
Write-Host ""

# Test invalid URL
Write-Host "7. Testing https://invalid-url-that-does-not-exist-12345.com (should be invalid)..." -ForegroundColor Green
try {
    $Body = @{"url" = "https://invalid-url-that-does-not-exist-12345.com"} | ConvertTo-Json
    $Response = Invoke-WebRequest -Uri "$BaseUrl/api/check-url" -Method POST `
        -ContentType "application/json" `
        -Body $Body `
        -UseBasicParsing -TimeoutSec 60
    
    if ($Response.Content -match "Invalid") {
        Write-Host "   ✓ Invalid URL correctly identified" -ForegroundColor Green
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Cyan
    } else {
        Write-Host "   ✗ Invalid URL test failed" -ForegroundColor Red
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "   ✗ Test failed: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test URL without prefix
Write-Host "8. Testing github.com (without https prefix)..." -ForegroundColor Green
try {
    $Body = @{"url" = "github.com"} | ConvertTo-Json
    $Response = Invoke-WebRequest -Uri "$BaseUrl/api/check-url" -Method POST `
        -ContentType "application/json" `
        -Body $Body `
        -UseBasicParsing -TimeoutSec 60
    
    if ($Response.Content -match "Valid") {
        Write-Host "   ✓ URL prefix handling works" -ForegroundColor Green
        Write-Host "   Response: $($Response.Content)" -ForegroundColor Cyan
    } else {
        Write-Host "   ⚠ URL prefix handling test result: $($Response.Content)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ⚠ Test had issue: $_" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Tests Complete!" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Container is still running. To keep testing, access:" -ForegroundColor Yellow
Write-Host "   $BaseUrl" -ForegroundColor Cyan
Write-Host ""
Write-Host "To stop the container, run: docker stop $ContainerName" -ForegroundColor Yellow

# Show logs
Write-Host ""
Write-Host "Last 20 log lines:" -ForegroundColor Green
docker logs $ContainerName | Select-Object -Last 20
