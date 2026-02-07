package com.edgerush.lootman.infrastructure.wow

import com.edgerush.lootman.domain.wow.model.WowClass
import com.edgerush.lootman.domain.wow.model.WowRole
import com.edgerush.lootman.domain.wow.model.WowSpecialization
import com.edgerush.lootman.domain.wow.repository.WowClassRepository
import com.edgerush.lootman.domain.wow.repository.WowSpecializationRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class JdbcWowClassRepository(
    private val jdbcTemplate: JdbcTemplate,
) : WowClassRepository {
    private val rowMapper =
        RowMapper<WowClass> { rs: ResultSet, _: Int ->
            WowClass(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                slug = rs.getString("slug"),
                mediaUrl = rs.getString("media_url"),
                powerType = rs.getString("power_type"),
                syncedAt = rs.getTimestamp("synced_at").toInstant(),
            )
        }

    override fun findById(id: Int): WowClass? {
        return jdbcTemplate.query(
            "SELECT * FROM wow_classes WHERE id = ?",
            rowMapper,
            id,
        ).firstOrNull()
    }

    override fun findByName(name: String): WowClass? {
        return jdbcTemplate.query(
            "SELECT * FROM wow_classes WHERE LOWER(name) = LOWER(?)",
            rowMapper,
            name,
        ).firstOrNull()
    }

    override fun findBySlug(slug: String): WowClass? {
        return jdbcTemplate.query(
            "SELECT * FROM wow_classes WHERE slug = ?",
            rowMapper,
            slug,
        ).firstOrNull()
    }

    override fun findAll(): List<WowClass> {
        return jdbcTemplate.query("SELECT * FROM wow_classes ORDER BY name", rowMapper)
    }

    override fun save(wowClass: WowClass): WowClass {
        jdbcTemplate.update(
            """
            INSERT INTO wow_classes (id, name, slug, media_url, power_type, synced_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                name = EXCLUDED.name,
                slug = EXCLUDED.slug,
                media_url = EXCLUDED.media_url,
                power_type = EXCLUDED.power_type,
                synced_at = EXCLUDED.synced_at
            """,
            wowClass.id,
            wowClass.name,
            wowClass.slug,
            wowClass.mediaUrl,
            wowClass.powerType,
            java.sql.Timestamp.from(wowClass.syncedAt),
        )
        return wowClass
    }

    override fun saveAll(classes: List<WowClass>): List<WowClass> {
        classes.forEach { save(it) }
        return classes
    }

    override fun deleteAll() {
        jdbcTemplate.update("DELETE FROM wow_classes")
    }
}

@Repository
class JdbcWowSpecializationRepository(
    private val jdbcTemplate: JdbcTemplate,
) : WowSpecializationRepository {
    private val rowMapper =
        RowMapper<WowSpecialization> { rs: ResultSet, _: Int ->
            WowSpecialization(
                id = rs.getInt("id"),
                classId = rs.getInt("class_id"),
                name = rs.getString("name"),
                slug = rs.getString("slug"),
                role = WowRole.fromString(rs.getString("role")),
                mediaUrl = rs.getString("media_url"),
                syncedAt = rs.getTimestamp("synced_at").toInstant(),
            )
        }

    override fun findById(id: Int): WowSpecialization? {
        return jdbcTemplate.query(
            "SELECT * FROM wow_specializations WHERE id = ?",
            rowMapper,
            id,
        ).firstOrNull()
    }

    override fun findByClassId(classId: Int): List<WowSpecialization> {
        return jdbcTemplate.query(
            "SELECT * FROM wow_specializations WHERE class_id = ? ORDER BY name",
            rowMapper,
            classId,
        )
    }

    override fun findByName(name: String): WowSpecialization? {
        return jdbcTemplate.query(
            "SELECT * FROM wow_specializations WHERE LOWER(name) = LOWER(?)",
            rowMapper,
            name,
        ).firstOrNull()
    }

    override fun findBySlug(slug: String): WowSpecialization? {
        return jdbcTemplate.query(
            "SELECT * FROM wow_specializations WHERE slug = ?",
            rowMapper,
            slug,
        ).firstOrNull()
    }

    override fun findAll(): List<WowSpecialization> {
        return jdbcTemplate.query("SELECT * FROM wow_specializations ORDER BY name", rowMapper)
    }

    override fun save(spec: WowSpecialization): WowSpecialization {
        jdbcTemplate.update(
            """
            INSERT INTO wow_specializations (id, class_id, name, slug, role, media_url, synced_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                class_id = EXCLUDED.class_id,
                name = EXCLUDED.name,
                slug = EXCLUDED.slug,
                role = EXCLUDED.role,
                media_url = EXCLUDED.media_url,
                synced_at = EXCLUDED.synced_at
            """,
            spec.id,
            spec.classId,
            spec.name,
            spec.slug,
            spec.role.name,
            spec.mediaUrl,
            java.sql.Timestamp.from(spec.syncedAt),
        )
        return spec
    }

    override fun saveAll(specs: List<WowSpecialization>): List<WowSpecialization> {
        specs.forEach { save(it) }
        return specs
    }

    override fun deleteAll() {
        jdbcTemplate.update("DELETE FROM wow_specializations")
    }
}
