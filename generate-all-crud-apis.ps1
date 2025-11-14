# PowerShell script to generate CRUD APIs for all remaining entities
# This script reads entity files and generates DTOs, Mappers, Services, and Controllers

$entities = @(
    @{Name="FlpsGuildModifier"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="LootBan"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildIdAndIsActive", "findByCharacterName")},
    @{Name="GuildConfiguration"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="RaiderGearItem"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="RaiderVaultSlot"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="RaiderCrestCount"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="RaiderRaidProgress"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="RaiderTrackItem"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="RaiderPvpBracket"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="RaiderRenown"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="RaiderStatistics"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="Application"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId", "findByStatus")},
    @{Name="ApplicationAlt"; Package=""; GuildScoped=$false; CustomMethods=@("findByApplicationId")},
    @{Name="ApplicationQuestion"; Package=""; GuildScoped=$false; CustomMethods=@("findByApplicationId")},
    @{Name="ApplicationQuestionFile"; Package=""; GuildScoped=$false; CustomMethods=@("findByQuestionId")},
    @{Name="WishlistSnapshot"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="WarcraftLogsConfig"; Package="warcraftlogs"; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="WarcraftLogsReport"; Package="warcraftlogs"; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="WarcraftLogsFight"; Package="warcraftlogs"; GuildScoped=$false; CustomMethods=@("findByReportId")},
    @{Name="WarcraftLogsPerformance"; Package="warcraftlogs"; GuildScoped=$false; CustomMethods=@("findByCharacterName")},
    @{Name="WarcraftLogsCharacterMapping"; Package="warcraftlogs"; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="RaidbotsConfig"; Package="raidbots"; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="RaidbotsSimulation"; Package="raidbots"; GuildScoped=$false; CustomMethods=@("findByCharacterName")},
    @{Name="RaidbotsResult"; Package="raidbots"; GuildScoped=$false; CustomMethods=@("findBySimulationId")},
    @{Name="SyncRun"; Package=""; GuildScoped=$false; CustomMethods=@("findByStatus")},
    @{Name="PeriodSnapshot"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="WoWAuditSnapshot"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="TeamMetadata"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="TeamRaidDay"; Package=""; GuildScoped=$false; CustomMethods=@("findByTeamId")},
    @{Name="RaidEncounter"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaidId")},
    @{Name="RaidSignup"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaidId", "findByCharacterName")},
    @{Name="Guest"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="HistoricalActivity"; Package=""; GuildScoped=$true; CustomMethods=@("findByGuildId")},
    @{Name="CharacterHistory"; Package=""; GuildScoped=$false; CustomMethods=@("findByCharacterName")},
    @{Name="RaiderWarcraftLog"; Package=""; GuildScoped=$false; CustomMethods=@("findByRaiderId")},
    @{Name="LootAwardBonusId"; Package=""; GuildScoped=$false; CustomMethods=@("findByLootAwardId")},
    @{Name="LootAwardOldItem"; Package=""; GuildScoped=$false; CustomMethods=@("findByLootAwardId")},
    @{Name="LootAwardWishData"; Package=""; GuildScoped=$false; CustomMethods=@("findByLootAwardId")}
)

Write-Host "This script will generate CRUD APIs for $($entities.Count) entities" -ForegroundColor Cyan
Write-Host "Each entity will get 5 files: Request DTOs, Response DTO, Mapper, Service, Controller" -ForegroundColor Cyan
Write-Host ""
Write-Host "Total files to generate: $($entities.Count * 5) = $($entities.Count * 5)" -ForegroundColor Yellow
Write-Host ""
Write-Host "This is a template script. Actual generation requires reading entity files and extracting fields." -ForegroundColor Green
Write-Host "For now, use this as a reference for which entities need APIs." -ForegroundColor Green
Write-Host ""

foreach ($entity in $entities) {
    Write-Host "Entity: $($entity.Name)" -ForegroundColor White
    Write-Host "  Package: $($entity.Package)" -ForegroundColor Gray
    Write-Host "  Guild Scoped: $($entity.GuildScoped)" -ForegroundColor Gray
    Write-Host "  Custom Methods: $($entity.CustomMethods -join ', ')" -ForegroundColor Gray
    Write-Host ""
}

Write-Host "To implement these, follow the pattern from existing entities like Raider or LootAward" -ForegroundColor Cyan
