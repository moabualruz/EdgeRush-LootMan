package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.ApplicationAltEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationAltRepository : CrudRepository<ApplicationAltEntity, Long>

