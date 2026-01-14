package com.edgerush.lootman.api.vault

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.api.raider.RaiderVaultSlotCrudService
import com.edgerush.lootman.api.raider.RaiderVaultSlotResponse
import com.edgerush.lootman.domain.shared.GuildId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * REST controller for Vault operations.
 *
 * Provides endpoints for retrieving vault options for the current user.
 */
@RestController
@RequestMapping("/api/v1/vault")
@Tag(name = "Vault", description = "Vault options endpoints")
class VaultController(
    private val vaultSlotService: RaiderVaultSlotCrudService,
    private val currentUserService: CurrentUserService,
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Get current user's vault options.
     *
     * @param guildId The guild's unique identifier
     * @param authenticatedUser The authenticated user from the JWT token
     * @return 200 OK with the vault options
     */
    @GetMapping("/guilds/{guildId}/me")
    @Operation(summary = "Get current user's vault options")
    fun getMyVaultOptions(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): VaultOptionsResponse {
        currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId))
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser)

        // Get all vault slots for the raider
        val allSlots = vaultSlotService.findByRaiderUnpaged(raiderId.value)

        // Calculate the start of the current reset week (Tuesday)
        val now = LocalDate.now()
        val weekOf = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY))

        // Group slots by type (slot field contains type info like "RAID_1", "MYTHIC_PLUS_2", etc.)
        val raidSlots = allSlots.filter { it.slot.startsWith("RAID") }
        val mythicPlusSlots = allSlots.filter { it.slot.startsWith("MYTHIC_PLUS") }
        val pvpSlots = allSlots.filter { it.slot.startsWith("PVP") }

        return VaultOptionsResponse(
            raiderId = raiderId.value,
            weekOf = weekOf.format(dateFormatter),
            raid = raidSlots,
            mythicPlus = mythicPlusSlots,
            pvp = pvpSlots,
        )
    }
}

/**
 * Response DTO for vault options.
 */
data class VaultOptionsResponse(
    val raiderId: Long,
    val weekOf: String,
    val raid: List<RaiderVaultSlotResponse>,
    val mythicPlus: List<RaiderVaultSlotResponse>,
    val pvp: List<RaiderVaultSlotResponse>,
)
