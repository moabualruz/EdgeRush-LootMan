# Test script with JDK 21
# JAVA_HOME is configured in gradle.properties
# This script ensures environment variables are set for the current session

$JDK21_PATH = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"

# Check if JDK 21 exists
if (-not (Test-Path $JDK21_PATH)) {
    Write-Host "ERROR: JDK 21 not found at: $JDK21_PATH" -ForegroundColor Red
    Write-Host "Please update the JDK21_PATH variable in this script to point to your JDK 21 installation" -ForegroundColor Yellow
    exit 1
}

# Set JAVA_HOME for this session
$env:JAVA_HOME = $JDK21_PATH
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Using JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green

# Verify Java version
Write-Host "`nJava version:" -ForegroundColor Cyan
& java -version

Write-Host "`n=== Running tests for data-sync-service ===" -ForegroundColor Cyan
& gradlew.bat :data-sync-service:test

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✓ Tests passed!" -ForegroundColor Green
} else {
    Write-Host "`n✗ Tests failed with exit code: $LASTEXITCODE" -ForegroundColor Red
}
