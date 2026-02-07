package com.edgerush.lootman.infrastructure.external.blizzard

import com.edgerush.lootman.api.auth.OAuth2Properties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant

@Service
class BlizzardDataService(
    private val properties: OAuth2Properties,
    private val restTemplate: RestTemplate,
) {
    private val logger = LoggerFactory.getLogger(BlizzardDataService::class.java)

    private val clientId get() = properties.battlenet.clientId
    private val clientSecret get() = properties.battlenet.clientSecret
    private val region get() = properties.battlenet.region

    private var accessToken: String? = null
    private var tokenExpiry: Instant = Instant.MIN

    private fun getBaseUrl(): String {
        return if (region == "cn") "https://gateway.battlenet.com.cn" else "https://$region.api.blizzard.com"
    }

    private fun getAuthUrl(): String {
        return if (region == "cn") "https://www.battlenet.com.cn/oauth/token" else "https://$region.battle.net/oauth/token"
    }

    /**
     * Check if Blizzard API credentials are configured.
     */
    fun isConfigured(): Boolean = properties.battlenet.isConfigured()

    @Synchronized
    private fun getAccessToken(): String {
        if (!isConfigured()) {
            throw IllegalStateException(
                "Blizzard API credentials not configured. Set BATTLENET_CLIENT_ID and BATTLENET_CLIENT_SECRET environment variables.",
            )
        }

        if (accessToken != null && Instant.now().isBefore(tokenExpiry)) {
            return accessToken!!
        }

        // Fetch new client credentials token using RestTemplate (avoids WebClient.block() issues)
        val headers =
            HttpHeaders().apply {
                setBasicAuth(clientId, clientSecret)
                contentType = org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
            }

        val response =
            restTemplate.exchange(
                getAuthUrl(),
                HttpMethod.POST,
                HttpEntity("grant_type=client_credentials", headers),
                TokenResponse::class.java,
            )

        val tokenResponse =
            response.body
                ?: throw IllegalStateException("Failed to retrieve Battle.net access token")

        accessToken = tokenResponse.access_token
        tokenExpiry = Instant.now().plusSeconds(tokenResponse.expires_in - 60) // Buffer
        return accessToken!!
    }

    fun getAccountCharacters(userAccessToken: String): List<BlizzardProfileCharacter> {
        val url = "https://${getAsBaseUrl()}/profile/user/wow?namespace=profile-$region&locale=en_US"
        logger.info("Fetching WoW characters from: $url")

        try {
            // Use RestTemplate for synchronous call (more reliable than WebClient.block())
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(userAccessToken)
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardAccountProfileResponse::class.java,
                )

            val body = response.body
            logger.info("Blizzard API response: ${body?.wow_accounts?.size ?: 0} WoW accounts found")

            val characters =
                body?.wow_accounts?.flatMap { account ->
                    logger.info("Account has ${account.characters.size} characters")
                    account.characters
                } ?: emptyList()

            logger.info("Total characters fetched: ${characters.size}")
            characters.take(5).forEach { char ->
                logger.info("  - ${char.name} (${char.realm.name}) Level ${char.level} ${char.playable_class.name}")
            }

            return characters
        } catch (e: Exception) {
            logger.error("Failed to fetch Battle.net characters: ${e.message}", e)
            return emptyList()
        }
    }

    private fun getAsBaseUrl(): String {
        return if (region == "cn") "gateway.battlenet.com.cn" else "$region.api.blizzard.com"
    }

    fun getRaids(): List<BlizzardRaid> {
        // Hardcoding current expansion raid tier ID (e.g., Nerub-ar Palace) logic or fetching 'current tier'
        // For now, let's fetch instances from the Journal API
        // https://us.api.blizzard.com/data/wow/journal-instance/index?namespace=static-us

        val url = "${getBaseUrl()}/data/wow/journal-instance/index?namespace=static-$region&locale=en_US"
        logger.info("Fetching raids from: $url")

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    JournalInstanceIndexResponse::class.java,
                )

            val raids =
                response.body?.instances?.map {
                    BlizzardRaid(it.id, it.name)
                } ?: emptyList()
            logger.info("Fetched ${raids.size} raids")
            return raids
        } catch (e: Exception) {
            logger.error("Failed to fetch raids: ${e.message}", e)
            return emptyList()
        }
    }

    fun getRaidMaps(instanceId: Int): List<BlizzardMap> {
        // 1. Get Instance Details to find description/background if needed
        // 2. We actually need encounters to get maps, or journal-instance/{id} which usually lists maps/encounters

        val url = "${getBaseUrl()}/data/wow/journal-instance/$instanceId?namespace=static-$region&locale=en_US"
        logger.info("Fetching raid maps for instance $instanceId from: $url")

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    JournalInstanceDetailResponse::class.java,
                )

            val maps =
                response.body?.maps?.map {
                    BlizzardMap(it.id, it.name, it.description)
                } ?: emptyList()
            logger.info("Fetched ${maps.size} maps for instance $instanceId")
            return maps
        } catch (e: Exception) {
            logger.error("Failed to fetch raid maps for instance $instanceId: ${e.message}", e)
            return emptyList()
        }
    }

    fun getMap(mapId: Int): BlizzardMapDetails? {
        // Fetch specific map data, ideally generating a tile URL or direct image URL if available
        // The Game Data API 'media' endpoint for Journal Media is often where the images live.
        throw NotImplementedError("Map detail fetching needs refinement on exact Blizzard endpoint")
    }

    /**
     * Fetches all playable classes from Blizzard API.
     * Uses client credentials (not user token) as this is game data.
     */
    fun getPlayableClasses(): List<BlizzardPlayableClass> {
        val url = "${getBaseUrl()}/data/wow/playable-class/index?namespace=static-$region&locale=en_US"
        logger.info("Fetching playable classes from: $url")

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardPlayableClassIndex::class.java,
                )

            val classes = response.body?.classes ?: emptyList()
            logger.info("Fetched ${classes.size} playable classes")
            return classes
        } catch (e: Exception) {
            logger.error("Failed to fetch playable classes: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Fetches details for a specific class including specializations.
     */
    fun getPlayableClassDetails(classId: Int): BlizzardPlayableClassDetail? {
        val url = "${getBaseUrl()}/data/wow/playable-class/$classId?namespace=static-$region&locale=en_US"
        logger.info("Fetching class details for ID $classId")

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardPlayableClassDetail::class.java,
                )

            return response.body
        } catch (e: Exception) {
            logger.error("Failed to fetch class details for ID $classId: ${e.message}", e)
            return null
        }
    }

    /**
     * Fetches all playable specializations from Blizzard API.
     */
    fun getPlayableSpecializations(): List<BlizzardPlayableSpecialization> {
        val url = "${getBaseUrl()}/data/wow/playable-specialization/index?namespace=static-$region&locale=en_US"
        logger.info("Fetching playable specializations from: $url")

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardPlayableSpecIndex::class.java,
                )

            val specs = response.body?.character_specializations ?: emptyList()
            logger.info("Fetched ${specs.size} playable specializations")
            return specs
        } catch (e: Exception) {
            logger.error("Failed to fetch playable specializations: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Fetches details for a specific specialization including role.
     */
    fun getSpecializationDetails(specId: Int): BlizzardSpecializationDetail? {
        val url = "${getBaseUrl()}/data/wow/playable-specialization/$specId?namespace=static-$region&locale=en_US"

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardSpecializationDetail::class.java,
                )

            return response.body
        } catch (e: Exception) {
            logger.error("Failed to fetch spec details for ID $specId: ${e.message}", e)
            return null
        }
    }

    /**
     * Fetches media (icon) for a class.
     */
    fun getClassMedia(classId: Int): String? {
        val url = "${getBaseUrl()}/data/wow/media/playable-class/$classId?namespace=static-$region"

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardMediaResponse::class.java,
                )

            return response.body?.assets?.firstOrNull { it.key == "icon" }?.value
        } catch (e: Exception) {
            logger.error("Failed to fetch class media for ID $classId: ${e.message}", e)
            return null
        }
    }

    /**
     * Fetches media (icon) for a specialization.
     */
    fun getSpecMedia(specId: Int): String? {
        val url = "${getBaseUrl()}/data/wow/media/playable-specialization/$specId?namespace=static-$region"

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardMediaResponse::class.java,
                )

            return response.body?.assets?.firstOrNull { it.key == "icon" }?.value
        } catch (e: Exception) {
            logger.error("Failed to fetch spec media for ID $specId: ${e.message}", e)
            return null
        }
    }

    /**
     * Fetches guild roster from Blizzard API.
     * Uses client credentials (game data API).
     *
     * @param realmSlug The realm slug (e.g., "twisting-nether")
     * @param guildNameSlug The guild name as a slug (e.g., "dod" for "DoD")
     * @return List of guild members with their character info
     */
    fun getGuildRoster(
        realmSlug: String,
        guildNameSlug: String,
    ): List<BlizzardGuildMember> {
        val url = "${getBaseUrl()}/data/wow/guild/$realmSlug/$guildNameSlug/roster?namespace=profile-$region&locale=en_US"
        logger.info("Fetching guild roster from: $url")

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardGuildRosterResponse::class.java,
                )

            val members = response.body?.members ?: emptyList()
            logger.info("Fetched ${members.size} guild members")
            return members
        } catch (e: Exception) {
            logger.error("Failed to fetch guild roster for $guildNameSlug@$realmSlug: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Fetches guild information from Blizzard API.
     */
    fun getGuildInfo(
        realmSlug: String,
        guildNameSlug: String,
    ): BlizzardGuildInfo? {
        val url = "${getBaseUrl()}/data/wow/guild/$realmSlug/$guildNameSlug?namespace=profile-$region&locale=en_US"
        logger.info("Fetching guild info from: $url")

        try {
            val headers =
                HttpHeaders().apply {
                    setBearerAuth(getAccessToken())
                }

            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    BlizzardGuildInfo::class.java,
                )

            return response.body
        } catch (e: Exception) {
            logger.error("Failed to fetch guild info for $guildNameSlug@$realmSlug: ${e.message}", e)
            return null
        }
    }
}

data class TokenResponse(val access_token: String, val expires_in: Long)

data class JournalInstanceIndexResponse(val instances: List<JournalInstanceRef>)

data class JournalInstanceRef(val id: Int, val name: String)

data class JournalInstanceDetailResponse(val id: Int, val name: String, val maps: List<JournalMapRef> = emptyList())

data class JournalMapRef(val id: Int, val name: String, val description: String?)

data class BlizzardAccountProfileResponse(val wow_accounts: List<BlizzardWowAccount> = emptyList())

data class BlizzardWowAccount(val characters: List<BlizzardProfileCharacter> = emptyList())

data class BlizzardProfileCharacter(
    val id: Long,
    val name: String,
    val realm: BlizzardRealm,
    val level: Int,
    val playable_class: BlizzardKeyName,
    val playable_race: BlizzardKeyName,
    val faction: BlizzardKeyName,
    val guild: BlizzardCharacterGuild? = null, // Guild info if available
)

data class BlizzardRealm(val name: String, val slug: String? = null)

data class BlizzardCharacterGuild(
    val name: String,
    val realm: BlizzardRealm,
)

data class BlizzardKeyName(val name: String)

data class BlizzardRaid(val id: Int, val name: String)

data class BlizzardMap(val id: Int, val name: String, val description: String?)

data class BlizzardMapDetails(val id: Int, val imageUrl: String)

// Playable Class API responses
data class BlizzardPlayableClassIndex(val classes: List<BlizzardPlayableClass> = emptyList())

data class BlizzardPlayableClass(
    val id: Int,
    val name: String,
    val key: BlizzardKeyRef? = null,
)

data class BlizzardPlayableClassDetail(
    val id: Int,
    val name: String,
    val power_type: BlizzardKeyName? = null,
    val specializations: List<BlizzardSpecRef> = emptyList(),
)

data class BlizzardSpecRef(val id: Int, val name: String)

data class BlizzardKeyRef(val href: String)

// Playable Specialization API responses
data class BlizzardPlayableSpecIndex(val character_specializations: List<BlizzardPlayableSpecialization> = emptyList())

data class BlizzardPlayableSpecialization(
    val id: Int,
    val name: String,
    val key: BlizzardKeyRef? = null,
)

data class BlizzardSpecializationDetail(
    val id: Int,
    val name: String,
    val playable_class: BlizzardClassRef,
    val role: BlizzardRoleType? = null,
)

data class BlizzardClassRef(val id: Int, val name: String)

data class BlizzardRoleType(val type: String, val name: String)

// Media API responses
data class BlizzardMediaResponse(val assets: List<BlizzardMediaAsset> = emptyList())

data class BlizzardMediaAsset(val key: String, val value: String)

// Guild Roster API responses
data class BlizzardGuildRosterResponse(
    val guild: BlizzardGuildRef,
    val members: List<BlizzardGuildMember> = emptyList(),
)

data class BlizzardGuildRef(
    val id: Long,
    val name: String,
    val realm: BlizzardRealmRef,
)

data class BlizzardRealmRef(
    val id: Int,
    val name: String? = null,
    val slug: String? = null,
)

data class BlizzardGuildMember(
    val character: BlizzardGuildCharacter,
    val rank: Int,
)

data class BlizzardGuildCharacter(
    val id: Long,
    val name: String,
    val realm: BlizzardRealmRef,
    val level: Int,
    val playable_class: BlizzardKeyId,
    val playable_race: BlizzardKeyId,
)

data class BlizzardKeyId(val id: Int)

// Guild Info API response
data class BlizzardGuildInfo(
    val id: Long,
    val name: String,
    val faction: BlizzardFaction,
    val achievement_points: Int,
    val member_count: Int,
    val realm: BlizzardRealmRef,
    val created_timestamp: Long? = null,
)

data class BlizzardFaction(val type: String, val name: String)
