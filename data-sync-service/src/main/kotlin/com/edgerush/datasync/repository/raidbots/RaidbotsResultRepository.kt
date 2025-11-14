package com.edgerush.datasync.repository.raidbots

import com.edgerush.datasync.entity.raidbots.RaidbotsResultEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RaidbotsResultRepository : CrudRepository<RaidbotsResultEntity, Long> {
    
    fun findBySimulationId(simulationId: Long): List<RaidbotsResultEntity>
    
    @Query("SELECT * FROM raidbots_results WHERE item_id = :itemId ORDER BY calculated_at DESC")
    fun findByItemId(@Param("itemId") itemId: Long): List<RaidbotsResultEntity>
}
