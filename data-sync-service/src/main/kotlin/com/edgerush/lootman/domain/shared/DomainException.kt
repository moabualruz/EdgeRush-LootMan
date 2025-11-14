package com.edgerush.lootman.domain.shared

/**
 * Base class for all domain exceptions.
 */
sealed class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when a raider is not found.
 */
class RaiderNotFoundException(val raiderId: RaiderId) :
    DomainException("Raider not found: ${raiderId.value}")

/**
 * Exception thrown when a guild is not found.
 */
class GuildNotFoundException(val guildId: GuildId) :
    DomainException("Guild not found: ${guildId.value}")

/**
 * Exception thrown when an item is not found.
 */
class ItemNotFoundException(val itemId: ItemId) :
    DomainException("Item not found: ${itemId.value}")

/**
 * Exception thrown when a raider has active loot bans.
 */
class LootBanActiveException(val raiderId: RaiderId, val bans: List<com.edgerush.lootman.domain.loot.model.LootBan>) :
    DomainException("Raider ${raiderId.value} has ${bans.size} active loot ban(s)")
