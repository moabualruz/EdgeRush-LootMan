package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.entity.RaidSignupEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaidRepository : CrudRepository<RaidEntity, Long>

@Repository
interface RaidSignupRepository : CrudRepository<RaidSignupEntity, Long>

@Repository
interface RaidEncounterRepository : CrudRepository<RaidEncounterEntity, Long>
