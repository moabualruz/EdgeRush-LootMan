package com.edgerush.datasync.service.crud

import com.edgerush.datasync.security.AuthenticatedUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CrudService<T : Any, ID : Any, CreateReq : Any, UpdateReq : Any, Resp : Any> {
    /**
     * Find all entities with pagination
     */
    fun findAll(pageable: Pageable): Page<Resp>

    /**
     * Find entity by ID
     * @throws ResourceNotFoundException if not found
     */
    fun findById(id: ID): Resp

    /**
     * Create new entity
     * @throws AccessDeniedException if user doesn't have permission
     */
    fun create(
        request: CreateReq,
        user: AuthenticatedUser,
    ): Resp

    /**
     * Update existing entity
     * @throws ResourceNotFoundException if not found
     * @throws AccessDeniedException if user doesn't have permission
     */
    fun update(
        id: ID,
        request: UpdateReq,
        user: AuthenticatedUser,
    ): Resp

    /**
     * Delete entity
     * @throws ResourceNotFoundException if not found
     * @throws AccessDeniedException if user doesn't have permission
     */
    fun delete(
        id: ID,
        user: AuthenticatedUser,
    )

    /**
     * Validate user has access to the entity
     * @throws AccessDeniedException if user doesn't have permission
     */
    fun validateAccess(
        entity: T,
        user: AuthenticatedUser,
    )
}
