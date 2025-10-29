# Test script for FLPS API endpoints
Write-Host "Testing FLPS API endpoints..." -ForegroundColor Green

# Test the status endpoint first
Write-Host "`nTesting /api/v1/flps/status..." -ForegroundColor Yellow
try {
    $statusResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/flps/status" -Method Get -UseBasicParsing
    Write-Host "Status Code: $($statusResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Response Body:" -ForegroundColor Green
    $statusResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Error testing status endpoint: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n" + "="*50

# Test the real data endpoint
Write-Host "`nTesting /api/v1/flps/real-data..." -ForegroundColor Yellow
try {
    $realDataResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/flps/real-data" -Method Get -UseBasicParsing
    Write-Host "Status Code: $($realDataResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Response Body:" -ForegroundColor Green
    $realDataResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Error testing real-data endpoint: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nAPI testing complete!" -ForegroundColor Green