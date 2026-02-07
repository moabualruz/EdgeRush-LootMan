package com.edgerush.lootman.application.guild

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.lootman.domain.guild.model.GuildPermission
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import com.edgerush.lootman.domain.guild.repository.GuildPermissionRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.infrastructure.external.blizzard.BlizzardDataService
import com.edgerush.lootman.infrastructure.external.blizzard.BlizzardGuildMember
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Service to sync guild roster from Battle.net API and populate the raiders table.
 * This provides a complete guild roster, not just WoWAudit raiders.
 */
@Service
class GuildRosterSyncService(
    private val blizzardDataService: BlizzardDataService,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val guildPermissionRepository: GuildPermissionRepository,
) {
    private val logger = LoggerFactory.getLogger(GuildRosterSyncService::class.java)

    /**
     * Syncs the guild roster from Battle.net API.
     *
     * @param realmSlug The realm slug (e.g., "twisting-nether")
     * @param guildNameSlug The guild name as a slug (lowercase, hyphens for spaces)
     * @param region The region (e.g., "eu", "us")
     * @param guildId The internal guild ID to associate with raiders
     * @return Number of raiders synced
     */
    fun syncGuildRoster(
        realmSlug: String,
        guildNameSlug: String,
        region: String,
        guildId: String,
    ): GuildRosterSyncResult {
        logger.info("Starting guild roster sync for $guildNameSlug@$realmSlug ($region), guildId=$guildId")

        val members = blizzardDataService.getGuildRoster(realmSlug, guildNameSlug)
        if (members.isEmpty()) {
            logger.warn("No guild members found for $guildNameSlug@$realmSlug")
            return GuildRosterSyncResult(0, 0, 0)
        }

        logger.info("Found ${members.size} guild members from Battle.net")

        var created = 0
        var updated = 0
        var skipped = 0

        // Get class names map (we need to resolve class IDs to names)
        val classNames = getClassNameMap()

        for (member in members) {
            try {
                val result = upsertRaider(member, region, guildId, classNames)
                when (result) {
                    UpsertResult.CREATED -> created++
                    UpsertResult.UPDATED -> updated++
                    UpsertResult.SKIPPED -> skipped++
                }
            } catch (e: Exception) {
                logger.warn("Failed to sync member ${member.character.name}: ${e.message}")
                skipped++
            }
        }

        logger.info("Guild roster sync completed: created=$created, updated=$updated, skipped=$skipped")

        // Auto-create default permissions for officer ranks if they don't exist
        ensureDefaultPermissions(guildId)

        return GuildRosterSyncResult(created, updated, skipped)
    }

    /**
     * Ensures default permissions exist for common ranks.
     * This is called after each sync to ensure permissions are always available.
     *
     * Note: "Main" and "Raider" are WoWAudit ranks that regular raiders have.
     * Without permissions defined for these ranks, users cannot access guild data.
     */
    private fun ensureDefaultPermissions(guildId: String) {
        // Officer ranks get full permissions
        val officerRanks = listOf("Guild Master", "Officer", "Veteran")
        // Raider ranks get view permissions only
        val raiderRanks = listOf("Main", "Raider", "Member", "Initiate")
        val permissionTypes =
            listOf(
                GuildPermissionType.SETTINGS_ACCESS,
                GuildPermissionType.LOOT_MANAGEMENT,
                GuildPermissionType.MEMBER_MANAGEMENT,
                GuildPermissionType.VIEW_ALL_SCORES,
            )

        var created = 0
        // Grant full permissions to officer ranks
        for (rank in officerRanks) {
            for (permissionType in permissionTypes) {
                try {
                    val permission =
                        GuildPermission.create(
                            guildId = GuildId(guildId),
                            rankName = rank,
                            permissionType = permissionType,
                        )
                    guildPermissionRepository.save(permission)
                    created++
                } catch (e: Exception) {
                    // Permission already exists (unique constraint), ignore
                }
            }
        }

        // Grant view-only permission to regular raider ranks
        for (rank in raiderRanks) {
            try {
                val permission =
                    GuildPermission.create(
                        guildId = GuildId(guildId),
                        rankName = rank,
                        permissionType = GuildPermissionType.VIEW_ALL_SCORES,
                    )
                guildPermissionRepository.save(permission)
                created++
            } catch (e: Exception) {
                // Permission already exists (unique constraint), ignore
            }
        }

        if (created > 0) {
            logger.info("Created $created default permissions for guild $guildId")
        }
    }

    private fun upsertRaider(
        member: BlizzardGuildMember,
        region: String,
        guildId: String,
        classNames: Map<Int, String>,
    ): UpsertResult {
        val character = member.character
        // Handle nullable realm name - use slug as fallback, or "Unknown" if both are null
        val realmName = character.realm.name ?: character.realm.slug ?: "Unknown"

        // First try to find by blizzardId
        var existing = raiderEntityRepository.findByBlizzardId(character.id)

        // If not found by blizzardId, try by name+realm (normalized to handle slug vs display name differences)
        if (existing == null) {
            existing = raiderEntityRepository.findByCharacterNameAndRealmNormalized(character.name, realmName)
        }

        val className = classNames[character.playable_class.id] ?: "Unknown"
        val rankName = getRankName(member.rank)
        val now = OffsetDateTime.now()

        if (existing != null) {
            // Update existing raider
            val updated =
                existing.copy(
                    characterName = character.name,
                    realm = realmName,
                    region = region,
                    guildId = guildId,
                    blizzardId = character.id,
                    clazz = className,
                    rank = rankName,
                    lastSync = now,
                )
            raiderEntityRepository.save(updated)
            return UpsertResult.UPDATED
        } else {
            // Create new raider
            val newRaider =
                RaiderEntity(
                    characterName = character.name,
                    realm = realmName,
                    region = region,
                    guildId = guildId,
                    wowauditId = null,
                    clazz = className,
                    spec = "",
                    role = "",
                    rank = rankName,
                    status = "Active",
                    note = null,
                    blizzardId = character.id,
                    trackingSince = now,
                    joinDate = now,
                    blizzardLastModified = null,
                    lastSync = now,
                )
            raiderEntityRepository.save(newRaider)
            return UpsertResult.CREATED
        }
    }

    private fun getClassNameMap(): Map<Int, String> {
        return try {
            val classes = blizzardDataService.getPlayableClasses()
            classes.associate { it.id to it.name }
        } catch (e: Exception) {
            logger.warn("Failed to fetch class names, using defaults: ${e.message}")
            // Fallback to hardcoded class names
            mapOf(
                1 to "Warrior",
                2 to "Paladin",
                3 to "Hunter",
                4 to "Rogue",
                5 to "Priest",
                6 to "Death Knight",
                7 to "Shaman",
                8 to "Mage",
                9 to "Warlock",
                10 to "Monk",
                11 to "Druid",
                12 to "Demon Hunter",
                13 to "Evoker",
            )
        }
    }

    /**
     * Maps guild rank index to a human-readable name.
     * Rank 0 is typically Guild Master, 1-4 are Officers, etc.
     */
    private fun getRankName(rank: Int): String {
        return when (rank) {
            0 -> "Guild Master"
            1 -> "Officer"
            2 -> "Officer"
            3 -> "Veteran"
            4 -> "Raider"
            5 -> "Member"
            6 -> "Initiate"
            else -> "Member"
        }
    }

    private enum class UpsertResult {
        CREATED,
        UPDATED,
        SKIPPED,
    }
}

data class GuildRosterSyncResult(
    val created: Int,
    val updated: Int,
    val skipped: Int,
) {
    val total: Int get() = created + updated + skipped
}
