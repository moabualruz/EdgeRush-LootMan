package com.edgerush.datasync.repository.warcraftlogs

import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsConfigEntity
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WarcraftLogsConfigRepository : CrudRepository<WarcraftLogsConfigEntity, String> {
    @Query("SELECT * FROM warcraft_logs_config WHERE enabled = true")
    fun findAllEnabled(): List<WarcraftLogsConfigEntity>

    fun findByGuildId(guildId: String): WarcraftLogsConfigEntity?
}
