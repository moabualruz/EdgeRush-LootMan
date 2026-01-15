package com.edgerush.lootman.api.graphql.query

import com.edgerush.lootman.application.attendance.GetGuildAttendanceSummaryQuery
import com.edgerush.lootman.application.attendance.GetGuildAttendanceSummaryUseCase
import com.edgerush.lootman.application.attendance.GuildAttendanceSummary
import com.edgerush.lootman.application.attendance.ListRaiderAttendanceQuery
import com.edgerush.lootman.application.attendance.ListRaiderAttendanceUseCase
import com.edgerush.lootman.application.attendance.RaiderAttendanceSummary
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * GraphQL Query resolver for Attendance operations.
 *
 * Exposes attendance queries through GraphQL, delegating to the application layer use cases.
 * Provides access to individual raider attendance and guild-wide summaries.
 */
@Component
class AttendanceQueryResolver(
    private val listRaiderAttendanceUseCase: ListRaiderAttendanceUseCase,
    private val getGuildAttendanceSummaryUseCase: GetGuildAttendanceSummaryUseCase,
) : Query {
    /**
     * Get attendance records for a specific raider within a date range.
     *
     * @param raiderId The raider ID
     * @param guildId The guild ID
     * @param startDate The start date (ISO format: YYYY-MM-DD)
     * @param endDate The end date (ISO format: YYYY-MM-DD)
     * @return List of attendance records for the raider
     * @throws RuntimeException on errors
     */
    fun raiderAttendance(
        raiderId: String,
        guildId: String,
        startDate: String,
        endDate: String,
    ): List<AttendanceRecordType> {
        val query =
            ListRaiderAttendanceQuery(
                raiderId = raiderId.toLong(),
                guildId = guildId,
                startDate = LocalDate.parse(startDate),
                endDate = LocalDate.parse(endDate),
            )
        return listRaiderAttendanceUseCase.execute(query)
            .map { records -> records.map { it.toGraphQLType() } }
            .getOrThrow()
    }

    /**
     * Get attendance summary for an entire guild within a date range.
     *
     * @param guildId The guild ID
     * @param startDate The start date (ISO format: YYYY-MM-DD)
     * @param endDate The end date (ISO format: YYYY-MM-DD)
     * @return Guild attendance summary with per-raider breakdowns
     * @throws RuntimeException on errors
     */
    fun guildAttendanceSummary(
        guildId: String,
        startDate: String,
        endDate: String,
    ): GuildAttendanceSummaryType {
        val query =
            GetGuildAttendanceSummaryQuery(
                guildId = guildId,
                startDate = LocalDate.parse(startDate),
                endDate = LocalDate.parse(endDate),
            )
        return getGuildAttendanceSummaryUseCase.execute(query)
            .map { it.toGraphQLType() }
            .getOrThrow()
    }
}

/**
 * GraphQL type representing an attendance record.
 */
data class AttendanceRecordType(
    val id: String,
    val raiderId: String,
    val guildId: String,
    val instance: String,
    val encounter: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val attendedRaids: Int,
    val totalRaids: Int,
    val attendancePercentage: Double,
)

/**
 * GraphQL type representing a guild attendance summary.
 */
data class GuildAttendanceSummaryType(
    val guildId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalRecords: Int,
    val uniqueRaiders: Int,
    val overallAttendancePercentage: Double,
    val raiderSummaries: List<RaiderAttendanceSummaryType>,
)

/**
 * GraphQL type representing a raider's attendance summary within a guild.
 */
data class RaiderAttendanceSummaryType(
    val raiderId: String,
    val totalRecords: Int,
    val totalAttendedRaids: Int,
    val totalRaids: Int,
    val averageAttendancePercentage: Double,
)

/**
 * Extension function to convert domain AttendanceRecord to GraphQL AttendanceRecordType.
 */
private fun AttendanceRecord.toGraphQLType(): AttendanceRecordType =
    AttendanceRecordType(
        id = this.id.value,
        raiderId = this.raiderId.value.toString(),
        guildId = this.guildId.value,
        instance = this.instance,
        encounter = this.encounter,
        startDate = this.startDate,
        endDate = this.endDate,
        attendedRaids = this.attendedRaids,
        totalRaids = this.totalRaids,
        attendancePercentage = this.attendancePercentage,
    )

/**
 * Extension function to convert GuildAttendanceSummary to GraphQL GuildAttendanceSummaryType.
 */
private fun GuildAttendanceSummary.toGraphQLType(): GuildAttendanceSummaryType =
    GuildAttendanceSummaryType(
        guildId = this.guildId,
        startDate = this.startDate,
        endDate = this.endDate,
        totalRecords = this.totalRecords,
        uniqueRaiders = this.uniqueRaiders,
        overallAttendancePercentage = this.overallAttendancePercentage,
        raiderSummaries = this.raiderSummaries.map { it.toGraphQLType() },
    )

/**
 * Extension function to convert RaiderAttendanceSummary to GraphQL RaiderAttendanceSummaryType.
 */
private fun RaiderAttendanceSummary.toGraphQLType(): RaiderAttendanceSummaryType =
    RaiderAttendanceSummaryType(
        raiderId = this.raiderId.toString(),
        totalRecords = this.totalRecords,
        totalAttendedRaids = this.totalAttendedRaids,
        totalRaids = this.totalRaids,
        averageAttendancePercentage = this.averageAttendancePercentage,
    )
