package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.GuestEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface GuestRepository : CrudRepository<GuestEntity, Long>
