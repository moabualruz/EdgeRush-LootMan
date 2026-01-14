package com.edgerush.lootman.api.team

import com.edgerush.datasync.entity.TeamMetadataEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.team.repository.TeamMetadataRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Implementation of TeamMetadataCrudService.
 *
 * Provides CRUD operations for team metadata.
 */
@Service
class TeamMetadataCrudServiceImpl(
    private val repository: TeamMetadataRepository,
) : TeamMetadataCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<TeamMetadataResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { TeamMetadataResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): TeamMetadataResponse {
        val entity = repository.findById(id)
            ?: throw NoSuchElementException("Team metadata not found with teamId: $id")
        return TeamMetadataResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateTeamMetadataRequest): TeamMetadataResponse {
        val entity = TeamMetadataEntity(
            teamId = request.teamId,
            guildId = request.guildId,
            guildName = request.guildName,
            name = request.name,
            region = request.region,
            realm = request.realm,
            url = request.url,
            lastRefreshedBlizzard = request.lastRefreshedBlizzard,
            lastRefreshedPercentiles = request.lastRefreshedPercentiles,
            lastRefreshedMythicPlus = request.lastRefreshedMythicPlus,
            wishlistUpdatedAt = request.wishlistUpdatedAt,
            syncedAt = OffsetDateTime.now(),
        )
        val saved = repository.save(entity)
        return TeamMetadataResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateTeamMetadataRequest): TeamMetadataResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Team metadata not found with teamId: $id")

        val updated = existing.copy(
            guildId = request.guildId ?: existing.guildId,
            guildName = request.guildName ?: existing.guildName,
            name = request.name ?: existing.name,
            region = request.region ?: existing.region,
            realm = request.realm ?: existing.realm,
            url = request.url ?: existing.url,
            lastRefreshedBlizzard = request.lastRefreshedBlizzard ?: existing.lastRefreshedBlizzard,
            lastRefreshedPercentiles = request.lastRefreshedPercentiles ?: existing.lastRefreshedPercentiles,
            lastRefreshedMythicPlus = request.lastRefreshedMythicPlus ?: existing.lastRefreshedMythicPlus,
            wishlistUpdatedAt = request.wishlistUpdatedAt ?: existing.wishlistUpdatedAt,
        )

        repository.save(updated)
        return TeamMetadataResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Team metadata not found with teamId: $id")
        }
        repository.delete(id)
    }

    override fun findByGuildId(guildId: Long, pageRequest: PageRequest): PagedResponse<TeamMetadataResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByGuildId(guildId, offset, pageRequest.size)
        val total = repository.countByGuildId(guildId)

        return PagedResponse(
            content = entities.map { TeamMetadataResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findByRegion(region: String, pageRequest: PageRequest): PagedResponse<TeamMetadataResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByRegion(region, offset, pageRequest.size)
        val total = repository.countByRegion(region)

        return PagedResponse(
            content = entities.map { TeamMetadataResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun countByGuildId(guildId: Long): Long {
        return repository.countByGuildId(guildId)
    }
}
