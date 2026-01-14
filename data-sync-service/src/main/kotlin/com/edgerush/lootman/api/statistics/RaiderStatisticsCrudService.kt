package com.edgerush.lootman.api.statistics

import com.edgerush.lootman.api.common.CrudService

interface RaiderStatisticsCrudService : CrudService<Long, CreateRaiderStatisticsRequest, UpdateRaiderStatisticsRequest, RaiderStatisticsResponse> {
    fun findByRaiderId(raiderId: Long): RaiderStatisticsResponse
    fun existsByRaiderId(raiderId: Long): Boolean
}
