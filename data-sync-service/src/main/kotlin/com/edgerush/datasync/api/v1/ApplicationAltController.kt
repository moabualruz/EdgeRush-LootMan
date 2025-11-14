package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateApplicationAltRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationAltRequest
import com.edgerush.datasync.api.dto.response.ApplicationAltResponse
import com.edgerush.datasync.service.crud.ApplicationAltCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.ApplicationAltEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/application-alts")
@Tag(name = "ApplicationAlt", description = "Manage applicationalt entities")
class ApplicationAltController(
    service: ApplicationAltCrudService
) : BaseCrudController<ApplicationAltEntity, Long, CreateApplicationAltRequest, UpdateApplicationAltRequest, ApplicationAltResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
