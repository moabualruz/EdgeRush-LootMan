package com.edgerush.lootman.api.guild

import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Implementation of GuildConfigurationCrudService.
 *
 * Provides CRUD operations for guild configurations.
 */
@Service
class GuildConfigurationCrudServiceImpl(
    private val repository: GuildConfigurationRepository,
) : GuildConfigurationCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<GuildConfigurationResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { GuildConfigurationResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): GuildConfigurationResponse {
        val entity =
            repository.findById(id)
                ?: throw NoSuchElementException("Guild configuration not found with id: $id")
        return GuildConfigurationResponse.from(entity)
    }

    override fun findByGuildId(guildId: String): GuildConfigurationResponse {
        val entity =
            repository.findByGuildId(guildId)
                ?: throw NoSuchElementException("Guild configuration not found with guild id: $guildId")
        return GuildConfigurationResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateGuildConfigurationRequest): GuildConfigurationResponse {
        val entity =
            GuildConfigurationEntity(
                guildId = request.guildId,
                guildName = request.guildName,
                guildDescription = request.guildDescription,
                wowauditApiKeyEncrypted = request.wowauditApiKeyEncrypted,
                wowauditGuildUri = request.wowauditGuildUri,
                wowauditBaseUrl = request.wowauditBaseUrl,
                syncEnabled = request.syncEnabled,
                syncCronExpression = request.syncCronExpression,
                syncRunOnStartup = request.syncRunOnStartup,
                lastSyncAt = null,
                lastSyncStatus = null,
                lastSyncError = null,
                timezone = request.timezone,
                isActive = true,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
                benchmarkMode = request.benchmarkMode,
                customBenchmarkRms = request.customBenchmarkRms,
                customBenchmarkIpi = request.customBenchmarkIpi,
                benchmarkUpdatedAt = null,
            )
        val saved = repository.save(entity)
        return GuildConfigurationResponse.from(saved)
    }

    override fun update(
        id: Long,
        request: UpdateGuildConfigurationRequest,
    ): GuildConfigurationResponse {
        val existing =
            repository.findById(id)
                ?: throw NoSuchElementException("Guild configuration not found with id: $id")

        val updated =
            existing.copy(
                guildName = request.guildName ?: existing.guildName,
                guildDescription = request.guildDescription ?: existing.guildDescription,
                wowauditApiKeyEncrypted = request.wowauditApiKeyEncrypted ?: existing.wowauditApiKeyEncrypted,
                wowauditGuildUri = request.wowauditGuildUri ?: existing.wowauditGuildUri,
                wowauditBaseUrl = request.wowauditBaseUrl ?: existing.wowauditBaseUrl,
                syncEnabled = request.syncEnabled ?: existing.syncEnabled,
                syncCronExpression = request.syncCronExpression ?: existing.syncCronExpression,
                syncRunOnStartup = request.syncRunOnStartup ?: existing.syncRunOnStartup,
                timezone = request.timezone ?: existing.timezone,
                isActive = request.isActive ?: existing.isActive,
                updatedAt = OffsetDateTime.now(),
            )

        repository.save(updated)
        return GuildConfigurationResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Guild configuration not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findActive(pageRequest: PageRequest): PagedResponse<GuildConfigurationResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findActive(offset, pageRequest.size)
        val total = repository.countActive()

        return PagedResponse(
            content = entities.map { GuildConfigurationResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun updateBenchmark(
        id: Long,
        request: UpdateBenchmarkRequest,
    ): GuildConfigurationResponse {
        val existing =
            repository.findById(id)
                ?: throw NoSuchElementException("Guild configuration not found with id: $id")

        val updated =
            existing.copy(
                benchmarkMode = request.benchmarkMode ?: existing.benchmarkMode,
                customBenchmarkRms = request.customBenchmarkRms ?: existing.customBenchmarkRms,
                customBenchmarkIpi = request.customBenchmarkIpi ?: existing.customBenchmarkIpi,
                benchmarkUpdatedAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        repository.save(updated)
        return GuildConfigurationResponse.from(updated)
    }

    override fun updateSyncStatus(
        guildId: String,
        status: String,
        error: String?,
    ): GuildConfigurationResponse {
        val existing =
            repository.findByGuildId(guildId)
                ?: throw NoSuchElementException("Guild configuration not found with guild id: $guildId")

        val updated =
            existing.copy(
                lastSyncAt = OffsetDateTime.now(),
                lastSyncStatus = status,
                lastSyncError = error,
                updatedAt = OffsetDateTime.now(),
            )

        repository.save(updated)
        return GuildConfigurationResponse.from(updated)
    }
}
