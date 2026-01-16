package com.edgerush.lootman.api.auth

import com.edgerush.lootman.domain.auth.model.UserCharacter
import com.edgerush.lootman.domain.auth.repository.UserCharacterRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for user-centric data operations.
 */
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "User data and preferences")
class UserController(
    private val authenticationService: AuthenticationService,
    private val userCharacterRepository: UserCharacterRepository
) {

    @GetMapping("/characters")
    @Operation(summary = "Get user characters", description = "Returns list of synced WoW characters")
    fun getCharacters(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String
    ): List<UserCharacter> {
        val token = authorization.substring(7)
        val userId = authenticationService.validateToken(token) 
            ?: throw IllegalArgumentException("Invalid token")
            
        return userCharacterRepository.findAllByUserId(userId)
    }
}
