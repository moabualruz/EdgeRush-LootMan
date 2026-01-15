package com.edgerush.lootman.api.flps

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for FlpsDefaultModifier operations.
 */
interface FlpsDefaultModifierCrudService : CrudService<Long, CreateFlpsDefaultModifierRequest, UpdateFlpsDefaultModifierRequest, FlpsDefaultModifierResponse> {
    /**
     * Find default modifiers by category with pagination.
     *
     * @param category The modifier category (e.g., "rms", "ipi")
     * @param pageRequest Pagination parameters
     * @return Paginated list of modifiers for the category
     */
    fun findByCategory(
        category: String,
        pageRequest: PageRequest,
    ): PagedResponse<FlpsDefaultModifierResponse>
}

/**
 * CRUD service interface for FlpsGuildModifier operations.
 */
interface FlpsGuildModifierCrudService : CrudService<Long, CreateFlpsGuildModifierRequest, UpdateFlpsGuildModifierRequest, FlpsGuildModifierResponse> {
    /**
     * Find guild modifiers by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param pageRequest Pagination parameters
     * @return Paginated list of modifiers for the guild
     */
    fun findByGuild(
        guildId: String,
        pageRequest: PageRequest,
    ): PagedResponse<FlpsGuildModifierResponse>

    /**
     * Find guild modifiers by guild and category with pagination.
     *
     * @param guildId The guild identifier
     * @param category The modifier category
     * @param pageRequest Pagination parameters
     * @return Paginated list of modifiers for the guild and category
     */
    fun findByGuildAndCategory(
        guildId: String,
        category: String,
        pageRequest: PageRequest,
    ): PagedResponse<FlpsGuildModifierResponse>

    /**
     * Count modifiers for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of modifiers for the guild
     */
    fun countByGuild(guildId: String): Long
}
