package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.HistoricalActivityEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HistoricalActivityRepository : CrudRepository<HistoricalActivityEntity, Long>
