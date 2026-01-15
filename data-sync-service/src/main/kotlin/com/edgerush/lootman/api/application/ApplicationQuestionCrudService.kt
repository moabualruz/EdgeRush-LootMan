package com.edgerush.lootman.api.application

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface ApplicationQuestionCrudService : CrudService<Long, CreateApplicationQuestionRequest, UpdateApplicationQuestionRequest, ApplicationQuestionResponse> {
    fun findByApplicationId(
        applicationId: Long,
        pageRequest: PageRequest,
    ): PagedResponse<ApplicationQuestionResponse>

    fun countByApplicationId(applicationId: Long): Long
}
