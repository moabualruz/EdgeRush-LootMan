package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.TeamRaidDayEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamRaidDayRepository : CrudRepository<TeamRaidDayEntity, Long> {
    fun deleteByTeamId(teamId: Long)
}
