package com.edgerush.lootman.application.raider

import com.edgerush.lootman.domain.shared.AccountId
import com.edgerush.lootman.domain.shared.CharacterId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime

/**
 * Use case for creating a new raider.
 */
@Service
class CreateRaiderUseCase(
    private val raiderRepository: RaiderRepository,
) {
    fun execute(command: CreateRaiderCommand): Result<Raider> =
        runCatching {
            val now = Instant.now()
            val raider =
                Raider(
                    id = RaiderId(command.id),
                    characterId = CharacterId(command.characterId),
                    name = command.characterName,
                    realm = command.realm,
                    region = command.region,
                    characterClass = CharacterClass.valueOf(command.characterClass.uppercase()),
                    blizzardId = command.blizzardId,
                    accountId = command.accountId?.let { AccountId(it) },
                    createdAt = now,
                    updatedAt = now,
                    guildId = GuildId(command.guildId),
                    role = Role.valueOf(command.role.uppercase()),
                    rank = command.rank,
                    status = RaiderStatus.valueOf(command.status.uppercase()),
                    joinDate = command.joinDate,
                    wowauditId = command.wowauditId,
                )
            raiderRepository.save(raider)
        }
}

/**
 * Use case for updating an existing raider.
 */
@Service
class UpdateRaiderUseCase(
    private val raiderRepository: RaiderRepository,
) {
    fun execute(command: UpdateRaiderCommand): Result<Raider> =
        runCatching {
            val existingRaider =
                raiderRepository.findById(RaiderId(command.id))
                    ?: throw NoSuchElementException("Raider not found with id: ${command.id}")

            val updatedRaider =
                existingRaider.copy(
                    name = command.characterName ?: existingRaider.name,
                    realm = command.realm ?: existingRaider.realm,
                    characterClass =
                        command.characterClass?.let { CharacterClass.valueOf(it.uppercase()) }
                            ?: existingRaider.characterClass,
                    role = command.role?.let { Role.valueOf(it.uppercase()) } ?: existingRaider.role,
                    rank = command.rank ?: existingRaider.rank,
                    status =
                        command.status?.let { RaiderStatus.valueOf(it.uppercase()) }
                            ?: existingRaider.status,
                    updatedAt = Instant.now(),
                )
            raiderRepository.save(updatedRaider)
        }
}

/**
 * Use case for deleting a raider.
 */
@Service
class DeleteRaiderUseCase(
    private val raiderRepository: RaiderRepository,
) {
    fun execute(command: DeleteRaiderCommand): Result<Unit> =
        runCatching {
            val existingRaider =
                raiderRepository.findById(RaiderId(command.id))
                    ?: throw NoSuchElementException("Raider not found with id: ${command.id}")
            raiderRepository.delete(existingRaider.id)
        }
}

/**
 * Use case for getting a raider by ID.
 */
@Service
class GetRaiderUseCase(
    private val raiderRepository: RaiderRepository,
) {
    fun execute(query: GetRaiderQuery): Result<Raider> =
        runCatching {
            raiderRepository.findById(RaiderId(query.id))
                ?: throw NoSuchElementException("Raider not found with id: ${query.id}")
        }
}

/**
 * Use case for listing raiders.
 */
@Service
class ListRaidersUseCase(
    private val raiderRepository: RaiderRepository,
) {
    fun executeByGuild(query: ListRaidersByGuildQuery): Result<List<Raider>> =
        runCatching {
            raiderRepository.findByGuildId(GuildId(query.guildId))
        }

    fun executeByGuildPaginated(query: ListRaidersByGuildPaginatedQuery): Result<PaginatedRaiders> =
        runCatching {
            val guildId = GuildId(query.guildId)
            val raiders = raiderRepository.findByGuildId(guildId, query.offset, query.limit)
            val totalCount = raiderRepository.countByGuildId(guildId)
            PaginatedRaiders(raiders, totalCount)
        }
}

/**
 * Result of a paginated raiders query.
 */
data class PaginatedRaiders(
    val raiders: List<Raider>,
    val totalCount: Long,
)

// Commands and Queries

data class CreateRaiderCommand(
    val id: Long,
    val characterId: Long,
    val guildId: String,
    val characterName: String,
    val realm: String,
    val region: String = "eu",
    val characterClass: String,
    val role: String,
    val rank: String? = null,
    val status: String = "ACTIVE",
    val joinDate: LocalDateTime? = null,
    val wowauditId: Long? = null,
    val blizzardId: Long? = null,
    val accountId: Long? = null,
)

data class UpdateRaiderCommand(
    val id: Long,
    val characterName: String? = null,
    val realm: String? = null,
    val characterClass: String? = null,
    val role: String? = null,
    val rank: String? = null,
    val status: String? = null,
)

data class DeleteRaiderCommand(val id: Long)

data class GetRaiderQuery(val id: Long)

data class ListRaidersByGuildQuery(val guildId: String)

data class ListRaidersByGuildPaginatedQuery(
    val guildId: String,
    val offset: Long,
    val limit: Int,
)
