package com.edgerush.lootman.api.loot

import com.edgerush.lootman.application.loot.AwardLootCommand
import com.edgerush.lootman.application.loot.AwardLootUseCase
import com.edgerush.lootman.application.loot.CreateLootBanCommand
import com.edgerush.lootman.application.loot.GetLootAwardQuery
import com.edgerush.lootman.application.loot.GetLootAwardUseCase
import com.edgerush.lootman.application.loot.GetLootBanQuery
import com.edgerush.lootman.application.loot.GetLootBanUseCase
import com.edgerush.lootman.application.loot.GetLootHistoryUseCase
import com.edgerush.lootman.application.loot.ListLootAwardsByGuildPaginatedQuery
import com.edgerush.lootman.application.loot.ListLootAwardsByGuildQuery
import com.edgerush.lootman.application.loot.ListLootAwardsUseCase
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import com.edgerush.lootman.application.loot.ManageLootBansUseCase
import com.edgerush.lootman.application.loot.RemoveLootBanCommand
import com.edgerush.lootman.application.loot.RevokeLootAwardCommand
import com.edgerush.lootman.application.loot.RevokeLootAwardUseCase
import com.edgerush.lootman.application.loot.UpdateLootBanCommand
import com.edgerush.lootman.application.loot.UpdateLootBanUseCase
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
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

/**
 * REST controller for Loot operations.
 *
 * Provides endpoints for:
 * - Awarding loot to raiders
 * - Querying loot history
 * - Managing loot bans
 * - Viewing loot awards
 */
@RestController
@RequestMapping("/api/v1/loot")
class LootController(
    private val awardLootUseCase: AwardLootUseCase,
    private val getLootHistoryUseCase: GetLootHistoryUseCase,
    private val manageLootBansUseCase: ManageLootBansUseCase,
    private val getLootAwardUseCase: GetLootAwardUseCase,
    private val listLootAwardsUseCase: ListLootAwardsUseCase,
    private val revokeLootAwardUseCase: RevokeLootAwardUseCase,
    private val getLootBanUseCase: GetLootBanUseCase,
    private val updateLootBanUseCase: UpdateLootBanUseCase,
    private val paginationProperties: PaginationProperties,
) {
    /**
     * Award loot to a raider.
     */
    @PostMapping("/awards")
    fun awardLoot(
        @RequestBody request: AwardLootRequest,
    ): ResponseEntity<LootAwardDto> {
        val command =
            AwardLootCommand(
                itemId = ItemId(request.itemId),
                raiderId = RaiderId(request.raiderId.toLong()),
                guildId = GuildId(request.guildId),
                flpsScore = FlpsScore.of(request.flpsScore),
                tier = LootTier.valueOf(request.tier),
            )

        return awardLootUseCase
            .execute(command)
            .map { lootAward ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(LootAwardDto.from(lootAward))
            }.getOrElse { exception ->
                throw exception
            }
    }

    /**
     * Get loot history for a guild.
     */
    @GetMapping("/guilds/{guildId}/history")
    fun getGuildLootHistory(
        @PathVariable guildId: String,
        @RequestParam(defaultValue = "false") activeOnly: Boolean,
    ): LootHistoryResponse {
        val query =
            com.edgerush.lootman.application.loot.GetLootHistoryByGuildQuery(
                guildId = GuildId(guildId),
                activeOnly = activeOnly,
            )
        val result = getLootHistoryUseCase.getByGuild(query)
        return result
            .map { awards -> LootHistoryResponse.from(awards) }
            .getOrThrow()
    }

    /**
     * Get loot history for a raider.
     */
    @GetMapping("/raiders/{raiderId}/history")
    fun getRaiderLootHistory(
        @PathVariable raiderId: String,
        @RequestParam(defaultValue = "false") activeOnly: Boolean,
    ): LootHistoryResponse {
        val query =
            com.edgerush.lootman.application.loot.GetLootHistoryByRaiderQuery(
                raiderId = RaiderId(raiderId.toLong()),
                activeOnly = activeOnly,
            )
        val result = getLootHistoryUseCase.getByRaider(query)
        return result
            .map { awards -> LootHistoryResponse.from(awards) }
            .getOrThrow()
    }

    /**
     * Create a loot ban for a raider.
     */
    @PostMapping("/bans")
    fun createBan(
        @RequestBody request: CreateLootBanRequest,
    ): ResponseEntity<LootBanDto> {
        val command =
            CreateLootBanCommand(
                raiderId = RaiderId(request.raiderId.toLong()),
                guildId = GuildId(request.guildId),
                reason = request.reason,
                expiresAt = request.expiresAt,
            )

        return manageLootBansUseCase
            .createBan(command)
            .map { ban ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(LootBanDto.from(ban))
            }.getOrElse { exception ->
                throw exception
            }
    }

    /**
     * Remove a loot ban.
     */
    @DeleteMapping("/bans/{banId}")
    fun removeBan(
        @PathVariable banId: String,
    ): ResponseEntity<Void> {
        val command = RemoveLootBanCommand(banId = LootBanId(banId))

        return manageLootBansUseCase
            .removeBan(command)
            .map {
                ResponseEntity
                    .noContent()
                    .build<Void>()
            }.getOrElse { exception ->
                throw exception
            }
    }

    /**
     * Get active loot bans for a raider.
     */
    @GetMapping("/raiders/{raiderId}/bans")
    fun getActiveBans(
        @PathVariable raiderId: String,
        @RequestParam guildId: String,
    ): LootBansResponse {
        val query =
            com.edgerush.lootman.application.loot.GetActiveBansQuery(
                raiderId = RaiderId(raiderId.toLong()),
                guildId = GuildId(guildId),
            )
        val result = manageLootBansUseCase.getActiveBans(query)
        return result
            .map { bans -> LootBansResponse.from(bans) }
            .getOrThrow()
    }

    /**
     * Get all loot awards for a guild (non-paginated, for backwards compatibility).
     */
    @GetMapping("/awards/all")
    fun listAllLootAwards(
        @RequestParam guildId: String
    ): LootAwardsListResponse {
        return listLootAwardsUseCase.executeByGuild(ListLootAwardsByGuildQuery(guildId))
            .map { awards -> LootAwardsListResponse.from(awards) }
            .getOrThrow()
    }

    /**
     * Get loot awards for a guild with pagination.
     */
    @GetMapping("/awards")
    fun listLootAwards(
        @RequestParam guildId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?
    ): PagedResponse<LootAwardDto> {
        val pageRequest = paginationProperties.createPageRequest(page, size)
        val query = ListLootAwardsByGuildPaginatedQuery(
            guildId = guildId,
            offset = pageRequest.offset,
            limit = pageRequest.size
        )

        return listLootAwardsUseCase.executeByGuildPaginated(query)
            .map { result ->
                PagedResponse.of(
                    content = result.awards.map { LootAwardDto.from(it) },
                    pageRequest = pageRequest,
                    totalElements = result.totalCount
                )
            }
            .getOrThrow()
    }

    /**
     * Get a specific loot award by ID.
     */
    @GetMapping("/awards/{awardId}")
    fun getLootAward(@PathVariable awardId: String): LootAwardDto {
        return getLootAwardUseCase.execute(GetLootAwardQuery(awardId))
            .map { award -> LootAwardDto.from(award) }
            .getOrThrow()
    }

    /**
     * Revoke/delete a loot award.
     */
    @DeleteMapping("/awards/{awardId}")
    fun revokeLootAward(@PathVariable awardId: String): ResponseEntity<Void> {
        return revokeLootAwardUseCase.execute(RevokeLootAwardCommand(awardId))
            .map { ResponseEntity.noContent().build<Void>() }
            .getOrThrow()
    }

    /**
     * Get a specific loot ban by ID.
     */
    @GetMapping("/bans/{banId}")
    fun getLootBan(@PathVariable banId: String): LootBanDto {
        return getLootBanUseCase.execute(GetLootBanQuery(banId))
            .map { ban -> LootBanDto.from(ban) }
            .getOrThrow()
    }

    /**
     * Update a loot ban.
     */
    @PutMapping("/bans/{banId}")
    fun updateLootBan(
        @PathVariable banId: String,
        @RequestBody request: UpdateLootBanRequest
    ): LootBanDto {
        val command = UpdateLootBanCommand(
            banId = banId,
            reason = request.reason,
            expiresAt = request.expiresAt
        )
        return updateLootBanUseCase.execute(command)
            .map { ban -> LootBanDto.from(ban) }
            .getOrThrow()
    }
}
