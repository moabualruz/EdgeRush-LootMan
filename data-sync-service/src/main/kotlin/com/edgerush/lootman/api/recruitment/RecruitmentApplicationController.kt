package com.edgerush.lootman.api.recruitment

import com.edgerush.lootman.api.common.CountResponse
import com.edgerush.lootman.api.common.ExistsResponse
import com.edgerush.lootman.domain.application.client.RaiderIOCharacterProfile
import com.edgerush.lootman.domain.application.client.RaiderIOClient
import com.edgerush.lootman.domain.application.model.Application
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.application.model.ApplicationStatus
import com.edgerush.lootman.domain.application.service.ApplicationService
import com.edgerush.lootman.domain.shared.GuildId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * REST controller for the enhanced recruitment application system.
 *
 * Supports OAuth integration and auto-fetching of character data from external APIs.
 */
@RestController
@RequestMapping("/api/v1/recruitment/applications")
@Tag(name = "Recruitment", description = "Enhanced guild application management with OAuth and auto-fetch support")
class RecruitmentApplicationController(
    private val applicationService: ApplicationService,
    private val raiderIOClient: RaiderIOClient,
) {

    @Operation(summary = "Get all applications for a guild", description = "Returns applications for a guild with optional status filter")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully retrieved applications"),
    )
    @GetMapping("/guilds/{guildId}")
    fun getApplicationsByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) status: ApplicationStatus?,
        @Parameter(description = "Page offset")
        @RequestParam(defaultValue = "0") offset: Long,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "50") limit: Int,
    ): List<ApplicationResponse> {
        val applications = if (status != null) {
            applicationService.getApplicationsByStatus(GuildId(guildId), status, offset, limit)
        } else {
            applicationService.getApplicationsByGuild(GuildId(guildId), offset, limit)
        }
        return applications.map { it.toResponse() }
    }

    @Operation(summary = "Get pending applications for a guild", description = "Returns only pending applications")
    @GetMapping("/guilds/{guildId}/pending")
    fun getPendingApplications(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Page offset")
        @RequestParam(defaultValue = "0") offset: Long,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "50") limit: Int,
    ): List<ApplicationResponse> {
        return applicationService.getPendingApplications(GuildId(guildId), offset, limit)
            .map { it.toResponse() }
    }

    @Operation(summary = "Get application by ID", description = "Returns a single application")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Application found"),
        ApiResponse(responseCode = "404", description = "Application not found"),
    )
    @GetMapping("/{applicationId}")
    fun getApplicationById(
        @Parameter(description = "Application ID")
        @PathVariable applicationId: String,
    ): ResponseEntity<ApplicationResponse> {
        val application = applicationService.getApplicationById(ApplicationId(applicationId))
        return if (application != null) {
            ResponseEntity.ok(application.toResponse())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "Submit a new application", description = "Creates a new guild application")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Application submitted successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
        ApiResponse(responseCode = "409", description = "Application already exists for this account"),
    )
    @PostMapping("/guilds/{guildId}")
    fun submitApplication(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Valid @RequestBody request: SubmitApplicationRequest,
    ): ResponseEntity<ApplicationResponse> {
        return try {
            val application = applicationService.submitApplication(
                guildId = GuildId(guildId),
                battleNetId = request.battleNetId,
                discordId = request.discordId,
                email = request.email,
                characterName = request.characterName,
                characterRealm = request.characterRealm,
                characterClass = request.characterClass,
                specialization = request.specialization,
                itemLevel = request.itemLevel,
                raiderIOScore = request.raiderIOScore,
                bestParseAverage = request.bestParseAverage,
                age = request.age,
                location = request.location,
                timezone = request.timezone,
                raidDaysAvailable = request.raidDaysAvailable,
                previousGuilds = request.previousGuilds,
                reasonForLeaving = request.reasonForLeaving,
                whyThisGuild = request.whyThisGuild,
            )
            ResponseEntity.status(HttpStatus.CREATED).body(application.toResponse())
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    @Operation(summary = "Start review of an application", description = "Marks an application as under review")
    @PutMapping("/{applicationId}/review")
    fun startReview(
        @Parameter(description = "Application ID")
        @PathVariable applicationId: String,
        @Valid @RequestBody request: ReviewRequest,
    ): ResponseEntity<ApplicationResponse> {
        return try {
            val application = applicationService.startReview(ApplicationId(applicationId), request.reviewerId)
            ResponseEntity.ok(application.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @Operation(summary = "Approve an application", description = "Approves a pending or under-review application")
    @PutMapping("/{applicationId}/approve")
    fun approveApplication(
        @Parameter(description = "Application ID")
        @PathVariable applicationId: String,
        @Valid @RequestBody request: ReviewRequest,
    ): ResponseEntity<ApplicationResponse> {
        return try {
            val application = applicationService.approveApplication(ApplicationId(applicationId), request.reviewerId)
            ResponseEntity.ok(application.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @Operation(summary = "Reject an application", description = "Rejects a pending or under-review application")
    @PutMapping("/{applicationId}/reject")
    fun rejectApplication(
        @Parameter(description = "Application ID")
        @PathVariable applicationId: String,
        @Valid @RequestBody request: ReviewRequest,
    ): ResponseEntity<ApplicationResponse> {
        return try {
            val application = applicationService.rejectApplication(ApplicationId(applicationId), request.reviewerId)
            ResponseEntity.ok(application.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @Operation(summary = "Withdraw an application", description = "Allows the applicant to withdraw their application")
    @PutMapping("/{applicationId}/withdraw")
    fun withdrawApplication(
        @Parameter(description = "Application ID")
        @PathVariable applicationId: String,
    ): ResponseEntity<ApplicationResponse> {
        return try {
            val application = applicationService.withdrawApplication(ApplicationId(applicationId))
            ResponseEntity.ok(application.toResponse())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @Operation(summary = "Count applications by guild", description = "Returns the count of applications for a guild")
    @GetMapping("/guilds/{guildId}/count")
    fun countByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) status: ApplicationStatus?,
    ): CountResponse {
        val count = if (status != null) {
            applicationService.countApplicationsByStatus(GuildId(guildId), status)
        } else {
            applicationService.countApplicationsByGuild(GuildId(guildId))
        }
        return CountResponse(count)
    }

    @Operation(summary = "Fetch character data from Raider.IO", description = "Auto-fetches character data from Raider.IO API")
    @GetMapping("/character-lookup")
    fun lookupCharacter(
        @Parameter(description = "Character region (us, eu, kr, tw, cn)")
        @RequestParam region: String,
        @Parameter(description = "Character realm")
        @RequestParam realm: String,
        @Parameter(description = "Character name")
        @RequestParam name: String,
    ): ResponseEntity<CharacterLookupResponse> {
        return try {
            val profile = raiderIOClient.fetchCharacterProfile(region, realm, name).block()
            if (profile != null) {
                ResponseEntity.ok(profile.toLookupResponse())
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }

    private fun Application.toResponse() = ApplicationResponse(
        id = id.value,
        guildId = guildId.value,
        battleNetId = battleNetId,
        discordId = discordId,
        email = email,
        characterName = characterName,
        characterRealm = characterRealm,
        characterClass = characterClass,
        specialization = specialization,
        itemLevel = itemLevel,
        raiderIOScore = raiderIOScore,
        bestParseAverage = bestParseAverage,
        age = age,
        location = location,
        timezone = timezone,
        raidDaysAvailable = raidDaysAvailable,
        previousGuilds = previousGuilds,
        reasonForLeaving = reasonForLeaving,
        whyThisGuild = whyThisGuild,
        status = status,
        reviewedBy = reviewedBy,
        reviewedAt = reviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun RaiderIOCharacterProfile.toLookupResponse() = CharacterLookupResponse(
        name = name,
        realm = realm,
        region = region,
        characterClass = characterClass,
        specialization = activeSpecName,
        role = activeSpecRole,
        itemLevel = getItemLevel(),
        raiderIOScore = getCurrentMythicPlusScore(),
        profileUrl = profileUrl,
    )
}

data class SubmitApplicationRequest(
    @field:NotBlank
    val battleNetId: String,
    @field:NotBlank
    val discordId: String,
    @field:Email
    val email: String,
    @field:NotBlank
    val characterName: String,
    @field:NotBlank
    val characterRealm: String,
    @field:NotBlank
    val characterClass: String,
    @field:NotBlank
    val specialization: String,
    val itemLevel: Double,
    val raiderIOScore: Double?,
    val bestParseAverage: Double?,
    @field:Min(18)
    val age: Int,
    @field:NotBlank
    val location: String,
    @field:NotBlank
    val timezone: String,
    @field:Size(min = 1)
    val raidDaysAvailable: List<String>,
    @field:NotBlank
    val previousGuilds: String,
    @field:NotBlank
    val reasonForLeaving: String,
    @field:NotBlank
    val whyThisGuild: String,
)

data class ReviewRequest(
    @field:NotBlank
    val reviewerId: String,
)

data class ApplicationResponse(
    val id: String,
    val guildId: String,
    val battleNetId: String,
    val discordId: String,
    val email: String,
    val characterName: String,
    val characterRealm: String,
    val characterClass: String,
    val specialization: String,
    val itemLevel: Double,
    val raiderIOScore: Double?,
    val bestParseAverage: Double?,
    val age: Int,
    val location: String,
    val timezone: String,
    val raidDaysAvailable: List<String>,
    val previousGuilds: String,
    val reasonForLeaving: String,
    val whyThisGuild: String,
    val status: ApplicationStatus,
    val reviewedBy: String?,
    val reviewedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CharacterLookupResponse(
    val name: String,
    val realm: String,
    val region: String,
    val characterClass: String,
    val specialization: String?,
    val role: String?,
    val itemLevel: Double?,
    val raiderIOScore: Double?,
    val profileUrl: String,
)
