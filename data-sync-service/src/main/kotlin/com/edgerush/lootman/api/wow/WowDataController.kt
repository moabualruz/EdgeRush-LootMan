package com.edgerush.lootman.api.wow

import com.edgerush.lootman.application.wow.SyncResult
import com.edgerush.lootman.application.wow.WowDataSyncService
import com.edgerush.lootman.domain.wow.model.WowClass
import com.edgerush.lootman.domain.wow.model.WowSpecialization
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for WoW class and specialization data.
 */
@RestController
@RequestMapping("/api/v1/wow")
class WowDataController(
    private val wowDataSyncService: WowDataSyncService,
) {
    /**
     * Get all WoW classes.
     */
    @GetMapping("/classes")
    fun getAllClasses(): ResponseEntity<List<WowClassDto>> {
        val classes = wowDataSyncService.getAllClasses()
        return ResponseEntity.ok(classes.map { it.toDto() })
    }

    /**
     * Get a specific WoW class by ID.
     */
    @GetMapping("/classes/{id}")
    fun getClassById(
        @PathVariable id: Int,
    ): ResponseEntity<WowClassDto> {
        val wowClass =
            wowDataSyncService.getAllClasses().find { it.id == id }
                ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(wowClass.toDto())
    }

    /**
     * Get all specializations for a class.
     */
    @GetMapping("/classes/{classId}/specializations")
    fun getSpecsForClass(
        @PathVariable classId: Int,
    ): ResponseEntity<List<WowSpecializationDto>> {
        val specs = wowDataSyncService.getSpecsForClass(classId)
        return ResponseEntity.ok(specs.map { it.toDto() })
    }

    /**
     * Get all WoW specializations.
     */
    @GetMapping("/specializations")
    fun getAllSpecializations(): ResponseEntity<List<WowSpecializationDto>> {
        val classes = wowDataSyncService.getAllClasses()
        val allSpecs = classes.flatMap { wowDataSyncService.getSpecsForClass(it.id) }
        return ResponseEntity.ok(allSpecs.map { it.toDto() })
    }

    /**
     * Trigger a sync of WoW classes and specializations from Blizzard API.
     * Requires admin role.
     */
    @PostMapping("/sync")
    fun syncWowData(): ResponseEntity<SyncResultDto> {
        val result = wowDataSyncService.syncAllClassesAndSpecs()
        return ResponseEntity.ok(result.toDto())
    }
}

// DTOs
data class WowClassDto(
    val id: Int,
    val name: String,
    val slug: String,
    val mediaUrl: String?,
    val powerType: String?,
)

data class WowSpecializationDto(
    val id: Int,
    val classId: Int,
    val name: String,
    val slug: String,
    val role: String,
    val mediaUrl: String?,
)

data class SyncResultDto(
    val success: Boolean,
    val classesAdded: Int,
    val classesUpdated: Int,
    val specsAdded: Int,
    val specsUpdated: Int,
    val totalClasses: Int,
    val totalSpecs: Int,
    val errors: List<String>,
)

// Extension functions
private fun WowClass.toDto() =
    WowClassDto(
        id = id,
        name = name,
        slug = slug,
        mediaUrl = mediaUrl,
        powerType = powerType,
    )

private fun WowSpecialization.toDto() =
    WowSpecializationDto(
        id = id,
        classId = classId,
        name = name,
        slug = slug,
        role = role.name,
        mediaUrl = mediaUrl,
    )

private fun SyncResult.toDto() =
    SyncResultDto(
        success = success,
        classesAdded = classesAdded,
        classesUpdated = classesUpdated,
        specsAdded = specsAdded,
        specsUpdated = specsUpdated,
        totalClasses = totalClasses,
        totalSpecs = totalSpecs,
        errors = errors,
    )
