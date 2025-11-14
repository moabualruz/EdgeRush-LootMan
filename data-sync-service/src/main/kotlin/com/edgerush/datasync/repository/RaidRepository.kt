package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaidEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RaidRepository : CrudRepository<RaidEntity, Long> {
    /**
     * Find all raids by date.
     * 
     * @param date The date to search for
     * @return List of raids on the specified date
     */
    fun findByDate(date: LocalDate): List<RaidEntity>
}
