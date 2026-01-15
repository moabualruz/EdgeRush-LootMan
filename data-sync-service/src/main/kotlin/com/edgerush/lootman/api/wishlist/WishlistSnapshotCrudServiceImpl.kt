package com.edgerush.lootman.api.wishlist

import com.edgerush.datasync.entity.WishlistSnapshotEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.wishlist.repository.WishlistSnapshotRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class WishlistSnapshotCrudServiceImpl(private val repository: WishlistSnapshotRepository) : WishlistSnapshotCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<WishlistSnapshotResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findAll(offset, pageRequest.size).map { WishlistSnapshotResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.count(),
        )
    }

    override fun findById(id: Long): WishlistSnapshotResponse =
        repository.findById(id)?.let { WishlistSnapshotResponse.from(it) }
            ?: throw NoSuchElementException("WishlistSnapshot not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateWishlistSnapshotRequest): WishlistSnapshotResponse {
        val entity =
            WishlistSnapshotEntity(
                null, request.raiderId, request.characterName, request.characterRealm, request.characterRegion,
                request.teamId, request.seasonId, request.periodId, request.rawPayload, OffsetDateTime.now(),
            )
        return WishlistSnapshotResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdateWishlistSnapshotRequest,
    ): WishlistSnapshotResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("WishlistSnapshot not found with id: $id")
        val updated = existing.copy(rawPayload = request.rawPayload ?: existing.rawPayload)
        return WishlistSnapshotResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("WishlistSnapshot not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(
        raiderId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<WishlistSnapshotResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findByRaiderId(raiderId, offset, pageRequest.size).map { WishlistSnapshotResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByRaiderId(raiderId),
        )
    }

    override fun findByTeamId(
        teamId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<WishlistSnapshotResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findByTeamId(teamId, offset, pageRequest.size).map { WishlistSnapshotResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.countByTeamId(teamId),
        )
    }

    override fun countByRaiderId(raiderId: Long): Long = repository.countByRaiderId(raiderId)

    override fun countByTeamId(teamId: Long): Long = repository.countByTeamId(teamId)
}
