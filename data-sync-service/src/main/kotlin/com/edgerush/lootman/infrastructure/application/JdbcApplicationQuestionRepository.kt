package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationQuestionEntity
import com.edgerush.lootman.domain.application.repository.ApplicationQuestionRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcApplicationQuestionRepository(private val jdbcTemplate: JdbcTemplate) : ApplicationQuestionRepository {
    override fun findById(id: Long) = jdbcTemplate.query("SELECT * FROM application_questions WHERE id = ?", rowMapper, id).firstOrNull()
    override fun existsById(id: Long) = (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_questions WHERE id = ?", Int::class.java, id) ?: 0) > 0
    override fun findAll(offset: Long, limit: Int) = jdbcTemplate.query("SELECT * FROM application_questions ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)
    override fun count() = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_questions", Long::class.java) ?: 0L
    override fun findByApplicationId(applicationId: Long, offset: Long, limit: Int) = jdbcTemplate.query("SELECT * FROM application_questions WHERE application_id = ? ORDER BY position LIMIT ? OFFSET ?", rowMapper, applicationId, limit, offset)
    override fun countByApplicationId(applicationId: Long) = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM application_questions WHERE application_id = ?", Long::class.java, applicationId) ?: 0L
    override fun save(entity: ApplicationQuestionEntity) = if (entity.id == null) insert(entity) else { update(entity); entity }
    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM application_questions WHERE id = ?", id) }
    private fun insert(entity: ApplicationQuestionEntity): ApplicationQuestionEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn -> conn.prepareStatement("INSERT INTO application_questions (application_id, position, question, answer, files_json) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS).apply {
            setLong(1, entity.applicationId); entity.position?.let { setInt(2, it) } ?: setNull(2, java.sql.Types.INTEGER)
            entity.question?.let { setString(3, it) } ?: setNull(3, java.sql.Types.VARCHAR); entity.answer?.let { setString(4, it) } ?: setNull(4, java.sql.Types.VARCHAR)
            entity.filesJson?.let { setString(5, it) } ?: setNull(5, java.sql.Types.VARCHAR) }
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }
    private fun update(entity: ApplicationQuestionEntity) { jdbcTemplate.update("UPDATE application_questions SET application_id=?, position=?, question=?, answer=?, files_json=? WHERE id=?", entity.applicationId, entity.position, entity.question, entity.answer, entity.filesJson, entity.id) }
    private val rowMapper = RowMapper { rs, _ -> fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }; ApplicationQuestionEntity(rs.getLong("id"), rs.getLong("application_id"), getIntOrNull("position"), rs.getString("question"), rs.getString("answer"), rs.getString("files_json")) }
}
