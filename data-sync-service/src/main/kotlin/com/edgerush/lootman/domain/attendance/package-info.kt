/**
 * Attendance Bounded Context.
 *
 * This bounded context tracks raid attendance and calculates attendance statistics.
 *
 * ## Key Components
 *
 * - **AttendanceRecord**: Entity for individual attendance records
 * - **AttendanceStats**: Value object for attendance statistics
 * - **AttendanceCalculationService**: Domain service for attendance calculations
 * - **AttendanceRepository**: Repository for attendance records
 *
 * ## Business Rules
 *
 * - Attendance is tracked per raid
 * - Statistics are calculated over configurable time periods
 * - Attendance percentage affects FLPS score
 * - Late arrivals and early departures are tracked
 *
 * @since 1.0.0
 */
package com.edgerush.lootman.domain.attendance
