package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaidEncounterEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaidEncounterRepository : CrudRepository<RaidEncounterEntity, Long>
