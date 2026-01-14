package com.edgerush.lootman.domain.character.repository

import com.edgerush.datasync.entity.CharacterHistoryEntity

/**
 * Repository interface for CharacterHistoryEntity CRUD operations.
 *
 * Provides access to character history data at the entity level.
 */
interface CharacterHistoryRepository {
    /**
     * Finds character history by its unique identifier.
     *
     * @param id The character history's unique identifier
     * @return The character history entity if found, null otherwise
     */
    fun findById(id: Long): CharacterHistoryEntity?

    /**
     * Checks if character history exists by ID.
     *
     * @param id The character history's unique identifier
     * @return true if the character history exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all character history with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of character history entities
     */
    fun findAll(offset: Long, limit: Int): List<CharacterHistoryEntity>

    /**
     * Counts all character history.
     *
     * @return The total count of character history
     */
    fun count(): Long

    /**
     * Finds character history by character with pagination.
     *
     * @param characterId The character identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of character history entities for the character
     */
    fun findByCharacterId(characterId: Long, offset: Long, limit: Int): List<CharacterHistoryEntity>

    /**
     * Counts character history for a character.
     *
     * @param characterId The character identifier
     * @return The count of character history for the character
     */
    fun countByCharacterId(characterId: Long): Long

    /**
     * Finds character history by team with pagination.
     *
     * @param teamId The team identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of character history entities for the team
     */
    fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<CharacterHistoryEntity>

    /**
     * Counts character history for a team.
     *
     * @param teamId The team identifier
     * @return The count of character history for the team
     */
    fun countByTeamId(teamId: Long): Long

    /**
     * Saves a character history entity.
     *
     * @param entity The character history to save
     * @return The saved character history entity
     */
    fun save(entity: CharacterHistoryEntity): CharacterHistoryEntity

    /**
     * Deletes character history by ID.
     *
     * @param id The character history ID to delete
     */
    fun delete(id: Long)
}
