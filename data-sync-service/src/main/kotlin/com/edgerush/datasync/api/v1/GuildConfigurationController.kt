package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateGuildConfigurationRequest
import com.edgerush.datasync.api.dto.request.UpdateGuildConfigurationRequest
import com.edgerush.datasync.api.dto.response.GuildConfigurationResponse
import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.datasync.service.crud.GuildConfigurationCrudService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/guild-configurations")
@Tag(name = "GuildConfiguration", description = "Manage guildconfiguration entities")
class GuildConfigurationController(
    service: GuildConfigurationCrudService,
) : BaseCrudController<GuildConfigurationEntity, Long, CreateGuildConfigurationRequest, UpdateGuildConfigurationRequest, GuildConfigurationResponse>(
        service,
    ) {
    // Custom endpoints can be added here manually as needed
}
