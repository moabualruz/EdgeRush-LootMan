package com.edgerush.lootman.api.graphql.query

import com.edgerush.lootman.application.raider.GetRaiderQuery
import com.edgerush.lootman.application.raider.GetRaiderUseCase
import com.edgerush.lootman.application.raider.ListRaidersByGuildQuery
import com.edgerush.lootman.application.raider.ListRaidersUseCase
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component

/**
 * GraphQL Query resolver for Raider operations.
 *
 * Exposes raider queries through GraphQL, delegating to the application layer use cases.
 * Returns GraphQL types that are automatically generated from the RaiderType class.
 */
@Component
class RaiderQueryResolver(
    private val getRaiderUseCase: GetRaiderUseCase,
    private val listRaidersUseCase: ListRaidersUseCase,
) : Query {
    /**
     * Get a single raider by ID.
     *
     * @param id The raider ID as a string
     * @return The raider if found, null otherwise
     * @throws RuntimeException for non-NotFound errors
     */
    fun raider(id: String): RaiderType? {
        val query = GetRaiderQuery(id.toLong())
        return getRaiderUseCase.execute(query)
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
     * Get all raiders for a guild.
     *
     * @param guildId The guild ID
     * @return List of raiders in the guild
     * @throws RuntimeException on errors
     */
    fun raiders(guildId: String): List<RaiderType> {
        val query = ListRaidersByGuildQuery(guildId)
        return listRaidersUseCase.executeByGuild(query)
            .map { raiders -> raiders.map { it.toGraphQLType() } }
            .getOrThrow()
    }
}

/**
 * GraphQL type representing a Raider.
 *
 * This class defines the GraphQL schema for raiders, with fields
 * automatically exposed based on the properties.
 */
data class RaiderType(
    val id: String,
    val guildId: String,
    val characterName: String,
    val realm: String,
    val characterClass: CharacterClass,
    val role: Role,
    val rank: String?,
    val status: RaiderStatus,
    val fullName: String,
    val isEligibleForLoot: Boolean,
)

/**
 * Extension function to convert domain Raider to GraphQL RaiderType.
 */
private fun Raider.toGraphQLType(): RaiderType =
    RaiderType(
        id = this.id.value.toString(),
        guildId = this.guildId.value,
        characterName = this.characterName,
        realm = this.realm,
        characterClass = this.characterClass,
        role = this.role,
        rank = this.rank,
        status = this.status,
        fullName = this.displayName,
        isEligibleForLoot = this.isEligibleForLoot(),
    )
