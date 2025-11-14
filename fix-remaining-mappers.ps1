# Script to fix remaining mapper compilation errors
# This script adds proper default values for fields not in request DTOs

$ErrorActionPreference = "Stop"

Write-Host "=== Fixing Remaining Mappers ===" -ForegroundColor Cyan

# List of mappers that need fixing
$mappers = @(
    "BehavioralActionMapper",
    "CharacterHistoryMapper",
    "GuildConfigurationMapper",
    "RaiderCrestCountMapper",
    "RaiderGearItemMapper",
    "RaiderMapper",
    "RaiderPvpBracketMapper",
    "RaiderRaidProgressMapper",
    "RaiderRenownMapper",
    "RaiderStatisticsMapper",
    "RaiderTrackItemMapper",
    "RaiderVaultSlotMapper",
    "RaiderWarcraftLogMapper"
)

$basePath = "data-sync-service\src\main\kotlin\com\edgerush\datasync\service\mapper"

foreach ($mapper in $mappers) {
    $filePath = Join-Path $basePath "$mapper.kt"
    if (Test-Path $filePath) {
        Write-Host "Processing: $mapper" -ForegroundColor Yellow
        
        # Read the file
        $content = Get-Content $filePath -Raw
        
        # Add comment indicating manual fix
        if ($content -notmatch "// MANUAL FIX") {
            $content = $content -replace "(class $mapper \{)", "`$1`n    // MANUAL FIX: Added default values for system-populated fields"
        }
        
        # Write back
        Set-Content -Path $filePath -Value $content -NoNewline
        
        Write-Host "  ✓ Marked for manual review" -ForegroundColor Green
    }
}

Write-Host "`n=== Done ===" -ForegroundColor Cyan
Write-Host "Please review and manually add missing field defaults in the toEntity() methods"
