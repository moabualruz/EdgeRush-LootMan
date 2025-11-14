package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.CreateCharacterHistoryRequest
import com.edgerush.datasync.api.dto.request.UpdateCharacterHistoryRequest
import com.edgerush.datasync.api.dto.response.CharacterHistoryResponse
import com.edgerush.datasync.service.crud.CharacterHistoryCrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import com.edgerush.datasync.entity.CharacterHistoryEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/character-historys")
@Tag(name = "CharacterHistory", description = "Manage characterhistory entities")
class CharacterHistoryController(
    service: CharacterHistoryCrudService
) : BaseCrudController<CharacterHistoryEntity, Long, CreateCharacterHistoryRequest, UpdateCharacterHistoryRequest, CharacterHistoryResponse>(service) {
    // Custom endpoints can be added here manually as needed
}
