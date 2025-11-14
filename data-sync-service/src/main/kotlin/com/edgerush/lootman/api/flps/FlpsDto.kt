package com.edgerush.lootman.api.flps

import com.edgerush.lootman.application.flps.FlpsCalculationResult
import com.edgerush.lootman.application.flps.FlpsReport

/**
 * Response DTO for FLPS report.
 */
data class FlpsReportResponse(
    val guildId: String,
    val calculations: List<FlpsCalculationResponse>,
) {
    companion object {
        fun from(report: FlpsReport): FlpsReportResponse {
            return FlpsReportResponse(
                guildId = report.guildId.value,
                calculations = report.calculations.map { FlpsCalculationResponse.from(it) },
            )
        }
    }
}

/**
 * Response DTO for individual FLPS calculation.
 */
data class FlpsCalculationResponse(
    val raiderId: String,
    val itemId: Long,
    val components: FlpsComponentsResponse,
    val flpsScore: Double,
    val eligible: Boolean,
) {
    companion object {
        fun from(result: FlpsCalculationResult): FlpsCalculationResponse {
            return FlpsCalculationResponse(
                raiderId = result.raiderId.value,
                itemId = result.itemId.value,
                components =
                    FlpsComponentsResponse(
                        rms =
                            RmsComponentsResponse(
                                acs = result.acs.value,
                                mas = result.mas.value,
                                eps = result.eps.value,
                                total = result.rms.value,
                            ),
                        ipi =
                            IpiComponentsResponse(
                                uv = result.uv.value,
                                tb = result.tb.value,
                                rm = result.rm.value,
                                total = result.ipi.value,
                            ),
                        rdf = result.rdf.value,
                    ),
                flpsScore = result.flps.value,
                eligible = result.eligible,
            )
        }
    }
}

/**
 * Response DTO for FLPS component scores.
 */
data class FlpsComponentsResponse(
    val rms: RmsComponentsResponse,
    val ipi: IpiComponentsResponse,
    val rdf: Double,
)

/**
 * Response DTO for Raider Merit Score components.
 */
data class RmsComponentsResponse(
    val acs: Double, // Attendance Commitment Score
    val mas: Double, // Mechanical Adherence Score
    val eps: Double, // External Preparation Score
    val total: Double,
)

/**
 * Response DTO for Item Priority Index components.
 */
data class IpiComponentsResponse(
    val uv: Double, // Upgrade Value
    val tb: Double, // Tier Bonus
    val rm: Double, // Role Multiplier
    val total: Double,
)

/**
 * Response DTO for system status.
 */
data class FlpsStatusResponse(
    val message: String,
    val features: List<String>,
    val endpoints: Map<String, String>,
)
