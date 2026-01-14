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

/**
 * Exception thrown when a Discord user link is not found.
 */
class DiscordUserLinkNotFoundException(val linkId: Long) :
    DomainException("Discord user link not found: $linkId")

/**
 * Exception thrown when a Discord user link already exists.
 */
class DiscordUserLinkAlreadyExistsException(val discordUserId: String, val raiderId: Long) :
    DomainException("Discord user $discordUserId is already linked to raider $raiderId")

/**
 * Exception thrown when a user is not found.
 */
class UserNotFoundException(val userId: Long) :
    DomainException("User not found: $userId")

/**
 * Exception thrown when a user is not found by Discord ID.
 */
class UserNotFoundByDiscordIdException(val discordId: String) :
    DomainException("User not found with Discord ID: $discordId")

/**
 * Exception thrown when a user is not found by Battle.net ID.
 */
class UserNotFoundByBattlenetIdException(val battlenetId: String) :
    DomainException("User not found with Battle.net ID: $battlenetId")

/**
 * Exception thrown when authentication fails.
 */
class AuthenticationFailedException(message: String) :
    DomainException(message)

/**
 * Exception thrown when a refresh token is invalid.
 */
class InvalidRefreshTokenException(message: String = "Invalid or expired refresh token") :
    DomainException(message)

/**
 * Exception thrown when OAuth2 authentication fails.
 */
class OAuth2AuthenticationException(val provider: String, message: String) :
    DomainException("$provider OAuth2 error: $message")
