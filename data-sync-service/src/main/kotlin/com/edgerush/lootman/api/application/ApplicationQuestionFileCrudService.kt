package com.edgerush.lootman.api.application

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

interface ApplicationQuestionFileCrudService : CrudService<Long, CreateApplicationQuestionFileRequest, UpdateApplicationQuestionFileRequest, ApplicationQuestionFileResponse> {
    fun findByApplicationId(applicationId: Long, pageRequest: PageRequest): PagedResponse<ApplicationQuestionFileResponse>
    fun countByApplicationId(applicationId: Long): Long
}
