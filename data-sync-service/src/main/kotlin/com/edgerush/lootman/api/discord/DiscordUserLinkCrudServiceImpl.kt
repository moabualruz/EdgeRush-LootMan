package com.edgerush.lootman.api.discord

import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.discord.model.DiscordUserLink
import com.edgerush.lootman.domain.discord.model.DiscordUserLinkId
import com.edgerush.lootman.domain.discord.model.DiscordUserId
import com.edgerush.lootman.domain.discord.repository.DiscordUserLinkRepository
import com.edgerush.lootman.domain.shared.DiscordUserLinkAlreadyExistsException
import com.edgerush.lootman.domain.shared.DiscordUserLinkNotFoundException
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of DiscordUserLinkCrudService.
 *
 * Provides CRUD operations for Discord user links with business logic
 * for handling primary character designation and duplicate prevention.
 */
@Service
@Transactional
class DiscordUserLinkCrudServiceImpl(
    private val repository: DiscordUserLinkRepository
) : DiscordUserLinkCrudService {

    @Transactional(readOnly = true)
    override fun findAll(pageRequest: PageRequest): PagedResponse<DiscordUserLinkResponse> {
        val links = repository.findAll(pageRequest.offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = links.map { DiscordUserLinkResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total
        )
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): DiscordUserLinkResponse {
        val link = repository.findById(DiscordUserLinkId(id))
            ?: throw DiscordUserLinkNotFoundException(id)
        return DiscordUserLinkResponse.from(link)
    }

    override fun create(request: CreateDiscordUserLinkRequest): DiscordUserLinkResponse {
        val discordUserId = DiscordUserId(request.discordUserId)
        val raiderId = RaiderId(request.raiderId)

        // Check for duplicate link
        if (repository.existsByDiscordUserIdAndRaiderId(discordUserId, raiderId)) {
            throw DiscordUserLinkAlreadyExistsException(request.discordUserId, request.raiderId)
        }

        // If this is set as primary, clear existing primary
        if (request.isPrimary) {
            repository.clearPrimaryForDiscordUser(discordUserId)
        }

        // If no links exist for this user, make it primary automatically
        val isPrimary = request.isPrimary || repository.countByDiscordUserId(discordUserId) == 0L

        val link = DiscordUserLink.create(
            discordUserId = discordUserId,
            raiderId = raiderId,
            isPrimary = isPrimary,
            linkedBy = request.linkedBy
        )

        val savedLink = repository.save(link)
        return DiscordUserLinkResponse.from(savedLink)
    }

    override fun update(id: Long, request: UpdateDiscordUserLinkRequest): DiscordUserLinkResponse {
        val existingLink = repository.findById(DiscordUserLinkId(id))
            ?: throw DiscordUserLinkNotFoundException(id)

        var updatedLink = existingLink

        // Handle isPrimary change
        if (request.isPrimary != null && request.isPrimary != existingLink.isPrimary) {
            if (request.isPrimary) {
                // Clear other primary links for this user
                repository.clearPrimaryForDiscordUser(existingLink.discordUserId)
                updatedLink = updatedLink.markAsPrimary()
            } else {
                updatedLink = updatedLink.markAsNonPrimary()
            }
        }

        // Handle linkedBy change
        if (request.linkedBy != null) {
            updatedLink = updatedLink.copy(linkedBy = request.linkedBy)
        }

        val savedLink = repository.save(updatedLink)
        return DiscordUserLinkResponse.from(savedLink)
    }

    override fun delete(id: Long) {
        val link = repository.findById(DiscordUserLinkId(id))
            ?: throw DiscordUserLinkNotFoundException(id)

        repository.deleteById(link.id!!)

        // If we deleted the primary link, promote another link to primary
        if (link.isPrimary) {
            val remainingLinks = repository.findByDiscordUserId(link.discordUserId)
            if (remainingLinks.isNotEmpty()) {
                val newPrimary = remainingLinks.first().markAsPrimary()
                repository.save(newPrimary)
            }
        }
    }

    @Transactional(readOnly = true)
    override fun existsById(id: Long): Boolean {
        return repository.findById(DiscordUserLinkId(id)) != null
    }

    @Transactional(readOnly = true)
    override fun findByDiscordUserId(discordUserId: String): List<DiscordUserLinkResponse> {
        val links = repository.findByDiscordUserId(DiscordUserId(discordUserId))
        return links.map { DiscordUserLinkResponse.from(it) }
    }

    @Transactional(readOnly = true)
    override fun findPrimaryByDiscordUserId(discordUserId: String): DiscordUserLinkResponse {
        val link = repository.findPrimaryByDiscordUserId(DiscordUserId(discordUserId))
            ?: throw NoSuchElementException("No primary link found for Discord user: $discordUserId")
        return DiscordUserLinkResponse.from(link)
    }

    @Transactional(readOnly = true)
    override fun findByRaiderId(raiderId: Long): List<DiscordUserLinkResponse> {
        val links = repository.findByRaiderId(RaiderId(raiderId))
        return links.map { DiscordUserLinkResponse.from(it) }
    }

    @Transactional(readOnly = true)
    override fun countByDiscordUserId(discordUserId: String): Long {
        return repository.countByDiscordUserId(DiscordUserId(discordUserId))
    }

    override fun setPrimary(linkId: Long): DiscordUserLinkResponse {
        val link = repository.findById(DiscordUserLinkId(linkId))
            ?: throw DiscordUserLinkNotFoundException(linkId)

        // Clear other primary links for this user
        repository.clearPrimaryForDiscordUser(link.discordUserId)

        // Set this link as primary
        val updatedLink = repository.save(link.markAsPrimary())
        return DiscordUserLinkResponse.from(updatedLink)
    }

    override fun deleteByDiscordUserId(discordUserId: String): Int {
        return repository.deleteByDiscordUserId(DiscordUserId(discordUserId))
    }
}
