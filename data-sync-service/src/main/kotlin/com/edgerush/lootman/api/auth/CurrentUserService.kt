package com.edgerush.lootman.api.auth

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Exception thrown when no raider is linked to the current user.
 */
class NoLinkedRaiderException(userId: Long) :
    RuntimeException("No raider linked to user $userId. Please link a character first.")

/**
 * Exception thrown when the user does not have access to a guild.
 */
class GuildAccessDeniedException(userId: Long, guildId: String) :
    RuntimeException("User $userId does not have access to guild $guildId")

/**
 * Service for resolving the current authenticated user's context.
 * Provides methods to get the current user's primary raider ID and guild access.
 */
@Service
class CurrentUserService(
    private val userCharacterMappingService: UserCharacterMappingService,
) {
    /**
     * Gets the current authenticated user from the security context.
     */
    fun getCurrentUser(): Mono<AuthenticatedUser> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.principal as AuthenticatedUser }
    }

    /**
     * Gets the primary raider ID for the current user.
     * @throws NoLinkedRaiderException if no raider is linked
     */
    fun getCurrentUserPrimaryRaiderId(): Mono<RaiderId> {
        return getCurrentUser().flatMap { user ->
            val userIdLong = user.id.toLongOrNull()

            // If in admin mode and no valid user ID, we can't get a raider ID
            if (user.isAdminMode && userIdLong == null) {
                return@flatMap Mono.error(NoLinkedRaiderException(-1L))
            }

            if (userIdLong == null) {
                return@flatMap Mono.error(IllegalArgumentException("Invalid user ID: ${user.id}"))
            }

            val userId = UserId(userIdLong)
            val primaryMapping =
                userCharacterMappingService.getPrimaryCharacterForUser(userId)
                    ?: return@flatMap Mono.error(NoLinkedRaiderException(userId.value))

            Mono.just(RaiderId(primaryMapping.raiderId))
        }
    }

    /**
     * Gets the primary raider ID for the current user, blocking.
     * For use in non-reactive controllers.
     */
    fun getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser: AuthenticatedUser): RaiderId {
        val userIdLong = authenticatedUser.id.toLongOrNull()

        // If in admin mode and no valid user ID, we can't get a raider ID
        if (authenticatedUser.isAdminMode && userIdLong == null) {
            throw NoLinkedRaiderException(-1L)
        }

        if (userIdLong == null) {
            throw IllegalArgumentException("Invalid user ID: ${authenticatedUser.id}")
        }

        val userId = UserId(userIdLong)
        val primaryMapping =
            userCharacterMappingService.getPrimaryCharacterForUser(userId)
                ?: throw NoLinkedRaiderException(userId.value)

        return RaiderId(primaryMapping.raiderId)
    }

    /**
     * Validates that the current user has access to the specified guild.
     */
    fun validateGuildAccess(
        user: AuthenticatedUser,
        guildId: GuildId,
    ) {
        if (!user.hasGuildAccess(guildId.value)) {
            val userId = user.id.toLongOrNull() ?: -1L
            throw GuildAccessDeniedException(userId, guildId.value)
        }
    }

    /**
     * Gets the user ID from the authenticated user.
     */
    fun getUserId(user: AuthenticatedUser): UserId {
        return UserId(user.id.toLongOrNull() ?: throw IllegalStateException("Invalid user ID"))
    }
}
