package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import com.edgerush.lootman.domain.application.repository.ApplicationQuestionFileRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcApplicationQuestionFileRepository(private val jdbcTemplate: JdbcTemplate) : ApplicationQuestionFileRepository {

    override fun findById(id: Long): ApplicationQuestionFileEntity? =
        jdbcTemplate.query("SELECT * FROM application_question_files WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_question_files WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<ApplicationQuestionFileEntity> =
        jdbcTemplate.query("SELECT * FROM application_question_files ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_question_files", Long::class.java) ?: 0L

    override fun findByApplicationId(applicationId: Long, offset: Long, limit: Int): List<ApplicationQuestionFileEntity> =
        jdbcTemplate.query("SELECT * FROM application_question_files WHERE application_id = ? ORDER BY question_position LIMIT ? OFFSET ?", rowMapper, applicationId, limit, offset)

    override fun countByApplicationId(applicationId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_question_files WHERE application_id = ?", Long::class.java, applicationId) ?: 0L

    override fun save(entity: ApplicationQuestionFileEntity): ApplicationQuestionFileEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM application_question_files WHERE id = ?", id) }

    private fun insert(entity: ApplicationQuestionFileEntity): ApplicationQuestionFileEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO application_question_files (application_id, question_position, question, original_filename, url) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.applicationId)
            entity.questionPosition?.let { ps.setInt(2, it) } ?: ps.setNull(2, java.sql.Types.INTEGER)
            entity.question?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            entity.originalFilename?.let { ps.setString(4, it) } ?: ps.setNull(4, java.sql.Types.VARCHAR)
            entity.url?.let { ps.setString(5, it) } ?: ps.setNull(5, java.sql.Types.VARCHAR)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: ApplicationQuestionFileEntity) {
        jdbcTemplate.update(
            "UPDATE application_question_files SET application_id=?, question_position=?, question=?, original_filename=?, url=? WHERE id=?",
            entity.applicationId, entity.questionPosition, entity.question, entity.originalFilename, entity.url, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }
        ApplicationQuestionFileEntity(
            rs.getLong("id"), rs.getLong("application_id"), getIntOrNull("question_position"),
            rs.getString("question"), rs.getString("original_filename"), rs.getString("url")
        )
    }
}
