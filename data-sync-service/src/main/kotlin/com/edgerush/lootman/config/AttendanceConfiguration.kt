package com.edgerush.lootman.config

import com.edgerush.datasync.repository.AttendanceStatRepository
import com.edgerush.lootman.application.attendance.GetAttendanceReportUseCase
import com.edgerush.lootman.application.attendance.TrackAttendanceUseCase
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.attendance.service.AttendanceCalculationService
import com.edgerush.lootman.infrastructure.persistence.mapper.AttendanceMapper
import com.edgerush.lootman.infrastructure.persistence.repository.JdbcAttendanceRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for Attendance bounded context.
 *
 * Explicitly defines beans for the attendance domain to avoid conflicts
 * with other bounded contexts during component scanning.
 */
@Configuration
class AttendanceConfiguration {

    @Bean
    fun attendanceMapper(): AttendanceMapper {
        return AttendanceMapper()
    }

    @Bean
    fun attendanceRepository(
        springRepository: AttendanceStatRepository,
        mapper: AttendanceMapper
    ): AttendanceRepository {
        return JdbcAttendanceRepository(springRepository, mapper)
    }

    @Bean
    fun attendanceCalculationService(
        attendanceRepository: AttendanceRepository
    ): AttendanceCalculationService {
        return AttendanceCalculationService(attendanceRepository)
    }

    @Bean
    fun trackAttendanceUseCase(
        attendanceRepository: AttendanceRepository
    ): TrackAttendanceUseCase {
        return TrackAttendanceUseCase(attendanceRepository)
    }

    @Bean
    fun getAttendanceReportUseCase(
        attendanceCalculationService: AttendanceCalculationService
    ): GetAttendanceReportUseCase {
        return GetAttendanceReportUseCase(attendanceCalculationService)
    }
}
