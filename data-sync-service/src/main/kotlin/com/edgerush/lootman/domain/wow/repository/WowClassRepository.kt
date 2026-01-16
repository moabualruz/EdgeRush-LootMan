package com.edgerush.lootman.domain.wow.repository

import com.edgerush.lootman.domain.wow.model.WowClass
import com.edgerush.lootman.domain.wow.model.WowSpecialization

/**
 * Repository for WoW classes.
 */
interface WowClassRepository {
    fun findById(id: Int): WowClass?
    fun findByName(name: String): WowClass?
    fun findBySlug(slug: String): WowClass?
    fun findAll(): List<WowClass>
    fun save(wowClass: WowClass): WowClass
    fun saveAll(classes: List<WowClass>): List<WowClass>
    fun deleteAll()
}

/**
 * Repository for WoW specializations.
 */
interface WowSpecializationRepository {
    fun findById(id: Int): WowSpecialization?
    fun findByClassId(classId: Int): List<WowSpecialization>
    fun findByName(name: String): WowSpecialization?
    fun findBySlug(slug: String): WowSpecialization?
    fun findAll(): List<WowSpecialization>
    fun save(spec: WowSpecialization): WowSpecialization
    fun saveAll(specs: List<WowSpecialization>): List<WowSpecialization>
    fun deleteAll()
}
