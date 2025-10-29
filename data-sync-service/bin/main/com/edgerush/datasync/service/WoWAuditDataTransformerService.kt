package com.edgerush.datasync.service

import com.edgerush.datasync.repository.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class WoWAuditDataTransformerService(
    private val attendanceStatRepository: AttendanceStatRepository,
    private val historicalActivityRepository: HistoricalActivityRepository,
    private val characterHistoryRepository: CharacterHistoryRepository,
    private val wishlistSnapshotRepository: WishlistSnapshotRepository,
    private val lootAwardRepository: LootAwardRepository,
    private val raidRepository: RaidRepository,
    private val raidSignupRepository: RaidSignupRepository,
    private val raiderService: RaiderService
) {

    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Transform WoWAudit attendance data for FLPS RMS calculation
     */
    fun getAttendanceData(): AttendanceData {
        val attendanceStats = attendanceStatRepository.findAll()
        val raidSignups = raidSignupRepository.findAll()
        
        val characterAttendance = mutableMapOf<String, CharacterAttendanceInfo>()
        
        // Process attendance statistics
        attendanceStats.forEach { stat ->
            val key = "${stat.characterName}-${stat.characterRealm}"
            characterAttendance[key] = CharacterAttendanceInfo(
                characterName = stat.characterName,
                characterRealm = stat.characterRealm ?: "",
                role = stat.characterRole,
                attendancePercentage = stat.attendedPercentage ?: 0.0,
                attendedRaids = stat.attendedAmountOfRaids ?: 0,
                totalRaids = stat.totalAmountOfRaids ?: 0,
                selectedPercentage = stat.selectedPercentage ?: 0.0
            )
        }
        
        // Supplement with raid signup data for more detailed tracking
        val signupsByCharacter = raidSignups.groupBy { "${it.characterName}-${it.characterRealm}" }
        signupsByCharacter.forEach { (key, signups) ->
            val presentCount = signups.count { it.status == "Present" }
            val totalCount = signups.size
            val attendancePercentage = if (totalCount > 0) (presentCount.toDouble() / totalCount) * 100 else 0.0
            
            if (characterAttendance.containsKey(key)) {
                // Update with more recent data if available
                val existing = characterAttendance[key]!!
                if (totalCount > existing.totalRaids) {
                    characterAttendance[key] = existing.copy(
                        attendancePercentage = attendancePercentage,
                        attendedRaids = presentCount,
                        totalRaids = totalCount
                    )
                }
            } else {
                // Add new character from raid data
                val signup = signups.first()
                characterAttendance[key] = CharacterAttendanceInfo(
                    characterName = signup.characterName ?: "",
                    characterRealm = signup.characterRealm ?: "",
                    role = signup.characterRole,
                    attendancePercentage = attendancePercentage,
                    attendedRaids = presentCount,
                    totalRaids = totalCount,
                    selectedPercentage = attendancePercentage
                )
            }
        }
        
        return AttendanceData(
            characters = characterAttendance.values.toList(),
            lastUpdated = OffsetDateTime.now()
        )
    }

    /**
     * Transform WoWAudit historical activity data for FLPS RMS calculation
     */
    fun getActivityData(): ActivityData {
        val activities = historicalActivityRepository.findAll()
        val characterActivities = mutableListOf<CharacterActivityInfo>()
        
        activities.forEach { activity ->
            try {
                val dataNode = objectMapper.readTree(activity.dataJson)
                characterActivities.add(
                    CharacterActivityInfo(
                        characterName = activity.characterName,
                        characterRealm = activity.characterRealm ?: "",
                        dungeonsDone = parseDungeonsData(dataNode.path("dungeons_done")),
                        worldQuestsDone = dataNode.path("world_quests_done").asInt(0),
                        vaultOptions = parseVaultData(dataNode.path("vault_options")),
                        periodId = activity.periodId ?: 0L
                    )
                )
            } catch (ex: Exception) {
                logger.warn("Failed to parse activity data for ${activity.characterName}: ${ex.message}")
            }
        }
        
        return ActivityData(
            characters = characterActivities,
            lastUpdated = OffsetDateTime.now()
        )
    }

    /**
     * Transform WoWAudit wishlist data for FLPS IPI calculation
     */
    fun getWishlistData(): WishlistData {
        val wishlists = wishlistSnapshotRepository.findAll()
        val characterWishlists = mutableListOf<CharacterWishlistInfo>()
        
        wishlists.forEach { wishlist ->
            try {
                val payloadNode = objectMapper.readTree(wishlist.rawPayload)
                val items = parseWishlistItems(payloadNode)
                
                characterWishlists.add(
                    CharacterWishlistInfo(
                        characterName = wishlist.characterName,
                        characterRealm = wishlist.characterRealm,
                        items = items,
                        lastUpdated = wishlist.syncedAt
                    )
                )
            } catch (ex: Exception) {
                logger.warn("Failed to parse wishlist data for ${wishlist.characterName}: ${ex.message}")
            }
        }
        
        return WishlistData(
            characters = characterWishlists,
            lastUpdated = OffsetDateTime.now()
        )
    }

    /**
     * Transform WoWAudit loot history for FLPS RDF calculation
     */
    fun getLootHistoryData(): LootHistoryData {
        val lootAwards = lootAwardRepository.findAll()
        val characterLoot = mutableListOf<CharacterLootInfo>()
        
        val lootByCharacter = lootAwards.groupBy { it.raiderId }
        lootByCharacter.forEach { (raiderId, awards) ->
            val raider = raiderService.findById(raiderId)
            if (raider != null) {
                val recentAwards = awards.map { award ->
                    LootAwardInfo(
                        itemId = award.itemId,
                        itemName = award.itemName,
                        awardedAt = award.awardedAt,
                        tier = award.tier,
                        flps = award.flps,
                        rdf = award.rdf
                    )
                }
                
                characterLoot.add(
                    CharacterLootInfo(
                        characterName = raider.characterName,
                        characterRealm = raider.realm,
                        recentAwards = recentAwards
                    )
                )
            }
        }
        
        return LootHistoryData(
            characters = characterLoot,
            lastUpdated = OffsetDateTime.now()
        )
    }

    /**
     * Transform individual character history for IPI tier impact calculation
     */
    fun getCharacterGearData(): CharacterGearData {
        val characterHistories = characterHistoryRepository.findAll()
        val gearData = mutableListOf<CharacterCurrentGear>()
        
        characterHistories.forEach { history ->
            try {
                val bestGearJson = history.bestGearJson
                if (bestGearJson != null) {
                    val gearNode = objectMapper.readTree(bestGearJson)
                    val gearItems = parseGearItems(gearNode)
                    
                    gearData.add(
                        CharacterCurrentGear(
                            characterName = history.characterName,
                            characterRealm = history.characterRealm ?: "",
                            currentGear = gearItems
                        )
                    )
                }
            } catch (ex: Exception) {
                logger.warn("Failed to parse gear data for ${history.characterName}: ${ex.message}")
            }
        }
        
        return CharacterGearData(
            characters = gearData,
            lastUpdated = OffsetDateTime.now()
        )
    }

    private fun parseDungeonsData(node: JsonNode): List<DungeonCompletion> {
        if (!node.isArray) return emptyList()
        return node.map { dungeon ->
            DungeonCompletion(
                level = dungeon.path("level").asInt(0),
                dungeonId = dungeon.path("dungeon").asLong(0)
            )
        }
    }

    private fun parseVaultData(node: JsonNode): VaultOptions {
        return VaultOptions(
            raidsOption1 = node.path("raids").path("option_1").asInt(0),
            raidsOption2 = node.path("raids").path("option_2").asInt(0),
            raidsOption3 = node.path("raids").path("option_3").asInt(0),
            dungeonsOption1 = node.path("dungeons").path("option_1").asInt(0),
            dungeonsOption2 = node.path("dungeons").path("option_2").asInt(0),
            dungeonsOption3 = node.path("dungeons").path("option_3").asInt(0),
            worldOption1 = node.path("world").path("option_1").asInt(0),
            worldOption2 = node.path("world").path("option_2").asInt(0),
            worldOption3 = node.path("world").path("option_3").asInt(0)
        )
    }

    private fun parseWishlistItems(node: JsonNode): List<WishlistItem> {
        val items = mutableListOf<WishlistItem>()
        val wishlists = node.path("wishlists")
        
        if (wishlists.isArray) {
            wishlists.forEach { wishlist ->
                val instances = wishlist.path("instances")
                if (instances.isArray) {
                    instances.forEach { instance ->
                        val difficulties = instance.path("difficulties")
                        if (difficulties.isArray) {
                            difficulties.forEach { difficulty ->
                                val encounters = difficulty.path("wishlist").path("wishlist").path("encounters")
                                if (encounters.isArray) {
                                    encounters.forEach { encounter ->
                                        val encounterItems = encounter.path("items")
                                        if (encounterItems.isArray) {
                                            encounterItems.forEach { item ->
                                                val wishes = item.path("wishes")
                                                if (wishes.isArray && wishes.size() > 0) {
                                                    val wish = wishes[0] // Take first wish
                                                    items.add(
                                                        WishlistItem(
                                                            itemId = item.path("id").asLong(0),
                                                            itemName = item.path("name").asText(""),
                                                            slot = item.path("slot").asText(""),
                                                            upgradeWeight = wish.path("weight").asInt(0),
                                                            upgradePercentage = wish.path("percentage").asDouble(0.0),
                                                            upgradeType = wish.path("upgrade").asText(null)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return items
    }

    private fun parseGearItems(node: JsonNode): List<GearItem> {
        val items = mutableListOf<GearItem>()
        val slots = listOf(
            "head", "neck", "shoulder", "back", "chest", "wrist", 
            "hands", "waist", "legs", "feet", "finger_1", "finger_2", 
            "trinket_1", "trinket_2", "main_hand", "off_hand"
        )
        
        slots.forEach { slot ->
            val slotNode = node.path(slot)
            if (slotNode.isObject) {
                items.add(
                    GearItem(
                        slot = slot,
                        itemId = slotNode.path("id").asLong(0),
                        itemName = slotNode.path("name").asText(""),
                        itemLevel = slotNode.path("ilvl").asInt(0),
                        quality = slotNode.path("quality").asInt(0)
                    )
                )
            }
        }
        
        return items
    }
}

// Data Transfer Objects for FLPS integration
data class AttendanceData(
    val characters: List<CharacterAttendanceInfo>,
    val lastUpdated: OffsetDateTime
)

data class CharacterAttendanceInfo(
    val characterName: String,
    val characterRealm: String,
    val role: String?,
    val attendancePercentage: Double,
    val attendedRaids: Int,
    val totalRaids: Int,
    val selectedPercentage: Double
)

data class ActivityData(
    val characters: List<CharacterActivityInfo>,
    val lastUpdated: OffsetDateTime
)

data class CharacterActivityInfo(
    val characterName: String,
    val characterRealm: String,
    val dungeonsDone: List<DungeonCompletion>,
    val worldQuestsDone: Int,
    val vaultOptions: VaultOptions,
    val periodId: Long
)

data class DungeonCompletion(
    val level: Int,
    val dungeonId: Long
)

data class VaultOptions(
    val raidsOption1: Int,
    val raidsOption2: Int,
    val raidsOption3: Int,
    val dungeonsOption1: Int,
    val dungeonsOption2: Int,
    val dungeonsOption3: Int,
    val worldOption1: Int,
    val worldOption2: Int,
    val worldOption3: Int
)

data class WishlistData(
    val characters: List<CharacterWishlistInfo>,
    val lastUpdated: OffsetDateTime
)

data class CharacterWishlistInfo(
    val characterName: String,
    val characterRealm: String,
    val items: List<WishlistItem>,
    val lastUpdated: OffsetDateTime
)

data class WishlistItem(
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val upgradeWeight: Int,
    val upgradePercentage: Double,
    val upgradeType: String?
)

data class LootHistoryData(
    val characters: List<CharacterLootInfo>,
    val lastUpdated: OffsetDateTime
)

data class CharacterLootInfo(
    val characterName: String,
    val characterRealm: String,
    val recentAwards: List<LootAwardInfo>
)

data class LootAwardInfo(
    val itemId: Long,
    val itemName: String,
    val awardedAt: OffsetDateTime,
    val tier: String,
    val flps: Double,
    val rdf: Double
)

data class CharacterGearData(
    val characters: List<CharacterCurrentGear>,
    val lastUpdated: OffsetDateTime
)

data class CharacterCurrentGear(
    val characterName: String,
    val characterRealm: String,
    val currentGear: List<GearItem>
)

data class GearItem(
    val slot: String,
    val itemId: Long,
    val itemName: String,
    val itemLevel: Int,
    val quality: Int
)