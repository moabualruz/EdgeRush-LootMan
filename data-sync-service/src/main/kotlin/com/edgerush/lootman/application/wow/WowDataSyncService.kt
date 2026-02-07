package com.edgerush.lootman.application.wow

import com.edgerush.lootman.domain.wow.model.WowClass
import com.edgerush.lootman.domain.wow.model.WowRole
import com.edgerush.lootman.domain.wow.model.WowSpecialization
import com.edgerush.lootman.domain.wow.repository.WowClassRepository
import com.edgerush.lootman.domain.wow.repository.WowSpecializationRepository
import com.edgerush.lootman.infrastructure.external.blizzard.BlizzardDataService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Service for syncing WoW classes and specializations from Blizzard API.
 */
@Service
class WowDataSyncService(
    private val blizzardDataService: BlizzardDataService,
    private val wowClassRepository: WowClassRepository,
    private val wowSpecializationRepository: WowSpecializationRepository,
) {
    private val logger = LoggerFactory.getLogger(WowDataSyncService::class.java)

    /**
     * Check if Blizzard API is configured.
     */
    fun isBlizzardApiConfigured(): Boolean = blizzardDataService.isConfigured()

    /**
     * Syncs all playable classes and their specializations from Blizzard API.
     * This should be called on application startup and periodically (e.g., daily).
     */
    fun syncAllClassesAndSpecs(): SyncResult {
        logger.info("Starting WoW classes and specializations sync from Blizzard API")

        // Check if Blizzard API credentials are configured
        if (!isBlizzardApiConfigured()) {
            logger.warn(
                "Blizzard API credentials not configured, skipping WoW data sync. Set BATTLENET_CLIENT_ID and BATTLENET_CLIENT_SECRET to enable.",
            )
            return SyncResult(
                classesAdded = 0,
                classesUpdated = 0,
                specsAdded = 0,
                specsUpdated = 0,
                errors = listOf("Blizzard API credentials not configured"),
            )
        }

        var classesAdded = 0
        var classesUpdated = 0
        var specsAdded = 0
        var specsUpdated = 0
        val errors = mutableListOf<String>()

        try {
            // Fetch all playable classes
            val blizzardClasses = blizzardDataService.getPlayableClasses()
            logger.info("Fetched ${blizzardClasses.size} playable classes from Blizzard")

            for (blizzardClass in blizzardClasses) {
                try {
                    // Get detailed class info including power type
                    val classDetail = blizzardDataService.getPlayableClassDetails(blizzardClass.id)
                    val mediaUrl = blizzardDataService.getClassMedia(blizzardClass.id)

                    val existingClass = wowClassRepository.findById(blizzardClass.id)

                    val wowClass =
                        WowClass(
                            id = blizzardClass.id,
                            name = blizzardClass.name,
                            slug = blizzardClass.name.lowercase().replace(" ", "-"),
                            mediaUrl = mediaUrl,
                            powerType = classDetail?.power_type?.name,
                            syncedAt = Instant.now(),
                        )

                    wowClassRepository.save(wowClass)

                    if (existingClass == null) {
                        classesAdded++
                        logger.info("Added new class: ${wowClass.name}")
                    } else {
                        classesUpdated++
                        logger.debug("Updated class: ${wowClass.name}")
                    }

                    // Sync specializations for this class
                    classDetail?.specializations?.forEach { specRef ->
                        try {
                            val specDetail = blizzardDataService.getSpecializationDetails(specRef.id)
                            val specMediaUrl = blizzardDataService.getSpecMedia(specRef.id)

                            if (specDetail != null) {
                                val existingSpec = wowSpecializationRepository.findById(specRef.id)

                                val role = specDetail.role?.type?.let { WowRole.fromString(it) } ?: WowRole.DPS

                                val wowSpec =
                                    WowSpecialization(
                                        id = specRef.id,
                                        classId = blizzardClass.id,
                                        name = specRef.name,
                                        slug = specRef.name.lowercase().replace(" ", "-"),
                                        role = role,
                                        mediaUrl = specMediaUrl,
                                        syncedAt = Instant.now(),
                                    )

                                wowSpecializationRepository.save(wowSpec)

                                if (existingSpec == null) {
                                    specsAdded++
                                    logger.info("Added new spec: ${wowSpec.name} for ${wowClass.name}")
                                } else {
                                    specsUpdated++
                                    logger.debug("Updated spec: ${wowSpec.name}")
                                }
                            }
                        } catch (e: Exception) {
                            val errorMsg = "Failed to sync spec ${specRef.name}: ${e.message}"
                            logger.error(errorMsg, e)
                            errors.add(errorMsg)
                        }
                    }
                } catch (e: Exception) {
                    val errorMsg = "Failed to sync class ${blizzardClass.name}: ${e.message}"
                    logger.error(errorMsg, e)
                    errors.add(errorMsg)
                }
            }

            logger.info(
                "WoW data sync completed: $classesAdded classes added, $classesUpdated updated, $specsAdded specs added, $specsUpdated updated",
            )
        } catch (e: Exception) {
            val errorMsg = "Fatal error during WoW data sync: ${e.message}"
            logger.error(errorMsg, e)
            errors.add(errorMsg)
        }

        return SyncResult(
            classesAdded = classesAdded,
            classesUpdated = classesUpdated,
            specsAdded = specsAdded,
            specsUpdated = specsUpdated,
            errors = errors,
        )
    }

    /**
     * Gets a WowClass by name, with flexible matching.
     */
    fun findClassByName(name: String): WowClass? {
        // Try exact match first
        wowClassRepository.findByName(name)?.let { return it }

        // Try slug match
        val slug = name.lowercase().replace(" ", "-")
        wowClassRepository.findBySlug(slug)?.let { return it }

        // Try normalized match (e.g., "DeathKnight" -> "Death Knight")
        val normalized = name.replace(Regex("([a-z])([A-Z])"), "$1 $2")
        wowClassRepository.findByName(normalized)?.let { return it }

        return null
    }

    /**
     * Gets all classes.
     */
    fun getAllClasses(): List<WowClass> = wowClassRepository.findAll()

    /**
     * Gets all specs for a class.
     */
    fun getSpecsForClass(classId: Int): List<WowSpecialization> = wowSpecializationRepository.findByClassId(classId)

    /**
     * Gets a spec by name with flexible matching.
     */
    fun findSpecByName(name: String): WowSpecialization? {
        wowSpecializationRepository.findByName(name)?.let { return it }

        val slug = name.lowercase().replace(" ", "-")
        wowSpecializationRepository.findBySlug(slug)?.let { return it }

        return null
    }
}

data class SyncResult(
    val classesAdded: Int,
    val classesUpdated: Int,
    val specsAdded: Int,
    val specsUpdated: Int,
    val errors: List<String>,
) {
    val success: Boolean get() = errors.isEmpty()
    val totalClasses: Int get() = classesAdded + classesUpdated
    val totalSpecs: Int get() = specsAdded + specsUpdated
}
