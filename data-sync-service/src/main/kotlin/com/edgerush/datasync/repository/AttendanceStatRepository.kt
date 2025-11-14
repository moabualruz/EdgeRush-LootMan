package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.AttendanceStatEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AttendanceStatRepository : CrudRepository<AttendanceStatEntity, Long>, org.springframework.data.repository.PagingAndSortingRepository<AttendanceStatEntity, Long> {
    fun findByCharacterId(characterId: Long): List<AttendanceStatEntity>
    fun findByCharacterName(characterName: String): List<AttendanceStatEntity>
}
