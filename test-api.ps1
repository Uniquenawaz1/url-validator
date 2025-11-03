<#
Simple smoke test for URL Validator API
Usage: In PowerShell, run: .\test-api.ps1
#>

$base = 'http://localhost:8080'

Write-Host "Checking health..."
try {
    $h = Invoke-WebRequest -Uri "$base/api/health" -UseBasicParsing -TimeoutSec 5
    Write-Host "Health: $($h.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Health request failed:" $_.Exception.Message -ForegroundColor Red
}

$tests = @(
    'https://google.com',
    'https://thiswebsitedoesnotexist12345.com',
    'http://example.com'
)

foreach ($u in $tests) {
    Write-Host "Checking: $u"
    try {
        $resp = Invoke-WebRequest -Uri "$base/api/check-url" -Method POST -Body (ConvertTo-Json @{url=$u}) -ContentType 'application/json' -UseBasicParsing -TimeoutSec 10
        Write-Host $resp.Content -ForegroundColor Cyan
    } catch {
        Write-Host "Request failed for $u: $($_.Exception.Message)" -ForegroundColor Red
    }
}
