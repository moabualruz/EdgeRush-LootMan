package com.edgerush.lootman.domain.application.service

import com.edgerush.lootman.domain.application.client.RaiderIOCharacterProfile
import com.edgerush.lootman.domain.application.client.RaiderIOClient
import com.edgerush.lootman.domain.application.client.WarcraftLogsClient
import com.edgerush.lootman.domain.application.client.WarcraftLogsParseResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for fetching character data from external APIs for recruitment applications.
 *
 * Combines data from Raider.IO (item level, M+ score) and Warcraft Logs (parse averages)
 * to provide a complete picture of a character's progression.
 */
@Service
class ApplicationDataFetchService(
    private val raiderIOClient: RaiderIOClient,
    private val warcraftLogsClient: WarcraftLogsClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Fetches combined character data from Raider.IO and Warcraft Logs.
     *
     * @param region The region (us, eu, kr, tw, cn)
     * @param realm The character's realm
     * @param name The character's name
     * @return Combined character data
     * @throws CharacterNotFoundException if the character is not found in Raider.IO
     */
    fun fetchCharacterData(region: String, realm: String, name: String): CharacterFetchResult {
        log.info("Fetching character data for {}-{}-{}", name, realm, region)

        // Fetch RaiderIO data (required)
        val raiderIOProfile = raiderIOClient.fetchCharacterProfile(region, realm, name)
            .block()
            ?: throw CharacterNotFoundException("Character not found: $name-$realm-$region")

        // Fetch WarcraftLogs data (optional - don't fail if unavailable)
        val warcraftLogsResult = try {
            warcraftLogsClient.fetchCharacterParses(region, realm, name)
                .onErrorResume { error ->
                    log.warn("Failed to fetch Warcraft Logs data for {}: {}", name, error.message)
                    Mono.empty()
                }
                .block()
        } catch (e: Exception) {
            log.warn("Exception fetching Warcraft Logs data for {}: {}", name, e.message)
            null
        }

        return CharacterFetchResult(
            characterName = raiderIOProfile.name,
            realm = raiderIOProfile.realm,
            region = raiderIOProfile.region,
            characterClass = raiderIOProfile.characterClass,
            specialization = raiderIOProfile.activeSpecName,
            role = raiderIOProfile.activeSpecRole,
            itemLevel = raiderIOProfile.getItemLevel(),
            raiderIOScore = raiderIOProfile.getCurrentMythicPlusScore(),
            bestParseAverage = warcraftLogsResult?.bestPerformanceAverage,
            medianParseAverage = warcraftLogsResult?.medianPerformanceAverage,
            profileUrl = raiderIOProfile.profileUrl,
        )
    }

    /**
     * Fetches only Raider.IO data for a character.
     */
    fun fetchRaiderIOData(region: String, realm: String, name: String): RaiderIOCharacterProfile? {
        return raiderIOClient.fetchCharacterProfile(region, realm, name).block()
    }

    /**
     * Fetches only Warcraft Logs data for a character.
     */
    fun fetchWarcraftLogsData(region: String, realm: String, name: String): WarcraftLogsParseResult? {
        return warcraftLogsClient.fetchCharacterParses(region, realm, name).block()
    }
}

/**
 * Combined result from fetching character data from multiple sources.
 */
data class CharacterFetchResult(
    val characterName: String,
    val realm: String,
    val region: String,
    val characterClass: String,
    val specialization: String?,
    val role: String?,
    val itemLevel: Double?,
    val raiderIOScore: Double?,
    val bestParseAverage: Double?,
    val medianParseAverage: Double?,
    val profileUrl: String,
)

class CharacterNotFoundException(message: String) : RuntimeException(message)
