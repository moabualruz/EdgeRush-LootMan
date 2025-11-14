# Fast CRUD Error Fix Script
# Fixes all systematic compilation errors in one pass

$JDK21_PATH = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"
$env:JAVA_HOME = $JDK21_PATH
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "=== Fast CRUD Error Fix ===" -ForegroundColor Cyan
Write-Host ""

# Fix 1: Add missing imports to all Request files
Write-Host "Fixing Request DTOs..." -ForegroundColor Yellow
Get-ChildItem "data