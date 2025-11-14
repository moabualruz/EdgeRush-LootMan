package com.edgerush.datasync.service.raidbots

import com.edgerush.datasync.config.raidbots.RaidbotsGuildConfig
import com.edgerush.datasync.config.raidbots.RaidbotsProperties
import com.edgerush.datasync.entity.raidbots.RaidbotsConfigEntity
import com.edgerush.datasync.repository.raidbots.RaidbotsConfigRepository
import com.edgerush.datasync.service.warcraftlogs.CredentialEncryptionService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RaidbotsConfigService(
    private val configRepository: RaidbotsConfigRepository,
    private val encryptionService: CredentialEncryptionService,
    private val properties: RaidbotsProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()

    fun getConfig(guildId: String): RaidbotsGuildConfig {
        val entity = configRepository.findById(guildId).orElse(null)
        return if (entity != null) {
            parseConfig(entity)
        } else {
            createDefaultConfig(guildId)
        }
    }

    fun updateConfig(
        guildId: String,
        config: RaidbotsGuildConfig,
    ) {
        val encryptedApiKey = config.apiKey?.let { encryptionService.encrypt(it) }

        val entity =
            RaidbotsConfigEntity(
                guildId = guildId,
                enabled = config.enabled,
                encryptedApiKey = encryptedApiKey,
                configJson = objectMapper.writeValueAsString(config),
                updatedAt = Instant.now(),
            )

        configRepository.save(entity)
        logger.info("Updated Raidbots config for guild: $guildId")
    }

    fun getEffectiveApiKey(guildId: String): String {
        val config = getConfig(guildId)
        return config.apiKey ?: properties.apiKey
    }

    fun getAllEnabledGuilds(): List<RaidbotsGuildConfig> {
        return configRepository.findAllEnabled().map { parseConfig(it) }
    }

    private fun parseConfig(entity: RaidbotsConfigEntity): RaidbotsGuildConfig {
        val config = objectMapper.readValue<RaidbotsGuildConfig>(entity.configJson)
        val decryptedApiKey =
            entity.encryptedApiKey?.let {
                runCatching { encryptionService.decrypt(it) }.getOrNull()
            }
        return config.copy(apiKey = decryptedApiKey)
    }

    private fun createDefaultConfig(guildId: String): RaidbotsGuildConfig {
        return RaidbotsGuildConfig(guildId = guildId, enabled = false)
    }
}
