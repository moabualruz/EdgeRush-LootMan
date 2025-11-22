package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.*
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * JPA Entity for raiders table.
 */
@Entity
@Table(name = "raiders")
class RaiderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var characterName: String = "",

    @Column(nullable = false)
    var realm: String = "",

    @Column(name = "class", nullable = false)
    var characterClass: String = "",

    @Column(nullable = false)
    var role: String = "",

    var rank: String? = null,

    @Column(nullable = false)
    var status: String = "active",

    var joinDate: LocalDateTime? = null,

    var wowauditId: Long? = null
) {
    /**
     * Converts JPA entity to domain model.
     */
    fun toDomain(): Raider {
        return Raider(
            id = RaiderId(id),
            guildId = GuildId("default"), // TODO: Add guild_id column
            characterName = characterName,
            realm = realm,
            characterClass = CharacterClass.fromString(characterClass),
            role = Role.fromString(role),
            rank = rank,
            status = RaiderStatus.fromString(status) ?: RaiderStatus.ACTIVE,
            joinDate = joinDate,
            wowauditId = wowauditId
        )
    }
}
