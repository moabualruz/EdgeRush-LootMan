package com.edgerush.lootman.api.common

/**
 * Generic CRUD service interface for REST API operations.
 *
 * Provides standard create, read, update, delete operations for entities.
 *
 * @param ID The type of the entity identifier
 * @param CreateReq The request type for creating entities
 * @param UpdateReq The request type for updating entities
 * @param Resp The response type for entity representation
 */
interface CrudService<ID, CreateReq, UpdateReq, Resp> {
    /**
     * Find all entities with pagination.
     *
     * @param pageRequest Pagination parameters
     * @return Paged response containing entities
     */
    fun findAll(pageRequest: PageRequest): PagedResponse<Resp>

    /**
     * Find an entity by its identifier.
     *
     * @param id The entity identifier
     * @return The entity response
     * @throws NoSuchElementException if entity not found
     */
    fun findById(id: ID): Resp

    /**
     * Create a new entity.
     *
     * @param request The creation request
     * @return The created entity response
     */
    fun create(request: CreateReq): Resp

    /**
     * Update an existing entity.
     *
     * @param id The entity identifier
     * @param request The update request
     * @return The updated entity response
     * @throws NoSuchElementException if entity not found
     */
    fun update(
        id: ID,
        request: UpdateReq,
    ): Resp

    /**
     * Delete an entity by its identifier.
     *
     * @param id The entity identifier
     * @throws NoSuchElementException if entity not found
     */
    fun delete(id: ID)

    /**
     * Check if an entity exists by its identifier.
     *
     * @param id The entity identifier
     * @return true if entity exists, false otherwise
     */
    fun existsById(id: ID): Boolean
}

/**
 * Extended CRUD service that supports guild-based filtering.
 *
 * @param ID The type of the entity identifier
 * @param CreateReq The request type for creating entities
 * @param UpdateReq The request type for updating entities
 * @param Resp The response type for entity representation
 */
interface GuildScopedCrudService<ID, CreateReq, UpdateReq, Resp> : CrudService<ID, CreateReq, UpdateReq, Resp> {
    /**
     * Find all entities for a specific guild with pagination.
     *
     * @param guildId The guild identifier
     * @param pageRequest Pagination parameters
     * @return Paged response containing entities for the guild
     */
    fun findByGuild(
        guildId: String,
        pageRequest: PageRequest,
    ): PagedResponse<Resp>

    /**
     * Count entities for a specific guild.
     *
     * @param guildId The guild identifier
     * @return The count of entities for the guild
     */
    fun countByGuild(guildId: String): Long
}

/**
 * Extended CRUD service that supports raider-based filtering.
 *
 * @param ID The type of the entity identifier
 * @param CreateReq The request type for creating entities
 * @param UpdateReq The request type for updating entities
 * @param Resp The response type for entity representation
 */
interface RaiderScopedCrudService<ID, CreateReq, UpdateReq, Resp> : CrudService<ID, CreateReq, UpdateReq, Resp> {
    /**
     * Find all entities for a specific raider with pagination.
     *
     * @param raiderId The raider identifier
     * @param pageRequest Pagination parameters
     * @return Paged response containing entities for the raider
     */
    fun findByRaider(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<Resp>

    /**
     * Count entities for a specific raider.
     *
     * @param raiderId The raider identifier
     * @return The count of entities for the raider
     */
    fun countByRaider(raiderId: Long): Long
}
