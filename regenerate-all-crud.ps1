# Regenerate All CRUD APIs
# This script rebuilds the CRUD generator and regenerates all 45 entity APIs

$JDK21_PATH = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"

# Set JAVA_HOME
$env:JAVA_HOME = $JDK21_PATH
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "=== Regenerating All CRUD APIs ===" -ForegroundColor Cyan
Write-Host "Using JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
Write-Host ""

# Step 1: Build the CRUD generator
Write-Host "Step 1: Building CRUD generator..." -ForegroundColor Yellow
Push-Location crud-generator
& .\gradlew.bat build -x test --quiet
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ CRUD generator build failed" -ForegroundColor Red
    Pop-Location
    exit 1
}
Pop-Location
Write-Host "✅ CRUD generator built successfully" -ForegroundColor Green
Write-Host ""

# Step 2: Run batch generation
Write-Host "Step 2: Generating all CRUD APIs..." -ForegroundColor Yellow
$entitiesDir = "data-sync-service\src\main\kotlin\com\edgerush\datasync\entity"
$outputDir = "data-sync-service\src\main\kotlin"

Push-Location crud-generator
& .\gradlew.bat run --args="generate-batch --entities-dir=..\$entitiesDir --output=..\$outputDir --package=com.edgerush.datasync"
$genResult = $LASTEXITCODE
Pop-Location

if ($genResult -ne 0) {
    Write-Host "❌ Generation failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== Generation Complete ===" -ForegroundColor Cyan
Write-Host ""

# Step 3: Build the main project
Write-Host "Step 3: Building main project..." -ForegroundColor Yellow
& .\gradlew.bat :data-sync-service:build -x test
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build successful!" -ForegroundColor Green
} else {
    Write-Host "❌ Build failed - check errors above" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== All Done! ===" -ForegroundColor Green
Write-Host "All 45 CRUD APIs have been regenerated and the project builds successfully." -ForegroundColor Cyan
