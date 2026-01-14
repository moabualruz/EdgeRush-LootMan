package com.edgerush.lootman.domain.auth.repository

import com.edgerush.lootman.domain.auth.model.User
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.shared.GuildId

/**
 * Repository interface for User operations.
 */
interface UserRepository {

    /**
     * Finds a user by their unique identifier.
     *
     * @param id The user's unique identifier
     * @return The user if found, null otherwise
     */
    fun findById(id: UserId): User?

    /**
     * Finds a user by their Discord ID.
     *
     * @param discordId The Discord user's ID
     * @return The user if found, null otherwise
     */
    fun findByDiscordId(discordId: String): User?

    /**
     * Finds a user by their Battle.net ID.
     *
     * @param battlenetId The Battle.net user's ID
     * @return The user if found, null otherwise
     */
    fun findByBattlenetId(battlenetId: String): User?

    /**
     * Finds all users in a guild.
     *
     * @param guildId The guild's identifier
     * @return List of users in the guild
     */
    fun findByGuildId(guildId: GuildId): List<User>

    /**
     * Saves a user (creates or updates).
     *
     * @param user The user to save
     * @return The saved user with ID assigned
     */
    fun save(user: User): User

    /**
     * Deletes a user by their ID.
     *
     * @param id The user ID to delete
     */
    fun deleteById(id: UserId)

    /**
     * Checks if a user exists with the given Discord ID.
     *
     * @param discordId The Discord user's ID
     * @return true if user exists, false otherwise
     */
    fun existsByDiscordId(discordId: String): Boolean

    /**
     * Checks if a user exists with the given Battle.net ID.
     *
     * @param battlenetId The Battle.net user's ID
     * @return true if user exists, false otherwise
     */
    fun existsByBattlenetId(battlenetId: String): Boolean

    /**
     * Finds all users with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return Paginated list of users
     */
    fun findAll(offset: Long, limit: Int): List<User>

    /**
     * Counts all users.
     *
     * @return The total number of users
     */
    fun count(): Long
}
