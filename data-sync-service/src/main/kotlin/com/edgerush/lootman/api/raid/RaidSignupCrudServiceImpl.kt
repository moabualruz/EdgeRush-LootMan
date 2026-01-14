package com.edgerush.lootman.api.raid

import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.raids.repository.RaidSignupRepository
import org.springframework.stereotype.Service

/**
 * Implementation of RaidSignupCrudService.
 *
 * Provides CRUD operations for RaidSignups using the domain repository.
 */
@Service
class RaidSignupCrudServiceImpl(
    private val signupRepository: RaidSignupRepository,
) : RaidSignupCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<RaidSignupResponse> {
        val signups = signupRepository.findAll(pageRequest.offset, pageRequest.size)
        val total = signupRepository.count()
        return PagedResponse.of(
            content = signups.map { RaidSignupResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findById(id: Long): RaidSignupResponse {
        val signup = signupRepository.findById(id)
            ?: throw NoSuchElementException("Signup not found with id: $id")
        return RaidSignupResponse.from(signup)
    }

    override fun create(request: CreateRaidSignupRequest): RaidSignupResponse {
        val entity = RaidSignupEntity(
            id = null,
            raidId = request.raidId,
            characterId = request.characterId,
            characterName = request.characterName,
            characterRealm = request.characterRealm,
            characterRegion = request.characterRegion,
            characterClass = request.characterClass,
            characterRole = request.characterRole,
            characterGuest = request.characterGuest,
            status = request.status,
            comment = request.comment,
            selected = request.selected,
        )
        val saved = signupRepository.save(entity)
        return RaidSignupResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateRaidSignupRequest): RaidSignupResponse {
        val existing = signupRepository.findById(id)
            ?: throw NoSuchElementException("Signup not found with id: $id")

        val updated = existing.copy(
            characterId = request.characterId ?: existing.characterId,
            characterName = request.characterName ?: existing.characterName,
            characterRealm = request.characterRealm ?: existing.characterRealm,
            characterRegion = request.characterRegion ?: existing.characterRegion,
            characterClass = request.characterClass ?: existing.characterClass,
            characterRole = request.characterRole ?: existing.characterRole,
            characterGuest = request.characterGuest ?: existing.characterGuest,
            status = request.status ?: existing.status,
            comment = request.comment ?: existing.comment,
            selected = request.selected ?: existing.selected,
        )

        val saved = signupRepository.save(updated)
        return RaidSignupResponse.from(saved)
    }

    override fun delete(id: Long) {
        if (!signupRepository.existsById(id)) {
            throw NoSuchElementException("Signup not found with id: $id")
        }
        signupRepository.delete(id)
    }

    override fun existsById(id: Long): Boolean =
        signupRepository.existsById(id)

    override fun findByRaid(raidId: Long, pageRequest: PageRequest): PagedResponse<RaidSignupResponse> {
        val signups = signupRepository.findByRaidId(raidId, pageRequest.offset, pageRequest.size)
        val total = signupRepository.countByRaidId(raidId)
        return PagedResponse.of(
            content = signups.map { RaidSignupResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findSelectedByRaid(raidId: Long, pageRequest: PageRequest): PagedResponse<RaidSignupResponse> {
        val signups = signupRepository.findSelectedByRaidId(raidId, pageRequest.offset, pageRequest.size)
        val total = signupRepository.countSelectedByRaidId(raidId)
        return PagedResponse.of(
            content = signups.map { RaidSignupResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun findByCharacter(characterId: Long, pageRequest: PageRequest): PagedResponse<RaidSignupResponse> {
        val signups = signupRepository.findByCharacterId(characterId, pageRequest.offset, pageRequest.size)
        val total = signupRepository.countByCharacterId(characterId)
        return PagedResponse.of(
            content = signups.map { RaidSignupResponse.from(it) },
            pageRequest = pageRequest,
            totalElements = total,
        )
    }

    override fun countByRaid(raidId: Long): Long =
        signupRepository.countByRaidId(raidId)
}
