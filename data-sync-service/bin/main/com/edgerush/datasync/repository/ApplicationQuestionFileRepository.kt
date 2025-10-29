package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationQuestionFileRepository : CrudRepository<ApplicationQuestionFileEntity, Long> {
    fun deleteByApplicationId(applicationId: Long)
}
