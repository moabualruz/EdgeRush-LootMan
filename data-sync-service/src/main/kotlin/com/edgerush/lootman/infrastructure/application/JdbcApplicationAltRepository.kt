package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationAltEntity
import com.edgerush.lootman.domain.application.repository.ApplicationAltRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcApplicationAltRepository(private val jdbcTemplate: JdbcTemplate) : ApplicationAltRepository {

    override fun findById(id: Long): ApplicationAltEntity? =
        jdbcTemplate.query("SELECT * FROM application_alts WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_alts WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<ApplicationAltEntity> =
        jdbcTemplate.query("SELECT * FROM application_alts ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_alts", Long::class.java) ?: 0L

    override fun findByApplicationId(applicationId: Long, offset: Long, limit: Int): List<ApplicationAltEntity> =
        jdbcTemplate.query("SELECT * FROM application_alts WHERE application_id = ? ORDER BY id LIMIT ? OFFSET ?", rowMapper, applicationId, limit, offset)

    override fun countByApplicationId(applicationId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_alts WHERE application_id = ?", Long::class.java, applicationId) ?: 0L

    override fun save(entity: ApplicationAltEntity): ApplicationAltEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM application_alts WHERE id = ?", id) }

    private fun insert(entity: ApplicationAltEntity): ApplicationAltEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO application_alts (application_id, name, realm, region, class, role, level, faction, race) VALUES (?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.applicationId)
            entity.name?.let { ps.setString(2, it) } ?: ps.setNull(2, java.sql.Types.VARCHAR)
            entity.realm?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            entity.region?.let { ps.setString(4, it) } ?: ps.setNull(4, java.sql.Types.VARCHAR)
            entity.clazz?.let { ps.setString(5, it) } ?: ps.setNull(5, java.sql.Types.VARCHAR)
            entity.role?.let { ps.setString(6, it) } ?: ps.setNull(6, java.sql.Types.VARCHAR)
            entity.level?.let { ps.setInt(7, it) } ?: ps.setNull(7, java.sql.Types.INTEGER)
            entity.faction?.let { ps.setString(8, it) } ?: ps.setNull(8, java.sql.Types.VARCHAR)
            entity.race?.let { ps.setString(9, it) } ?: ps.setNull(9, java.sql.Types.VARCHAR)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: ApplicationAltEntity) {
        jdbcTemplate.update(
            "UPDATE application_alts SET application_id=?, name=?, realm=?, region=?, class=?, role=?, level=?, faction=?, race=? WHERE id=?",
            entity.applicationId, entity.name, entity.realm, entity.region, entity.clazz, entity.role, entity.level, entity.faction, entity.race, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }
        ApplicationAltEntity(
            rs.getLong("id"), rs.getLong("application_id"), rs.getString("name"), rs.getString("realm"),
            rs.getString("region"), rs.getString("class"), rs.getString("role"), getIntOrNull("level"),
            rs.getString("faction"), rs.getString("race")
        )
    }
}
