package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateTeamMetadataRequest
import com.edgerush.datasync.api.dto.request.UpdateTeamMetadataRequest
import com.edgerush.datasync.api.dto.response.TeamMetadataResponse
import com.edgerush.datasync.service.crud.TeamMetadataCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.TeamMetadataEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/team-metadatas")
@Tag(name = "TeamMetadata", description = "Manage teammetadata entities")
class TeamMetadataController(
    service: TeamMetadataCrudService
) : BaseCrudController<TeamMetadataEntity, Long, CreateTeamMetadataRequest, UpdateTeamMetadataRequest, TeamMetadataResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
