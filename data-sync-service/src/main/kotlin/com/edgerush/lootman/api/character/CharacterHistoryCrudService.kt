package com.edgerush.lootman.api.character

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for CharacterHistory entity operations.
 *
 * Extends the generic CrudService with character-history-specific query methods.
 */
interface CharacterHistoryCrudService : CrudService<Long, CreateCharacterHistoryRequest, UpdateCharacterHistoryRequest, CharacterHistoryResponse> {

    /**
     * Find character history by character with pagination.
     *
     * @param characterId The character identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of character history for the character
     */
    fun findByCharacterId(characterId: Long, pageRequest: PageRequest): PagedResponse<CharacterHistoryResponse>

    /**
     * Find character history by team with pagination.
     *
     * @param teamId The team identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of character history for the team
     */
    fun findByTeamId(teamId: Long, pageRequest: PageRequest): PagedResponse<CharacterHistoryResponse>

    /**
     * Count character history for a character.
     *
     * @param characterId The character identifier
     * @return The count of character history for the character
     */
    fun countByCharacterId(characterId: Long): Long
}
