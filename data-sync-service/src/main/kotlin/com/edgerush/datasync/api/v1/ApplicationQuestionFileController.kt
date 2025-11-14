package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateApplicationQuestionFileRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationQuestionFileRequest
import com.edgerush.datasync.api.dto.response.ApplicationQuestionFileResponse
import com.edgerush.datasync.service.crud.ApplicationQuestionFileCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/application-question-files")
@Tag(name = "ApplicationQuestionFile", description = "Manage applicationquestionfile entities")
class ApplicationQuestionFileController(
    service: ApplicationQuestionFileCrudService
) : BaseCrudController<ApplicationQuestionFileEntity, Long, CreateApplicationQuestionFileRequest, UpdateApplicationQuestionFileRequest, ApplicationQuestionFileResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
