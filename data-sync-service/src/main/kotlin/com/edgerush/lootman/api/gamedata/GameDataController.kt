package com.edgerush.lootman.api.gamedata

import com.edgerush.lootman.infrastructure.external.blizzard.BlizzardDataService
import com.edgerush.lootman.infrastructure.external.blizzard.BlizzardMap
import com.edgerush.lootman.infrastructure.external.blizzard.BlizzardRaid
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/game-data")
class GameDataController(
    private val blizzardDataService: BlizzardDataService,
) {
    @Operation(summary = "Get list of raids (Journal Instances)")
    @GetMapping("/raids")
    fun getRaids(): List<BlizzardRaid> {
        return blizzardDataService.getRaids()
    }

    @Operation(summary = "Get maps for a specific raid instance")
    @GetMapping("/raids/{instanceId}/maps")
    fun getRaidMaps(
        @PathVariable instanceId: Int,
    ): List<BlizzardMap> {
        return blizzardDataService.getRaidMaps(instanceId)
    }
}
