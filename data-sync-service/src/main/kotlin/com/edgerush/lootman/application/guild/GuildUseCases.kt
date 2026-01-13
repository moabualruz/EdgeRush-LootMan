package com.edgerush.lootman.application.guild

import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.GuildSettings
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.edgerush.lootman.domain.guild.repository.GuildRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.stereotype.Service
import java.time.Instant

// Commands and Queries

data class CreateGuildCommand(
    val id: String,
    val name: String,
    val description: String?,
    val realm: String?,
    val region: String,
    val syncEnabled: Boolean,
    val syncCronExpression: String,
    val timezone: String,
    val benchmarkMode: String
)

data class UpdateGuildCommand(
    val id: String,
    val name: String?,
    val description: String?,
    val realm: String?,
    val region: String?,
    val syncEnabled: Boolean?,
    val syncCronExpression: String?,
    val timezone: String?,
    val benchmarkMode: String?,
    val customBenchmarkRms: Double?,
    val customBenchmarkIpi: Double?,
    val isActive: Boolean?
)

data class DeleteGuildCommand(val id: String)

data class GetGuildQuery(val id: String)

// Use Cases

@Service
class CreateGuildUseCase(
    private val guildRepository: GuildRepository
) {
    fun execute(command: CreateGuildCommand): Result<Guild> = runCatching {
        val guildId = GuildId(command.id)

        // Check if guild already exists
        if (guildRepository.existsById(guildId)) {
            throw IllegalArgumentException("Guild already exists with id: ${command.id}")
        }

        val region = Region.fromString(command.region)
            ?: throw IllegalArgumentException("Invalid region: ${command.region}")

        val benchmarkMode = BenchmarkMode.fromString(command.benchmarkMode)
            ?: throw IllegalArgumentException("Invalid benchmark mode: ${command.benchmarkMode}")

        val guild = Guild(
            id = guildId,
            name = command.name,
            description = command.description,
            realm = command.realm,
            region = region,
            settings = GuildSettings(
                syncEnabled = command.syncEnabled,
                syncCronExpression = command.syncCronExpression,
                timezone = command.timezone,
                benchmarkMode = benchmarkMode
            ),
            syncStatus = SyncStatus.NEVER_RUN,
            isActive = true,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        guildRepository.save(guild)
    }
}

@Service
class UpdateGuildUseCase(
    private val guildRepository: GuildRepository
) {
    fun execute(command: UpdateGuildCommand): Result<Guild> = runCatching {
        val guildId = GuildId(command.id)

        val existingGuild = guildRepository.findById(guildId)
            ?: throw NoSuchElementException("Guild not found with id: ${command.id}")

        val updatedRegion = command.region?.let {
            Region.fromString(it) ?: throw IllegalArgumentException("Invalid region: $it")
        } ?: existingGuild.region

        val updatedBenchmarkMode = command.benchmarkMode?.let {
            BenchmarkMode.fromString(it) ?: throw IllegalArgumentException("Invalid benchmark mode: $it")
        } ?: existingGuild.settings.benchmarkMode

        val updatedSettings = existingGuild.settings.copy(
            syncEnabled = command.syncEnabled ?: existingGuild.settings.syncEnabled,
            syncCronExpression = command.syncCronExpression ?: existingGuild.settings.syncCronExpression,
            timezone = command.timezone ?: existingGuild.settings.timezone,
            benchmarkMode = updatedBenchmarkMode,
            customBenchmarkRms = command.customBenchmarkRms ?: existingGuild.settings.customBenchmarkRms,
            customBenchmarkIpi = command.customBenchmarkIpi ?: existingGuild.settings.customBenchmarkIpi
        )

        val updatedGuild = existingGuild.copy(
            name = command.name ?: existingGuild.name,
            description = command.description ?: existingGuild.description,
            realm = command.realm ?: existingGuild.realm,
            region = updatedRegion,
            settings = updatedSettings,
            isActive = command.isActive ?: existingGuild.isActive,
            updatedAt = Instant.now()
        )

        guildRepository.save(updatedGuild)
    }
}

@Service
class DeleteGuildUseCase(
    private val guildRepository: GuildRepository
) {
    fun execute(command: DeleteGuildCommand): Result<Unit> = runCatching {
        val guildId = GuildId(command.id)

        if (!guildRepository.deleteById(guildId)) {
            throw NoSuchElementException("Guild not found with id: ${command.id}")
        }
    }
}

@Service
class GetGuildUseCase(
    private val guildRepository: GuildRepository
) {
    fun execute(query: GetGuildQuery): Result<Guild> = runCatching {
        val guildId = GuildId(query.id)

        guildRepository.findById(guildId)
            ?: throw NoSuchElementException("Guild not found with id: ${query.id}")
    }
}

@Service
class ListGuildsUseCase(
    private val guildRepository: GuildRepository
) {
    fun execute(): Result<List<Guild>> = runCatching {
        guildRepository.findAll()
    }

    fun executeActiveOnly(): Result<List<Guild>> = runCatching {
        guildRepository.findAllActive()
    }
}
