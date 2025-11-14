package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.ApplicationDto
import com.edgerush.datasync.api.dto.ReviewApplicationRequest
import com.edgerush.datasync.application.applications.*
import com.edgerush.datasync.domain.applications.model.ApplicationId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for guild application management
 */
@RestController
@RequestMapping("/api/v1/applications")
class ApplicationController(
    private val getApplicationsUseCase: GetApplicationsUseCase,
    private val reviewApplicationUseCase: ReviewApplicationUseCase,
    private val withdrawApplicationUseCase: WithdrawApplicationUseCase
) {

    @GetMapping("/{id}")
    fun getApplication(@PathVariable id: Long): ResponseEntity<ApplicationDto> {
        val query = GetApplicationQuery(ApplicationId(id))
        
        return getApplicationsUseCase.execute(query)
            .map { application -> 
                ResponseEntity.ok(ApplicationDto.from(application))
            }
            .getOrElse { exception ->
                when (exception) {
                    is ApplicationNotFoundException -> ResponseEntity.notFound().build()
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
    }

    @GetMapping
    fun getApplications(@RequestParam(required = false) status: String?): ResponseEntity<List<ApplicationDto>> {
        val query = GetApplicationsByStatusQuery(status)
        
        return getApplicationsUseCase.execute(query)
            .map { applications -> 
                ResponseEntity.ok(applications.map { ApplicationDto.from(it) })
            }
            .getOrElse { 
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
    }

    @PostMapping("/{id}/review")
    fun reviewApplication(
        @PathVariable id: Long,
        @RequestBody request: ReviewApplicationRequest
    ): ResponseEntity<ApplicationDto> {
        val action = try {
            ReviewAction.valueOf(request.action.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        val command = ReviewApplicationCommand(ApplicationId(id), action)
        
        return reviewApplicationUseCase.execute(command)
            .map { application -> 
                ResponseEntity.ok(ApplicationDto.from(application))
            }
            .getOrElse { exception ->
                when (exception) {
                    is ApplicationNotFoundException -> ResponseEntity.notFound().build()
                    is ApplicationRequirementsNotMetException -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build()
                    is IllegalArgumentException -> ResponseEntity.badRequest().build()
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
    }

    @PostMapping("/{id}/withdraw")
    fun withdrawApplication(@PathVariable id: Long): ResponseEntity<ApplicationDto> {
        val command = WithdrawApplicationCommand(ApplicationId(id))
        
        return withdrawApplicationUseCase.execute(command)
            .map { application -> 
                ResponseEntity.ok(ApplicationDto.from(application))
            }
            .getOrElse { exception ->
                when (exception) {
                    is ApplicationNotFoundException -> ResponseEntity.notFound().build()
                    is IllegalArgumentException -> ResponseEntity.badRequest().build()
                    else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
    }
}
