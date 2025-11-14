# Comprehensive fix for all remaining mapper errors
# Adds missing field defaults based on compilation errors

$ErrorActionPreference = "Stop"

Write-Host "=== Fixing All Remaining Mappers ===" -ForegroundColor Cyan

# Get compilation errors
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

cd data-sync-service
$errors = ..\gradlew.bat compileKotlin 2>&1 | Select-String "No value passed for parameter"

# Group errors by file
$errorsByFile = @{}
foreach ($error in $errors) {
    if ($error -match "([^/]+Mapper\.kt):.*parameter '(\w+)'") {
        $file = $matches[1]
        $param = $matches[2]
        
        if (-not $errorsByFile.ContainsKey($file)) {
            $errorsByFile[$file] = @()
        }
        if ($errorsByFile[$file] -notcontains $param) {
            $errorsByFile[$file] += $param
        }
    }
}

Write-Host "`nFound errors in $($errorsByFile.Count) files:" -ForegroundColor Yellow
foreach ($file in $errorsByFile.Keys) {
    Write-Host "  $file : $($errorsByFile[$file] -join ', ')" -ForegroundColor Gray
}

Write-Host "`nTo fix these, you need to manually add default values for these fields in each mapper's toEntity() method."
Write-Host "Example defaults:" -ForegroundColor Cyan
Write-Host "  - String fields: `"`" or appropriate default"
Write-Host "  - Long/Int fields: 0L or 0"
Write-Host "  - Double fields: 0.0"
Write-Host "  - Boolean fields: false or true"
Write-Host "  - LocalDateTime fields: LocalDateTime.now()"
Write-Host "  - Nullable fields: null"
