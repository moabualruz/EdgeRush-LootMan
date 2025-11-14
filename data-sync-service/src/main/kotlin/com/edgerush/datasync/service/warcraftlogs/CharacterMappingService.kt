package com.edgerush.datasync.service.warcraftlogs

import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsCharacterMappingEntity
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsCharacterMappingRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CharacterMappingService(
    private val mappingRepository: WarcraftLogsCharacterMappingRepository,
    private val configService: WarcraftLogsConfigService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Resolves a WoWAudit character name to a Warcraft Logs character name
     * Returns the mapped name if exists, otherwise returns the original name
     */
    fun resolveCharacterName(
        guildId: String,
        wowauditName: String,
        wowauditRealm: String,
    ): Pair<String, String> {
        // Check database mapping first
        val mapping =
            mappingRepository.findByGuildAndWoWAuditCharacter(
                guildId,
                wowauditName,
                wowauditRealm,
            )

        if (mapping != null) {
            logger.debug("Found mapping for $wowauditName-$wowauditRealm -> ${mapping.warcraftLogsName}-${mapping.warcraftLogsRealm}")
            return Pair(mapping.warcraftLogsName, mapping.warcraftLogsRealm)
        }

        // Check config-based mappings
        val config = configService.getConfig(guildId)
        val configKey = "$wowauditName-$wowauditRealm"
        val configMapping = config.characterNameMappings[configKey]

        if (configMapping != null) {
            val parts = configMapping.split("-")
            if (parts.size == 2) {
                logger.debug("Found config mapping for $configKey -> $configMapping")
                return Pair(parts[0], parts[1])
            }
        }

        // No mapping found, return original
        logger.debug("No mapping found for $wowauditName-$wowauditRealm, using original")
        return Pair(wowauditName, wowauditRealm)
    }

    /**
     * Creates a new character mapping
     */
    fun createMapping(
        guildId: String,
        wowauditName: String,
        wowauditRealm: String,
        warcraftLogsName: String,
        warcraftLogsRealm: String,
        createdBy: String? = null,
    ) {
        val entity =
            WarcraftLogsCharacterMappingEntity(
                guildId = guildId,
                wowauditName = wowauditName,
                wowauditRealm = wowauditRealm,
                warcraftLogsName = warcraftLogsName,
                warcraftLogsRealm = warcraftLogsRealm,
                createdAt = Instant.now(),
                createdBy = createdBy,
            )

        mappingRepository.save(entity)
        logger.info("Created character mapping: $wowauditName-$wowauditRealm -> $warcraftLogsName-$warcraftLogsRealm")
    }

    /**
     * Gets all mappings for a guild
     */
    fun getMappingsForGuild(guildId: String): List<WarcraftLogsCharacterMappingEntity> {
        return mappingRepository.findByGuildId(guildId)
    }

    /**
     * Gets all mappings for a guild (alias for consistency)
     */
    fun getAllMappings(guildId: String): List<WarcraftLogsCharacterMappingEntity> {
        return getMappingsForGuild(guildId)
    }

    /**
     * Deletes a character mapping
     */
    fun deleteMapping(mappingId: Long) {
        mappingRepository.deleteById(mappingId)
        logger.info("Deleted character mapping: $mappingId")
    }
}
