package com.edgerush.lootman.infrastructure.flps

import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import com.edgerush.lootman.domain.flps.repository.FlpsDefaultModifierRepository
import com.edgerush.lootman.infrastructure.springdata.FlpsDefaultModifierEntitySpringRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository

/**
 * Implementation of FlpsDefaultModifierRepository that delegates to Spring Data JDBC.
 */
@Repository
class JdbcFlpsDefaultModifierRepository(
    private val springRepository: FlpsDefaultModifierEntitySpringRepository,
) : FlpsDefaultModifierRepository {

    override fun findById(id: Long): FlpsDefaultModifierEntity? =
        springRepository.findById(id).orElse(null)

    override fun existsById(id: Long): Boolean =
        springRepository.existsById(id)

    override fun findAll(offset: Long, limit: Int): List<FlpsDefaultModifierEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("category", "modifierKey"),
        )
        return springRepository.findAll(pageRequest).content
    }

    override fun count(): Long =
        springRepository.count()

    override fun findByCategory(category: String, offset: Long, limit: Int): List<FlpsDefaultModifierEntity> {
        val pageRequest = PageRequest.of(
            (offset / limit).toInt(),
            limit,
            Sort.by("modifierKey"),
        )
        return springRepository.findByCategory(category, pageRequest).content
    }

    override fun countByCategory(category: String): Long =
        springRepository.countByCategory(category)

    override fun save(modifier: FlpsDefaultModifierEntity): FlpsDefaultModifierEntity =
        springRepository.save(modifier)

    override fun delete(id: Long) {
        springRepository.deleteById(id)
    }
}
