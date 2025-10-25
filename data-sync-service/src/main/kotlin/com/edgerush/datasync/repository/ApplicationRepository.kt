package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.ApplicationEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : CrudRepository<ApplicationEntity, Long>

