package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.ApplicationAltEntity
import com.edgerush.datasync.entity.ApplicationEntity
import com.edgerush.datasync.entity.ApplicationQuestionEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : CrudRepository<ApplicationEntity, Long>

@Repository
interface ApplicationAltRepository : CrudRepository<ApplicationAltEntity, Long>

@Repository
interface ApplicationQuestionRepository : CrudRepository<ApplicationQuestionEntity, Long>
