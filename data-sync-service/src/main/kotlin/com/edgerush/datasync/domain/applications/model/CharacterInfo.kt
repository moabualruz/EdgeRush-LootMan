package com.edgerush.datasync.domain.applications.model

/**
 * Value object representing character information
 */
data class CharacterInfo(
    val name: String,
    val realm: String,
    val region: String,
    val characterClass: String,
    val role: String,
    val race: String?,
    val faction: String?,
    val level: Int?
) {
    init {
        require(name.isNotBlank()) { "Character name cannot be blank" }
        require(realm.isNotBlank()) { "Character realm cannot be blank" }
        require(region.isNotBlank()) { "Character region cannot be blank" }
        require(characterClass.isNotBlank()) { "Character class cannot be blank" }
        require(role.isNotBlank()) { "Character role cannot be blank" }
        level?.let { require(it in 1..80) { "Character level must be between 1 and 80" } }
    }

    fun fullName(): String = "$name-$realm"
}
