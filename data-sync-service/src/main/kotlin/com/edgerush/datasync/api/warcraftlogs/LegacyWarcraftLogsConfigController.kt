package com.edgerush.datasync.api.warcraftlogs

import com.edgerush.datasync.config.warcraftlogs.WarcraftLogsGuildConfig
import com.edgerush.datasync.service.warcraftlogs.WarcraftLogsConfigService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/warcraft-logs/config")
class LegacyWarcraftLogsConfigController(
    private val configService: WarcraftLogsConfigService,
    private val characterMappingService: com.edgerush.datasync.service.warcraftlogs.CharacterMappingService,
) {
    @GetMapping("/{guildId}")
    fun getConfig(
        @PathVariable guildId: String,
    ): WarcraftLogsGuildConfig {
        return configService.getConfig(guildId)
    }

    @PutMapping("/{guildId}")
    fun updateConfig(
        @PathVariable guildId: String,
        @RequestBody config: WarcraftLogsGuildConfig,
    ): WarcraftLogsGuildConfig {
        configService.updateConfig(guildId, config)
        return configService.getConfig(guildId)
    }

    @PostMapping("/{guildId}/character-mapping")
    fun createCharacterMapping(
        @PathVariable guildId: String,
        @RequestBody request: CharacterMappingRequest,
    ): Map<String, String> {
        characterMappingService.createMapping(
            guildId = guildId,
            wowauditName = request.wowauditName,
            wowauditRealm = request.wowauditRealm,
            warcraftLogsName = request.warcraftLogsName,
            warcraftLogsRealm = request.warcraftLogsRealm,
            createdBy = request.createdBy,
        )
        return mapOf("status" to "success", "message" to "Character mapping created")
    }

    @GetMapping("/{guildId}/character-mappings")
    fun getCharacterMappings(
        @PathVariable guildId: String,
    ): List<CharacterMappingResponse> {
        return characterMappingService.getMappingsForGuild(guildId).map {
            CharacterMappingResponse(
                id = it.id!!,
                wowauditName = it.wowauditName,
                wowauditRealm = it.wowauditRealm,
                warcraftLogsName = it.warcraftLogsName,
                warcraftLogsRealm = it.warcraftLogsRealm,
                createdAt = it.createdAt.toString(),
            )
        }
    }

    @DeleteMapping("/{guildId}/character-mapping/{mappingId}")
    fun deleteCharacterMapping(
        @PathVariable guildId: String,
        @PathVariable mappingId: Long,
    ): Map<String, String> {
        characterMappingService.deleteMapping(mappingId)
        return mapOf("status" to "success", "message" to "Character mapping deleted")
    }
}

data class CharacterMappingRequest(
    val wowauditName: String,
    val wowauditRealm: String,
    val warcraftLogsName: String,
    val warcraftLogsRealm: String,
    val createdBy: String? = null,
)

data class CharacterMappingResponse(
    val id: Long,
    val wowauditName: String,
    val wowauditRealm: String,
    val warcraftLogsName: String,
    val warcraftLogsRealm: String,
    val createdAt: String,
)
