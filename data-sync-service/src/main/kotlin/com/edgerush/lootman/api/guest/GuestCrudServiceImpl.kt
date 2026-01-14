package com.edgerush.lootman.api.guest

import com.edgerush.datasync.entity.GuestEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.guest.repository.GuestRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class GuestCrudServiceImpl(private val repository: GuestRepository) : GuestCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<GuestResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(repository.findAll(offset, pageRequest.size).map { GuestResponse.from(it) },
            pageRequest.page, pageRequest.size, repository.count())
    }

    override fun findById(id: Long): GuestResponse = repository.findById(id)?.let { GuestResponse.from(it) }
        ?: throw NoSuchElementException("Guest not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateGuestRequest): GuestResponse {
        val entity = GuestEntity(request.guestId, request.name, request.realm, request.clazz, request.role,
            request.blizzardId, request.trackingSince, OffsetDateTime.now())
        return GuestResponse.from(repository.save(entity))
    }

    override fun update(id: Long, request: UpdateGuestRequest): GuestResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("Guest not found with id: $id")
        val updated = existing.copy(name = request.name ?: existing.name, realm = request.realm ?: existing.realm,
            clazz = request.clazz ?: existing.clazz, role = request.role ?: existing.role)
        return GuestResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("Guest not found with id: $id")
        repository.delete(id)
    }
}
