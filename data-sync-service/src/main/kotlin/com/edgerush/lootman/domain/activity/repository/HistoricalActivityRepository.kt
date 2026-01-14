package com.edgerush.lootman.domain.activity.repository

import com.edgerush.datasync.entity.HistoricalActivityEntity

interface HistoricalActivityRepository {
    fun findById(id: Long): HistoricalActivityEntity?
    fun existsById(id: Long): Boolean
    fun findAll(offset: Long, limit: Int): List<HistoricalActivityEntity>
    fun count(): Long
    fun findByCharacterId(characterId: Long, offset: Long, limit: Int): List<HistoricalActivityEntity>
    fun countByCharacterId(characterId: Long): Long
    fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<HistoricalActivityEntity>
    fun countByTeamId(teamId: Long): Long
    fun save(entity: HistoricalActivityEntity): HistoricalActivityEntity
    fun delete(id: Long)
}
