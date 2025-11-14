package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateApplicationQuestionRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationQuestionRequest
import com.edgerush.datasync.api.dto.response.ApplicationQuestionResponse
import com.edgerush.datasync.entity.ApplicationQuestionEntity
import com.edgerush.datasync.service.crud.ApplicationQuestionCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/application-questions")
@Tag(name = "ApplicationQuestion", description = "Manage applicationquestion entities")
class ApplicationQuestionController(
    service: ApplicationQuestionCrudService,
) : BaseCrudController<ApplicationQuestionEntity, Long, CreateApplicationQuestionRequest, UpdateApplicationQuestionRequest, ApplicationQuestionResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
