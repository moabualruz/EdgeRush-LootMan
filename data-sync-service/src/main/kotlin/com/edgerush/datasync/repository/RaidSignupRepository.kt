package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaidSignupEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaidSignupRepository : CrudRepository<RaidSignupEntity, Long>
