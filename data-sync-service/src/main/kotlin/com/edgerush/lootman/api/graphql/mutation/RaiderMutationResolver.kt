package com.edgerush.lootman.api.graphql.mutation

import com.edgerush.lootman.api.graphql.query.RaiderType
import com.edgerush.lootman.application.raider.CreateRaiderCommand
import com.edgerush.lootman.application.raider.CreateRaiderUseCase
import com.edgerush.lootman.application.raider.DeleteRaiderCommand
import com.edgerush.lootman.application.raider.DeleteRaiderUseCase
import com.edgerush.lootman.application.raider.UpdateRaiderCommand
import com.edgerush.lootman.application.raider.UpdateRaiderUseCase
import com.edgerush.lootman.domain.shared.model.Raider
import com.expediagroup.graphql.server.operations.Mutation
import org.springframework.stereotype.Component

/**
 * GraphQL Mutation resolver for Raider operations.
 *
 * Exposes raider mutations through GraphQL, delegating to the application layer use cases.
 * Provides create, update, and delete operations for raiders.
 */
@Component
class RaiderMutationResolver(
    private val createRaiderUseCase: CreateRaiderUseCase,
    private val updateRaiderUseCase: UpdateRaiderUseCase,
    private val deleteRaiderUseCase: DeleteRaiderUseCase,
) : Mutation {
    /**
     * Create a new raider.
     *
     * @param input The raider creation input
     * @return The created raider
     * @throws RuntimeException on errors
     */
    fun createRaider(input: CreateRaiderInput): RaiderType {
        val command =
            CreateRaiderCommand(
                id = input.id.toLong(),
                characterId = input.characterId.toLong(),
                guildId = input.guildId,
                characterName = input.characterName,
                realm = input.realm,
                region = input.region,
                characterClass = input.characterClass,
                role = input.role,
                rank = input.rank,
                status = input.status ?: "ACTIVE",
                joinDate = null,
                wowauditId = input.wowauditId?.toLong(),
                blizzardId = input.blizzardId?.toLong(),
                accountId = input.accountId?.toLong(),
            )
        return createRaiderUseCase.execute(command)
            .map { it.toGraphQLType() }
            .getOrThrow()
    }

    /**
     * Update an existing raider.
     *
     * @param input The raider update input
     * @return The updated raider
     * @throws RuntimeException on errors
     */
    fun updateRaider(input: UpdateRaiderInput): RaiderType {
        val command =
            UpdateRaiderCommand(
                id = input.id.toLong(),
                characterName = input.characterName,
                realm = input.realm,
                characterClass = input.characterClass,
                role = input.role,
                rank = input.rank,
                status = input.status,
            )
        return updateRaiderUseCase.execute(command)
            .map { it.toGraphQLType() }
            .getOrThrow()
    }

    /**
     * Delete a raider.
     *
     * @param id The raider ID to delete
     * @return True if deletion was successful
     * @throws RuntimeException on errors
     */
    fun deleteRaider(id: String): Boolean {
        val command = DeleteRaiderCommand(id = id.toLong())
        return deleteRaiderUseCase.execute(command)
            .map { true }
            .getOrThrow()
    }
}

/**
 * Input type for creating a raider.
 */
data class CreateRaiderInput(
    val id: String,
    val characterId: String,
    val guildId: String,
    val characterName: String,
    val realm: String,
    val region: String = "eu",
    val characterClass: String,
    val role: String,
    val rank: String? = null,
    val status: String? = null,
    val wowauditId: String? = null,
    val blizzardId: String? = null,
    val accountId: String? = null,
)

/**
 * Input type for updating a raider.
 */
data class UpdateRaiderInput(
    val id: String,
    val characterName: String? = null,
    val realm: String? = null,
    val characterClass: String? = null,
    val role: String? = null,
    val rank: String? = null,
    val status: String? = null,
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
