package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class RaiderEntityCrudServiceImpl(
    private val repository: RaiderEntityRepository,
) : RaiderEntityCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderEntityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        return PagedResponse(entities.map { RaiderEntityResponse.from(it) }, pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): RaiderEntityResponse {
        val entity = repository.findById(id) ?: throw NoSuchElementException("Raider not found with id: $id")
        return RaiderEntityResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderEntityRequest): RaiderEntityResponse {
        val entity =
            RaiderEntity(
                characterName = request.characterName,
                realm = request.realm,
                region = request.region,
                guildId = request.guildId,
                wowauditId = request.wowauditId,
                clazz = request.clazz,
                spec = request.spec,
                role = request.role,
                rank = request.rank,
                status = request.status,
                note = request.note,
                blizzardId = request.blizzardId,
                trackingSince = request.trackingSince,
                joinDate = request.joinDate,
                blizzardLastModified = null,
                lastSync = OffsetDateTime.now(),
            )
        return RaiderEntityResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdateRaiderEntityRequest,
    ): RaiderEntityResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("Raider not found with id: $id")
        val updated =
            existing.copy(
                guildId = request.guildId ?: existing.guildId,
                spec = request.spec ?: existing.spec,
                role = request.role ?: existing.role,
                rank = request.rank ?: existing.rank,
                status = request.status ?: existing.status,
                note = request.note ?: existing.note,
            )
        repository.save(updated)
        return RaiderEntityResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("Raider not found with id: $id")
        repository.delete(id)
    }

    override fun findByRealm(
        realm: String,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderEntityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByRealm(realm, offset, pageRequest.size)
        return PagedResponse(
            entities.map { RaiderEntityResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByRealm(realm),
        )
    }

    override fun findByRegion(
        region: String,
        pageRequest: PageRequest,
    ): PagedResponse<RaiderEntityResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByRegion(region, offset, pageRequest.size)
        return PagedResponse(
            entities.map { RaiderEntityResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByRegion(region),
        )
    }

    override fun countByRealm(realm: String): Long = repository.countByRealm(realm)
}
