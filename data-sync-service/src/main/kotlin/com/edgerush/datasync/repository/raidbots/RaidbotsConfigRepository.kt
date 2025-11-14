package com.edgerush.datasync.repository.raidbots

import com.edgerush.datasync.entity.raidbots.RaidbotsConfigEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaidbotsConfigRepository : CrudRepository<RaidbotsConfigEntity, String> {
    
    @Query("SELECT * FROM raidbots_config WHERE enabled = true")
    fun findAllEnabled(): List<RaidbotsConfigEntity>
}
