package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

interface AttendanceJpaRepository : JpaRepository<AttendanceEntity, Long> {
    fun findByStartDateGreaterThanEqualAndEndDateLessThanEqual(start: LocalDate, end: LocalDate): List<AttendanceEntity>
}

@Repository
class JpaAttendanceRepositoryImpl(
    private val jpaRepository: AttendanceJpaRepository
) : AttendanceRepository {

    override fun findById(id: AttendanceRecordId): AttendanceRecord? {
        return jpaRepository.findById(id.value).orElse(null)?.toDomain()
    }

    override fun findByRaiderIdAndGuildIdAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> {
        return jpaRepository.findByStartDateGreaterThanEqualAndEndDateLessThanEqual(startDate, endDate)
            .map { it.toDomain() }
    }

    override fun findByRaiderIdAndGuildIdAndInstanceAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> {
        return findByRaiderIdAndGuildIdAndDateRange(raiderId, guildId, startDate, endDate)
            .filter { it.instance == instance }
    }

    override fun findByRaiderIdAndGuildIdAndEncounterAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> {
        return findByRaiderIdAndGuildIdAndInstanceAndDateRange(raiderId, guildId, instance, startDate, endDate)
            .filter { it.encounter == encounter }
    }

    override fun findByGuildIdAndDateRange(
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> {
        return jpaRepository.findByStartDateGreaterThanEqualAndEndDateLessThanEqual(startDate, endDate)
            .map { it.toDomain() }
    }

    override fun save(record: AttendanceRecord): AttendanceRecord {
        throw UnsupportedOperationException("Save not yet implemented")
    }

    override fun delete(id: AttendanceRecordId) {
        jpaRepository.deleteById(id.value)
    }
}
