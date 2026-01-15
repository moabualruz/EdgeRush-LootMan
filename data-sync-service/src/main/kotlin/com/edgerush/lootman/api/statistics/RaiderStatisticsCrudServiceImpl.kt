package com.edgerush.lootman.api.statistics

import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.statistics.repository.RaiderStatisticsRepository
import org.springframework.stereotype.Service

@Service
class RaiderStatisticsCrudServiceImpl(private val repository: RaiderStatisticsRepository) : RaiderStatisticsCrudService {
    override fun findAll(pageRequest: PageRequest): PagedResponse<RaiderStatisticsResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        return PagedResponse(
            repository.findAll(offset, pageRequest.size).map { RaiderStatisticsResponse.from(it) },
            pageRequest.page,
            pageRequest.size,
            repository.count(),
        )
    }

    override fun findById(id: Long): RaiderStatisticsResponse =
        repository.findById(id)?.let { RaiderStatisticsResponse.from(it) }
            ?: throw NoSuchElementException("RaiderStatistics not found with id: $id")

    override fun existsById(id: Long): Boolean = repository.existsById(id)

    override fun create(request: CreateRaiderStatisticsRequest): RaiderStatisticsResponse {
        val entity =
            RaiderStatisticsEntity(
                null, request.raiderId, request.mythicPlusScore, request.weeklyHighestMplus, request.seasonHighestMplus,
                request.worldQuestsTotal, request.worldQuestsThisWeek, request.collectiblesMounts, request.collectiblesToys,
                request.collectiblesUniquePets, request.collectiblesLevel25Pets, request.honorLevel,
            )
        return RaiderStatisticsResponse.from(repository.save(entity))
    }

    override fun update(
        id: Long,
        request: UpdateRaiderStatisticsRequest,
    ): RaiderStatisticsResponse {
        val existing = repository.findById(id) ?: throw NoSuchElementException("RaiderStatistics not found with id: $id")
        val updated =
            existing.copy(
                mythicPlusScore = request.mythicPlusScore ?: existing.mythicPlusScore,
                weeklyHighestMplus = request.weeklyHighestMplus ?: existing.weeklyHighestMplus,
                seasonHighestMplus = request.seasonHighestMplus ?: existing.seasonHighestMplus,
                worldQuestsTotal = request.worldQuestsTotal ?: existing.worldQuestsTotal,
                worldQuestsThisWeek = request.worldQuestsThisWeek ?: existing.worldQuestsThisWeek,
                collectiblesMounts = request.collectiblesMounts ?: existing.collectiblesMounts,
                collectiblesToys = request.collectiblesToys ?: existing.collectiblesToys,
                collectiblesUniquePets = request.collectiblesUniquePets ?: existing.collectiblesUniquePets,
                collectiblesLevel25Pets = request.collectiblesLevel25Pets ?: existing.collectiblesLevel25Pets,
                honorLevel = request.honorLevel ?: existing.honorLevel,
            )
        return RaiderStatisticsResponse.from(repository.save(updated))
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) throw NoSuchElementException("RaiderStatistics not found with id: $id")
        repository.delete(id)
    }

    override fun findByRaiderId(raiderId: Long): RaiderStatisticsResponse =
        repository.findByRaiderId(raiderId)?.let { RaiderStatisticsResponse.from(it) }
            ?: throw NoSuchElementException("RaiderStatistics not found for raider: $raiderId")

    override fun existsByRaiderId(raiderId: Long): Boolean = repository.existsByRaiderId(raiderId)
}
