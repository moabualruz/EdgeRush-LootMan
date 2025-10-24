package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.AttendanceStatEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AttendanceStatRepository : CrudRepository<AttendanceStatEntity, Long>
