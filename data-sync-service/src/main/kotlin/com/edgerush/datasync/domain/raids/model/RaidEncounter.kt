package com.edgerush.datasync.domain.raids.model

/**
 * Entity representing a raid encounter (boss fight).
 */
data class RaidEncounter private constructor(
    val id: Long?,
    val encounterId: Long?,
    val name: String,
    val enabled: Boolean,
    val extra: Boolean,
    val notes: String?
) {
    init {
        require(name.isNotBlank()) { "Encounter name cannot be blank" }
    }
    
    companion object {
        fun create(
            encounterId: Long?,
            name: String,
            enabled: Boolean = true,
            extra: Boolean = false,
            notes: String? = null
        ): RaidEncounter {
            return RaidEncounter(
                id = null,
                encounterId = encounterId,
                name = name,
                enabled = enabled,
                extra = extra,
                notes = notes
            )
        }
        
        fun reconstitute(
            id: Long,
            encounterId: Long?,
            name: String,
            enabled: Boolean,
            extra: Boolean,
            notes: String?
        ): RaidEncounter {
            return RaidEncounter(
                id = id,
                encounterId = encounterId,
                name = name,
                enabled = enabled,
                extra = extra,
                notes = notes
            )
        }
    }
    
    fun disable(): RaidEncounter = copy(enabled = false)
    
    fun enable(): RaidEncounter = copy(enabled = true)
    
    fun updateNotes(newNotes: String?): RaidEncounter = copy(notes = newNotes)
}
