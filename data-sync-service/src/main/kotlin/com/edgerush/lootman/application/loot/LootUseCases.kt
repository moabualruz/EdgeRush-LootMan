package com.edgerush.lootman.application.loot

import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Use case for getting a specific loot award by ID.
 */
@Service
class GetLootAwardUseCase(
    private val lootAwardRepository: LootAwardRepository
) {
    fun execute(query: GetLootAwardQuery): Result<LootAward> = runCatching {
        lootAwardRepository.findById(LootAwardId(query.awardId))
            ?: throw NoSuchElementException("Loot award not found: ${query.awardId}")
    }
}

/**
 * Use case for listing all loot awards.
 */
@Service
class ListLootAwardsUseCase(
    private val lootAwardRepository: LootAwardRepository
) {
    fun executeByGuild(query: ListLootAwardsByGuildQuery): Result<List<LootAward>> = runCatching {
        lootAwardRepository.findByGuildId(GuildId(query.guildId))
    }

    fun executeByGuildPaginated(query: ListLootAwardsByGuildPaginatedQuery): Result<PaginatedLootAwards> = runCatching {
        val guildId = GuildId(query.guildId)
        val awards = lootAwardRepository.findByGuildId(guildId, query.offset, query.limit)
        val totalCount = lootAwardRepository.countByGuildId(guildId)
        PaginatedLootAwards(awards, totalCount)
    }
}

/**
 * Result of a paginated loot awards query.
 */
data class PaginatedLootAwards(
    val awards: List<LootAward>,
    val totalCount: Long
)

/**
 * Use case for revoking/deleting a loot award.
 */
@Service
class RevokeLootAwardUseCase(
    private val lootAwardRepository: LootAwardRepository
) {
    fun execute(command: RevokeLootAwardCommand): Result<Unit> = runCatching {
        val awardId = LootAwardId(command.awardId)
        lootAwardRepository.findById(awardId)
            ?: throw NoSuchElementException("Loot award not found: ${command.awardId}")
        lootAwardRepository.delete(awardId)
    }
}

/**
 * Use case for getting a specific loot ban by ID.
 */
@Service
class GetLootBanUseCase(
    private val lootBanRepository: LootBanRepository
) {
    fun execute(query: GetLootBanQuery): Result<LootBan> = runCatching {
        lootBanRepository.findById(LootBanId(query.banId))
            ?: throw NoSuchElementException("Loot ban not found: ${query.banId}")
    }
}

/**
 * Use case for updating a loot ban.
 */
@Service
class UpdateLootBanUseCase(
    private val lootBanRepository: LootBanRepository
) {
    fun execute(command: UpdateLootBanCommand): Result<LootBan> = runCatching {
        val existingBan = lootBanRepository.findById(LootBanId(command.banId))
            ?: throw NoSuchElementException("Loot ban not found: ${command.banId}")

        val updatedBan = existingBan.copy(
            reason = command.reason ?: existingBan.reason,
            expiresAt = command.expiresAt ?: existingBan.expiresAt
        )

        lootBanRepository.save(updatedBan)
    }
}

// Query and Command classes

data class GetLootAwardQuery(
    val awardId: String
)

data class ListLootAwardsByGuildQuery(
    val guildId: String
)

data class ListLootAwardsByGuildPaginatedQuery(
    val guildId: String,
    val offset: Long,
    val limit: Int
)

data class RevokeLootAwardCommand(
    val awardId: String
)

data class GetLootBanQuery(
    val banId: String
)

data class UpdateLootBanCommand(
    val banId: String,
    val reason: String? = null,
    val expiresAt: Instant? = null
)
