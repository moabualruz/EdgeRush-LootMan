package com.edgerush.lootman.domain.snapshot.repository

import com.edgerush.datasync.entity.PeriodSnapshotEntity

interface PeriodSnapshotRepository {
    fun findById(id: Long): PeriodSnapshotEntity?

    fun existsById(id: Long): Boolean

    fun findAll(
        offset: Long,
        limit: Int,
    ): List<PeriodSnapshotEntity>

    fun count(): Long

    fun findByTeamId(
        teamId: Long,
        offset: Long,
        limit: Int,
    ): List<PeriodSnapshotEntity>

    fun countByTeamId(teamId: Long): Long

    fun save(entity: PeriodSnapshotEntity): PeriodSnapshotEntity

    fun delete(id: Long)
}
