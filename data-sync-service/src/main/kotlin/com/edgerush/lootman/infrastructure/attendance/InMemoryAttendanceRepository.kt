package com.edgerush.lootman.infrastructure.attendance

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of AttendanceRepository.
 *
 * This implementation stores attendance records in memory using a ConcurrentHashMap
 * for thread-safe operations. Suitable for testing and development.
 */
@Repository("domainAttendanceRepository")
@Primary
class InMemoryAttendanceRepository : AttendanceRepository {
    private val storage = ConcurrentHashMap<AttendanceRecordId, AttendanceRecord>()

    override fun findById(id: AttendanceRecordId): AttendanceRecord? = storage[id]

    override fun findByRaiderIdAndGuildIdAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        return storage.values.filter { record ->
            record.raiderId == raiderId &&
                record.guildId == guildId &&
                isDateRangeOverlapping(record.startDate, record.endDate, startDate, endDate)
        }
    }

    override fun findByRaiderIdAndGuildIdAndInstanceAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        return storage.values.filter { record ->
            record.raiderId == raiderId &&
                record.guildId == guildId &&
                record.instance == instance &&
                isDateRangeOverlapping(record.startDate, record.endDate, startDate, endDate)
        }
    }

    override fun findByRaiderIdAndGuildIdAndEncounterAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        return storage.values.filter { record ->
            record.raiderId == raiderId &&
                record.guildId == guildId &&
                record.instance == instance &&
                record.encounter == encounter &&
                isDateRangeOverlapping(record.startDate, record.endDate, startDate, endDate)
        }
    }

    override fun findByGuildIdAndDateRange(
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        return storage.values.filter { record ->
            record.guildId == guildId &&
                isDateRangeOverlapping(record.startDate, record.endDate, startDate, endDate)
        }
    }

    override fun save(record: AttendanceRecord): AttendanceRecord {
        storage[record.id] = record
        return record
    }

    override fun delete(id: AttendanceRecordId) {
        storage.remove(id)
    }

    /**
     * Checks if two date ranges overlap.
     */
    private fun isDateRangeOverlapping(
        recordStart: LocalDate,
        recordEnd: LocalDate,
        queryStart: LocalDate,
        queryEnd: LocalDate,
    ): Boolean {
        return !recordEnd.isBefore(queryStart) && !recordStart.isAfter(queryEnd)
    }
}
