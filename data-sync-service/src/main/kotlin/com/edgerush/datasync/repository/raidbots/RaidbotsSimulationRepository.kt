package com.edgerush.datasync.repository.raidbots

import com.edgerush.datasync.entity.raidbots.RaidbotsSimulationEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RaidbotsSimulationRepository : CrudRepository<RaidbotsSimulationEntity, Long> {
    
    fun findBySimId(simId: String): RaidbotsSimulationEntity?
    
    @Query("SELECT * FROM raidbots_simulations WHERE character_name = :name AND character_realm = :realm ORDER BY submitted_at DESC")
    fun findByCharacter(@Param("name") name: String, @Param("realm") realm: String): List<RaidbotsSimulationEntity>
    
    @Query("SELECT * FROM raidbots_simulations WHERE status = :status")
    fun findByStatus(@Param("status") status: String): List<RaidbotsSimulationEntity>
}
