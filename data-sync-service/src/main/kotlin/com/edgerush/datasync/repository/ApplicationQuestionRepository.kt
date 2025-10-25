package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.ApplicationQuestionEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationQuestionRepository : CrudRepository<ApplicationQuestionEntity, Long>

