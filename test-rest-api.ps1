# Test REST API Phase 1
Write-Host "Testing REST API Phase 1 Implementation" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Check if Swagger UI is accessible
Write-Host "Test 1: Checking Swagger UI..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -Method GET -TimeoutSec 5
    if ($response.StatusCode -eq 200) {
        Write-Host "✅ Swagger UI is accessible" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Swagger UI not accessible (is the server running?)" -ForegroundColor Red
    Write-Host "   Start the server with: ./gradlew bootRun" -ForegroundColor Yellow
    exit 1
}

# Test 2: Check OpenAPI docs
Write-Host "Test 2: Checking OpenAPI documentation..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/v3/api-docs" -Method GET
    if ($response.openapi) {
        Write-Host "✅ OpenAPI docs available (version: $($response.openapi))" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ OpenAPI docs not accessible" -ForegroundColor Red
}

# Test 3: Check health endpoint
Write-Host "Test 3: Checking health endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET
    if ($response.status -eq "UP") {
        Write-Host "✅ Health check passed (status: $($response.status))" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Health check failed" -ForegroundColor Red
}

# Test 4: Test Raiders API (GET all)
Write-Host "Test 4: Testing Raiders API (GET all)..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/raiders?page=0&size=5" -Method GET
    Write-Host "✅ Raiders API accessible" -ForegroundColor Green
    Write-Host "   Total raiders: $($response.totalElements)" -ForegroundColor Cyan
    Write-Host "   Page size: $($response.size)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Raiders API failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Test Raiders API (Search)
Write-Host "Test 5: Testing Raiders search..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/raiders/search?name=Test&realm=TestRealm" -Method GET
    Write-Host "✅ Raiders search endpoint accessible" -ForegroundColor Green
} catch {
    Write-Host "❌ Raiders search failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Phase 1 Testing Complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Open Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor Cyan
Write-Host "2. Try creating a raider via the API" -ForegroundColor Cyan
Write-Host "3. Check audit logs in the database" -ForegroundColor Cyan
Write-Host "4. Proceed to Phase 2 implementation" -ForegroundColor Cyan
