package com.edgerush.lootman.api.application

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface ApplicationAltCrudService : CrudService<Long, CreateApplicationAltRequest, UpdateApplicationAltRequest, ApplicationAltResponse> {
    fun findByApplicationId(applicationId: Long, pageRequest: PageRequest): PagedResponse<ApplicationAltResponse>
    fun countByApplicationId(applicationId: Long): Long
}
