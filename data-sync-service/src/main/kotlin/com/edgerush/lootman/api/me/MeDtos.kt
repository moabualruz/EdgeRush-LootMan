package com.edgerush.lootman.api.me

import java.time.Instant

/**
 * Response DTO for personal gear data.
 */
data class PersonalGearResponse(
    val raiderId: Long,
    val raiderName: String,
    val characterClass: String,
    val averageItemLevel: Double,
    val equippedItemLevel: Double,
    val items: List<GearItemResponse>,
    val missingEnchants: List<String>,
    val missingGems: List<String>,
)

/**
 * Single gear item in personal response.
 */
data class GearItemResponse(
    val slot: String,
    val itemId: Long,
    val itemName: String,
    val itemLevel: Int,
    val quality: String,
    val enchanted: Boolean,
    val gemmed: Boolean,
    val bonusIds: List<Int>,
)

/**
 * Response DTO for personal vault data.
 */
data class PersonalVaultResponse(
    val raiderId: Long,
    val raiderName: String,
    val raidSlots: List<VaultSlotResponse>,
    val mythicPlusSlots: List<VaultSlotResponse>,
    val pvpSlots: List<VaultSlotResponse>,
)

/**
 * Single vault slot in personal response.
 */
data class VaultSlotResponse(
    val slot: Int,
    val unlocked: Boolean,
    val itemLevel: Int?,
    val progress: Int,
    val required: Int,
)

/**
 * Response DTO for personal attendance data.
 */
data class PersonalAttendanceResponse(
    val raiderId: Long,
    val raiderName: String,
    val overallRate: Double,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalRaids: Int,
    val attendedRaids: Int,
    val acsScore: Double,
    val breakdown: AttendanceBreakdownResponse,
    val recentAttendance: List<AttendanceRecordResponse>,
)

/**
 * Attendance breakdown by type.
 */
data class AttendanceBreakdownResponse(
    val present: Int,
    val late: Int,
    val excused: Int,
    val absent: Int,
)

/**
 * Single attendance record.
 */
data class AttendanceRecordResponse(
    val raidDate: Instant,
    val raidName: String,
    val status: String,
    val note: String?,
)

/**
 * Response DTO for personal performance data.
 */
data class PersonalPerformanceResponse(
    val raiderId: Long,
    val raiderName: String,
    val characterClass: String,
    val spec: String?,
    val masScore: Double,
    val averagePerformance: Double,
    val averageItemLevelPerformance: Double,
    val killCount: Int,
    val bestPerformance: Double,
    val recentReports: List<PerformanceReportResponse>,
    val trendData: List<PerformanceTrendPoint>,
)

/**
 * Single Warcraft Logs report summary.
 */
data class PerformanceReportResponse(
    val reportId: String,
    val raidName: String,
    val encounterName: String,
    val date: Instant,
    val percentile: Double,
    val ilvlPercentile: Double,
    val dps: Double?,
    val hps: Double?,
)

/**
 * Performance trend data point for charting.
 */
data class PerformanceTrendPoint(
    val date: Instant,
    val percentile: Double,
    val ilvlPercentile: Double,
)

/**
 * Response DTO for personal wishlist data.
 */
data class PersonalWishlistResponse(
    val raiderId: Long,
    val raiderName: String,
    val items: List<WishlistItemResponse>,
    val simulationStatus: SimulationStatusResponse?,
)

/**
 * Single wishlist item in personal response.
 */
data class WishlistItemResponse(
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val priority: Int,
    val upgradeValue: Double,
    val source: String,
    val boss: String?,
    val currentItemLevel: Int?,
    val wishlistItemLevel: Int,
    val isUpgrade: Boolean,
)

/**
 * Simulation status for wishlist upgrade values.
 */
data class SimulationStatusResponse(
    val status: String,
    val lastRun: Instant?,
    val nextScheduled: Instant?,
    val isStale: Boolean,
)
