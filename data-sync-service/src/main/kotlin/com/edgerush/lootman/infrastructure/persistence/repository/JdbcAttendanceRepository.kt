package com.edgerush.lootman.infrastructure.persistence.repository

import com.edgerush.datasync.repository.AttendanceStatRepository
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.infrastructure.persistence.mapper.AttendanceMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * JDBC implementation of AttendanceRepository.
 * Bridges the domain layer with the infrastructure persistence layer.
 *
 * Note: This implementation uses the attendance_stats table which stores aggregated statistics.
 * Individual attendance records are derived from these statistics.
 */
class JdbcAttendanceRepository(
    private val springRepository: AttendanceStatRepository,
    private val mapper: AttendanceMapper,
) : AttendanceRepository {
    override fun findById(id: AttendanceRecordId): AttendanceRecord? {
        val entityId = id.value.toLongOrNull() ?: return null
        return springRepository.findById(entityId)
            .map { mapper.toDomain(it, GuildId("unknown")) }
            .orElse(null)
    }

    override fun findByRaiderIdAndGuildIdAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        val entities = springRepository.findByCharacterId(raiderId.value)
        return entities
            .filter { entity ->
                val recordDate = entity.endDate ?: entity.startDate
                recordDate != null && !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)
            }
            .map { mapper.toDomain(it, guildId) }
    }

    override fun findByRaiderIdAndGuildIdAndInstanceAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        val entities = springRepository.findByCharacterId(raiderId.value)
        return entities
            .filter { entity ->
                val recordDate = entity.endDate ?: entity.startDate
                entity.instance == instance &&
                    recordDate != null &&
                    !recordDate.isBefore(startDate) &&
                    !recordDate.isAfter(endDate)
            }
            .map { mapper.toDomain(it, guildId) }
    }

    override fun findByRaiderIdAndGuildIdAndEncounterAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        val entities = springRepository.findByCharacterId(raiderId.value)
        return entities
            .filter { entity ->
                val recordDate = entity.endDate ?: entity.startDate
                entity.instance == instance &&
                    entity.encounter == encounter &&
                    recordDate != null &&
                    !recordDate.isBefore(startDate) &&
                    !recordDate.isAfter(endDate)
            }
            .map { mapper.toDomain(it, guildId) }
    }

    override fun findByGuildIdAndDateRange(
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        // Note: Legacy table doesn't have guild_id column
        // This returns all attendance records filtered by date range
        val entities = springRepository.findAll()
        return entities
            .filter { entity ->
                val recordDate = entity.endDate ?: entity.startDate
                recordDate != null && !recordDate.isBefore(startDate) && !recordDate.isAfter(endDate)
            }
            .map { mapper.toDomain(it, guildId) }
            .toList()
    }

    override fun save(record: AttendanceRecord): AttendanceRecord {
        val entity = mapper.toEntity(record)
        val saved = springRepository.save(entity)
        return mapper.toDomain(saved, record.guildId)
    }

    override fun delete(id: AttendanceRecordId) {
        val entityId = id.value.toLongOrNull() ?: return
        springRepository.deleteById(entityId)
    }
}
