package com.edgerush.datasync.service.warcraftlogs

import com.edgerush.datasync.config.warcraftlogs.WarcraftLogsGuildConfig
import com.edgerush.datasync.config.warcraftlogs.WarcraftLogsProperties
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsConfigEntity
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsConfigRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class WarcraftLogsConfigService(
    private val configRepository: WarcraftLogsConfigRepository,
    private val encryptionService: CredentialEncryptionService,
    private val properties: WarcraftLogsProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()
    
    fun getConfig(guildId: String): WarcraftLogsGuildConfig {
        val entity = configRepository.findByGuildId(guildId)
        return if (entity != null) {
            parseConfig(entity)
        } else {
            // Return default config if not found
            createDefaultConfig(guildId)
        }
    }
    
    fun updateConfig(guildId: String, config: WarcraftLogsGuildConfig, updatedBy: String? = null) {
        val encryptedClientId = config.clientId?.let { encryptionService.encrypt(it) }
        val encryptedClientSecret = config.clientSecret?.let { encryptionService.encrypt(it) }
        
        val entity = WarcraftLogsConfigEntity(
            guildId = guildId,
            enabled = config.enabled,
            guildName = config.guildName,
            realm = config.realm,
            region = config.region,
            encryptedClientId = encryptedClientId,
            encryptedClientSecret = encryptedClientSecret,
            configJson = objectMapper.writeValueAsString(config),
            updatedAt = Instant.now(),
            updatedBy = updatedBy
        )
        
        configRepository.save(entity)
        logger.info("Updated Warcraft Logs config for guild: $guildId")
    }
    
    fun getEffectiveClientCredentials(guildId: String): ClientCredentials {
        val config = getConfig(guildId)
        
        // Use guild-specific credentials if available, otherwise use system-wide
        val clientId = config.clientId ?: properties.clientId
        val clientSecret = config.clientSecret ?: properties.clientSecret
        
        return ClientCredentials(clientId, clientSecret)
    }
    
    fun getAllEnabledGuilds(): List<WarcraftLogsGuildConfig> {
        return configRepository.findAllEnabled().map { parseConfig(it) }
    }
    
    private fun parseConfig(entity: WarcraftLogsConfigEntity): WarcraftLogsGuildConfig {
        val config = objectMapper.readValue<WarcraftLogsGuildConfig>(entity.configJson)
        
        // Decrypt credentials if present
        val decryptedClientId = entity.encryptedClientId?.let { 
            runCatching { encryptionService.decrypt(it) }.getOrNull()
        }
        val decryptedClientSecret = entity.encryptedClientSecret?.let { 
            runCatching { encryptionService.decrypt(it) }.getOrNull()
        }
        
        return config.copy(
            clientId = decryptedClientId,
            clientSecret = decryptedClientSecret
        )
    }
    
    private fun createDefaultConfig(guildId: String): WarcraftLogsGuildConfig {
        return WarcraftLogsGuildConfig(
            guildId = guildId,
            enabled = false,
            guildName = "",
            realm = "",
            region = "US"
        )
    }
}

data class ClientCredentials(
    val clientId: String,
    val clientSecret: String
)
