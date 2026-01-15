package com.edgerush.lootman.api.application

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface ApplicationCrudService : CrudService<Long, CreateApplicationRequest, UpdateApplicationRequest, ApplicationResponse> {
    fun findByStatus(
        status: String,
        pageRequest: PageRequest,
    ): PagedResponse<ApplicationResponse>

    fun countByStatus(status: String): Long
}
