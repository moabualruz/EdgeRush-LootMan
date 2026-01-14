package com.edgerush.lootman.bot.client

import com.edgerush.lootman.bot.config.DiscordProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull

/**
 * Client for communicating with the backend API.
 *
 * Handles all REST API calls to the data-sync-service.
 */
@Service
class BackendApiClient(
    private val properties: DiscordProperties,
    webClientBuilder: WebClient.Builder,
) {
    private val logger = LoggerFactory.getLogger(BackendApiClient::class.java)

    private val webClient = webClientBuilder
        .baseUrl(properties.backend.url)
        .build()

    /**
     * Fetches FLPS report for a guild.
     */
    suspend fun getFlpsReport(guildId: String): FlpsReportResponse? {
        return try {
            webClient.get()
                .uri("/api/v1/flps/guilds/{guildId}/report", guildId)
                .retrieve()
                .awaitBodyOrNull()
        } catch (e: Exception) {
            logger.error("Failed to fetch FLPS report for guild $guildId", e)
            null
        }
    }

    /**
     * Fetches FLPS score for a specific raider.
     */
    suspend fun getRaiderFlps(guildId: String, raiderId: Long): RaiderFlpsResponse? {
        return try {
            webClient.get()
                .uri("/api/v1/flps/guilds/{guildId}/raiders/{raiderId}", guildId, raiderId)
                .retrieve()
                .awaitBodyOrNull()
        } catch (e: Exception) {
            logger.error("Failed to fetch FLPS for raider $raiderId", e)
            null
        }
    }

    /**
     * Fetches leaderboard for a guild with optional role filter.
     */
    suspend fun getLeaderboard(
        guildId: String,
        role: String? = null,
        limit: Int = 10,
    ): LeaderboardResponse? {
        return try {
            val uri = if (role != null) {
                "/api/v1/flps/guilds/{guildId}/leaderboard?role={role}&limit={limit}"
            } else {
                "/api/v1/flps/guilds/{guildId}/leaderboard?limit={limit}"
            }
            webClient.get()
                .uri(uri, guildId, role ?: "", limit)
                .retrieve()
                .awaitBodyOrNull()
        } catch (e: Exception) {
            logger.error("Failed to fetch leaderboard for guild $guildId", e)
            null
        }
    }

    /**
     * Fetches loot history for a raider.
     */
    suspend fun getLootHistory(
        guildId: String,
        raiderId: Long,
        limit: Int = 10,
    ): LootHistoryResponse? {
        return try {
            webClient.get()
                .uri("/api/v1/loot/guilds/{guildId}/raiders/{raiderId}/history?limit={limit}", guildId, raiderId, limit)
                .retrieve()
                .awaitBodyOrNull()
        } catch (e: Exception) {
            logger.error("Failed to fetch loot history for raider $raiderId", e)
            null
        }
    }

    /**
     * Fetches raider by character name.
     */
    suspend fun getRaiderByCharacter(
        guildId: String,
        characterName: String,
        realm: String,
    ): RaiderResponse? {
        return try {
            webClient.get()
                .uri("/api/v1/raiders/guilds/{guildId}/characters?name={name}&realm={realm}", guildId, characterName, realm)
                .retrieve()
                .awaitBodyOrNull()
        } catch (e: Exception) {
            logger.error("Failed to fetch raider by character $characterName-$realm", e)
            null
        }
    }

    /**
     * Fetches Discord user link.
     */
    suspend fun getDiscordUserLink(discordUserId: String): DiscordUserLinkResponse? {
        return try {
            webClient.get()
                .uri("/api/v1/discord/users/{discordUserId}/links", discordUserId)
                .retrieve()
                .awaitBodyOrNull()
        } catch (e: Exception) {
            logger.error("Failed to fetch Discord user link for $discordUserId", e)
            null
        }
    }

    /**
     * Creates a Discord user link.
     */
    suspend fun createDiscordUserLink(request: CreateDiscordUserLinkRequest): DiscordUserLinkResponse? {
        return try {
            webClient.post()
                .uri("/api/v1/discord/users/{discordUserId}/links", request.discordUserId)
                .bodyValue(request)
                .retrieve()
                .awaitBodyOrNull()
        } catch (e: Exception) {
            logger.error("Failed to create Discord user link for ${request.discordUserId}", e)
            null
        }
    }

    /**
     * Deletes a Discord user link.
     */
    suspend fun deleteDiscordUserLink(
        discordUserId: String,
        characterName: String,
        realm: String,
    ): Boolean {
        return try {
            webClient.delete()
                .uri(
                    "/api/v1/discord/users/{discordUserId}/links?characterName={characterName}&realm={realm}",
                    discordUserId,
                    characterName,
                    realm,
                )
                .retrieve()
                .awaitBodyOrNull<Unit>()
            true
        } catch (e: Exception) {
            logger.error("Failed to delete Discord user link for $discordUserId", e)
            false
        }
    }
}

// Response DTOs

data class FlpsReportResponse(
    val guildId: String,
    val raiders: List<RaiderFlpsResponse>,
    val generatedAt: String,
)

data class RaiderFlpsResponse(
    val raiderId: Long,
    val characterName: String,
    val characterClass: String,
    val role: String,
    val flps: Double,
    val rms: RmsBreakdown,
    val ipi: IpiBreakdown,
    val rdf: Double,
    val eligible: Boolean,
    val ineligibilityReasons: List<String>?,
    val rank: Int?,
)

data class RmsBreakdown(
    val value: Double,
    val acs: Double,
    val mas: Double,
    val eps: Double,
)

data class IpiBreakdown(
    val value: Double,
    val uv: Double,
    val tierBonus: Double,
    val roleMultiplier: Double,
)

data class LeaderboardResponse(
    val guildId: String,
    val entries: List<LeaderboardEntry>,
    val totalRaiders: Int,
)

data class LeaderboardEntry(
    val rank: Int,
    val raiderId: Long,
    val characterName: String,
    val characterClass: String,
    val role: String,
    val flps: Double,
    val eligible: Boolean,
)

data class LootHistoryResponse(
    val raiderId: Long,
    val characterName: String,
    val awards: List<LootAwardEntry>,
)

data class LootAwardEntry(
    val itemId: Long,
    val itemName: String,
    val awardedAt: String,
    val flpsAtAward: Double,
    val rdfExpired: Boolean,
    val rdfExpiresAt: String?,
)

data class RaiderResponse(
    val id: Long,
    val characterName: String,
    val realm: String,
    val characterClass: String,
    val role: String,
    val status: String,
    val guildId: String,
)

data class DiscordUserLinkResponse(
    val discordUserId: String,
    val links: List<CharacterLink>,
)

data class CharacterLink(
    val characterName: String,
    val realm: String,
    val isPrimary: Boolean,
    val linkedAt: String,
    val raiderId: Long?,
)

data class CreateDiscordUserLinkRequest(
    val discordUserId: String,
    val characterName: String,
    val realm: String,
    val isPrimary: Boolean = false,
)
