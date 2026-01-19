package com.edgerush.lootman.infrastructure.springdata

import com.edgerush.datasync.entity.RecruitmentCommentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data JDBC repository for RecruitmentCommentEntity.
 *
 * Provides automatic CRUD operations and custom query methods.
 */
@Repository
interface RecruitmentCommentEntitySpringRepository :
    CrudRepository<RecruitmentCommentEntity, Long>,
    PagingAndSortingRepository<RecruitmentCommentEntity, Long> {

    fun findByApplicationId(applicationId: String, pageable: Pageable): Page<RecruitmentCommentEntity>

    fun countByApplicationId(applicationId: String): Long

    fun findByApplicationId(applicationId: String): List<RecruitmentCommentEntity>

    fun findByAuthorId(authorId: Long, pageable: Pageable): Page<RecruitmentCommentEntity>

    fun countByAuthorId(authorId: Long): Long

    fun deleteByApplicationId(applicationId: String)
}
