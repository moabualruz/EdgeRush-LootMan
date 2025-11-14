# Apply fixes to all remaining mappers
$ErrorActionPreference = "Stop"

Write-Host "=== Applying Mapper Fixes ===" -ForegroundColor Cyan

# Define fixes for each mapper
$fixes = @{
    "RaiderVaultSlotMapper.kt" = @"
            raiderId = 0L, // System populated
            slot = 0, // System populated
            unlocked = false // System populated
"@
    
    "RaiderWarcraftLogMapper.kt" = @"
            raiderId = 0L, // System populated
            difficulty = "", // System populated
            score = 0.0 // System populated
"@
    
    "RaiderTrackItemMapper.kt" = @"
            raiderId = 0L, // System populated
            tier = "", // System populated
            itemCount = 0 // System populated
"@
    
    "RaiderRenownMapper.kt" = @"
            raiderId = 0L, // System populated
            faction = "", // System populated
            level = 0 // System populated
"@
    
    "RaiderPvpBracketMapper.kt" = @"
            raiderId = 0L, // System populated
            bracket = "", // System populated
            rating = 0, // System populated
            seasonPlayed = 0, // System populated
            weekPlayed = 0, // System populated
            maxRating = 0 // System populated
"@
    
    "RaiderRaidProgressMapper.kt" = @"
            raiderId = 0L, // System populated
            raid = "", // System populated
            difficulty = "", // System populated
            bossesDefeated = 0 // System populated
"@
}

$basePath = "data-sync-service\src\main\kotlin\com\edgerush\datasync\service\mapper"

foreach ($file in $fixes.Keys) {
    $filePath = Join-Path $basePath $file
    if (Test-Path $filePath) {
        Write-Host "Fixing: $file" -ForegroundColor Yellow
        
        $content = Get-Content $filePath -Raw
        
        # Replace the empty toEntity body
        $content = $content -replace "(\s+id = null,)\s+\)", "`$1`n$($fixes[$file])`n        )"
        
        Set-Content -Path $filePath -Value $content -NoNewline
        Write-Host "  ✓ Fixed" -ForegroundColor Green
    }
}

Write-Host "`n=== Done ===" -ForegroundColor Cyan
