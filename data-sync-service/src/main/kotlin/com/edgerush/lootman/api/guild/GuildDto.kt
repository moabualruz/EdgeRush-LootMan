package com.edgerush.lootman.api.guild

import com.edgerush.lootman.domain.guild.model.Guild

/**
 * Request DTO for creating a guild.
 */
data class CreateGuildRequest(
    val id: String,
    val name: String,
    val description: String? = null,
    val realm: String? = null,
    val region: String = "US",
    val syncEnabled: Boolean = true,
    val syncCronExpression: String = "0 0 4 * * *",
    val timezone: String = "UTC",
    val benchmarkMode: String = "THEORETICAL"
)

/**
 * Request DTO for updating a guild.
 */
data class UpdateGuildRequest(
    val name: String? = null,
    val description: String? = null,
    val realm: String? = null,
    val region: String? = null,
    val syncEnabled: Boolean? = null,
    val syncCronExpression: String? = null,
    val timezone: String? = null,
    val benchmarkMode: String? = null,
    val customBenchmarkRms: Double? = null,
    val customBenchmarkIpi: Double? = null,
    val isActive: Boolean? = null
)

/**
 * Response DTO for a guild.
 */
data class GuildResponse(
    val id: String,
    val name: String,
    val description: String?,
    val realm: String?,
    val region: String,
    val syncEnabled: Boolean,
    val syncCronExpression: String,
    val timezone: String,
    val benchmarkMode: String,
    val customBenchmarkRms: Double?,
    val customBenchmarkIpi: Double?,
    val syncStatus: String,
    val isActive: Boolean,
    val canSync: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(guild: Guild): GuildResponse = GuildResponse(
            id = guild.id.value,
            name = guild.name,
            description = guild.description,
            realm = guild.realm,
            region = guild.region.name,
            syncEnabled = guild.settings.syncEnabled,
            syncCronExpression = guild.settings.syncCronExpression,
            timezone = guild.settings.timezone,
            benchmarkMode = guild.settings.benchmarkMode.name,
            customBenchmarkRms = guild.settings.customBenchmarkRms,
            customBenchmarkIpi = guild.settings.customBenchmarkIpi,
            syncStatus = guild.syncStatus.name,
            isActive = guild.isActive,
            canSync = guild.canSync(),
            createdAt = guild.createdAt.toString(),
            updatedAt = guild.updatedAt.toString()
        )
    }
}

/**
 * Response DTO for a list of guilds.
 */
data class GuildListResponse(
    val guilds: List<GuildResponse>,
    val count: Int
) {
    companion object {
        fun from(guilds: List<Guild>): GuildListResponse = GuildListResponse(
            guilds = guilds.map { GuildResponse.from(it) },
            count = guilds.size
        )
    }
}
