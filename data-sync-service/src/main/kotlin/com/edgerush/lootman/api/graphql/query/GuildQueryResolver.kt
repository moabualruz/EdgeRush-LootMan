package com.edgerush.lootman.api.graphql.query

import com.edgerush.lootman.application.guild.GetGuildQuery
import com.edgerush.lootman.application.guild.GetGuildUseCase
import com.edgerush.lootman.application.guild.ListGuildsUseCase
import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component

/**
 * GraphQL Query resolver for Guild operations.
 *
 * Exposes guild queries through GraphQL, delegating to the application layer use cases.
 * Returns GraphQL types that are automatically generated from the GuildType class.
 */
@Component
class GuildQueryResolver(
    private val getGuildUseCase: GetGuildUseCase,
    private val listGuildsUseCase: ListGuildsUseCase,
) : Query {

    /**
     * Get a single guild by ID.
     *
     * @param id The guild ID as a string
     * @return The guild if found, null otherwise
     * @throws RuntimeException for non-NotFound errors
     */
    fun guild(id: String): GuildType? {
        val query = GetGuildQuery(id)
        return getGuildUseCase.execute(query)
            .map { it.toGraphQLType() }
            .getOrElse { exception ->
                if (exception is NoSuchElementException) {
                    null
                } else {
                    throw exception
                }
            }
    }

    /**
     * Get all guilds.
     *
     * @return List of all guilds
     * @throws RuntimeException on errors
     */
    fun guilds(): List<GuildType> {
        return listGuildsUseCase.execute()
            .map { guilds -> guilds.map { it.toGraphQLType() } }
            .getOrThrow()
    }
}

/**
 * GraphQL type representing a Guild.
 */
data class GuildType(
    val id: String,
    val name: String,
    val description: String?,
    val realm: String?,
    val region: Region,
    val settings: GuildSettingsType,
    val syncStatus: SyncStatus,
    val isActive: Boolean,
    val canSync: Boolean,
)

/**
 * GraphQL type representing Guild Settings.
 */
data class GuildSettingsType(
    val syncEnabled: Boolean,
    val syncCronExpression: String,
    val timezone: String,
    val benchmarkMode: BenchmarkMode,
    val customBenchmarkRms: Double?,
    val customBenchmarkIpi: Double?,
)

/**
 * Extension function to convert domain Guild to GraphQL GuildType.
 */
private fun Guild.toGraphQLType(): GuildType = GuildType(
    id = this.id.value,
    name = this.name,
    description = this.description,
    realm = this.realm,
    region = this.region,
    settings = GuildSettingsType(
        syncEnabled = this.settings.syncEnabled,
        syncCronExpression = this.settings.syncCronExpression,
        timezone = this.settings.timezone,
        benchmarkMode = this.settings.benchmarkMode,
        customBenchmarkRms = this.settings.customBenchmarkRms,
        customBenchmarkIpi = this.settings.customBenchmarkIpi,
    ),
    syncStatus = this.syncStatus,
    isActive = this.isActive,
    canSync = this.canSync(),
)
