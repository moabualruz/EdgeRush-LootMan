package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import jakarta.persistence.*
import java.time.LocalDate

/**
 * JPA Entity for attendance_stats table.
 */
@Entity
@Table(name = "attendance_stats")
class AttendanceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var characterName: String = "",
    var characterRealm: String = "",
    var instance: String? = null,
    var encounter: String? = null,
    var attendedAmount: Int = 0,
    var totalAmount: Int = 0,
    var attendancePercentage: Double = 0.0,
    var startDate: LocalDate = LocalDate.now(),
    var endDate: LocalDate = LocalDate.now()
) {
    fun toDomain(): AttendanceRecord {
        return AttendanceRecord(
            id = AttendanceRecordId(id),
            characterName = characterName,
            characterRealm = characterRealm,
            instance = instance,
            encounter = encounter,
            attendedAmount = attendedAmount,
            totalAmount = totalAmount,
            attendancePercentage = attendancePercentage,
            startDate = startDate,
            endDate = endDate
        )
    }
}
