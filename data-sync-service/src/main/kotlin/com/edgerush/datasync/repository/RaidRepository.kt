package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaidEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaidRepository : CrudRepository<RaidEntity, Long>

