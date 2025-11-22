package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "loot_bans")
class LootBanEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var raiderId: Long = 0,
    var reason: String = "",
    var bannedAt: LocalDateTime = LocalDateTime.now(),
    var expiresAt: LocalDateTime? = null,
    var bannedBy: String? = null
) {
    fun toDomain(): LootBan {
        return LootBan(
            id = id,
            raiderId = RaiderId(raiderId),
            reason = reason,
            bannedAt = bannedAt,
            expiresAt = expiresAt,
            bannedBy = bannedBy
        )
    }
}

interface LootBanJpaRepository : JpaRepository<LootBanEntity, Long> {
    fun findByRaiderId(raiderId: Long): List<LootBanEntity>
}

@Repository
class JpaLootBanRepositoryImpl(
    private val jpaRepository: LootBanJpaRepository
) : LootBanRepository {

    override fun findActiveByRaiderId(raiderId: RaiderId): List<LootBan> {
        val now = LocalDateTime.now()
        return jpaRepository.findByRaiderId(raiderId.value)
            .filter { it.expiresAt == null || it.expiresAt!!.isAfter(now) }
            .map { it.toDomain() }
    }

    override fun findByGuildId(guildId: GuildId): List<LootBan> {
        val now = LocalDateTime.now()
        return jpaRepository.findAll()
            .filter { it.expiresAt == null || it.expiresAt!!.isAfter(now) }
            .map { it.toDomain() }
    }

    override fun save(ban: LootBan): LootBan {
        throw UnsupportedOperationException("Save not yet implemented")
    }

    override fun delete(raiderId: RaiderId) {
        // Not implemented
    }
}
