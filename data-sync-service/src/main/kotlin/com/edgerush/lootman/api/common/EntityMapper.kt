package com.edgerush.lootman.api.common

/**
 * Generic mapper interface for converting between entities and DTOs.
 *
 * Provides a consistent pattern for entity-to-DTO conversion across all API endpoints.
 *
 * @param Entity The database entity type
 * @param CreateReq The request DTO type for creating entities
 * @param UpdateReq The request DTO type for updating entities
 * @param Resp The response DTO type
 */
interface EntityMapper<Entity, CreateReq, UpdateReq, Resp> {
    /**
     * Convert a create request DTO to a new entity.
     *
     * @param request The creation request DTO
     * @return A new entity instance (typically without ID set)
     */
    fun toEntity(request: CreateReq): Entity

    /**
     * Update an existing entity with values from an update request.
     *
     * @param entity The existing entity to update
     * @param request The update request DTO with new values
     * @return The updated entity (may be the same instance or a copy)
     */
    fun updateEntity(
        entity: Entity,
        request: UpdateReq,
    ): Entity

    /**
     * Convert an entity to a response DTO.
     *
     * @param entity The entity to convert
     * @return The response DTO representation
     */
    fun toResponse(entity: Entity): Resp

    /**
     * Convert multiple entities to response DTOs.
     *
     * Default implementation maps each entity individually.
     *
     * @param entities The list of entities to convert
     * @return List of response DTOs
     */
    fun toResponseList(entities: List<Entity>): List<Resp> = entities.map { toResponse(it) }
}

/**
 * Extended mapper interface that supports creating entities with a guild context.
 *
 * Use this when entities are guild-scoped and need the guild ID during creation.
 *
 * @param Entity The database entity type
 * @param CreateReq The request DTO type for creating entities
 * @param UpdateReq The request DTO type for updating entities
 * @param Resp The response DTO type
 */
interface GuildScopedEntityMapper<Entity, CreateReq, UpdateReq, Resp> :
    EntityMapper<Entity, CreateReq, UpdateReq, Resp> {
    /**
     * Convert a create request DTO to a new entity with guild context.
     *
     * @param request The creation request DTO
     * @param guildId The guild identifier for the new entity
     * @return A new entity instance with guild association
     */
    fun toEntityWithGuild(
        request: CreateReq,
        guildId: String,
    ): Entity
}

/**
 * Extended mapper interface that supports creating entities with a raider context.
 *
 * Use this when entities are raider-scoped and need the raider ID during creation.
 *
 * @param Entity The database entity type
 * @param CreateReq The request DTO type for creating entities
 * @param UpdateReq The request DTO type for updating entities
 * @param Resp The response DTO type
 */
interface RaiderScopedEntityMapper<Entity, CreateReq, UpdateReq, Resp> :
    EntityMapper<Entity, CreateReq, UpdateReq, Resp> {
    /**
     * Convert a create request DTO to a new entity with raider context.
     *
     * @param request The creation request DTO
     * @param raiderId The raider identifier for the new entity
     * @return A new entity instance with raider association
     */
    fun toEntityWithRaider(
        request: CreateReq,
        raiderId: Long,
    ): Entity
}
