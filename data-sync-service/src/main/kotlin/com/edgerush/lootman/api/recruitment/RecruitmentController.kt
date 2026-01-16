package com.edgerush.lootman.api.recruitment

import com.edgerush.lootman.application.recruitment.CreateApplicationCommand
import com.edgerush.lootman.application.recruitment.RecruitmentService
import com.edgerush.lootman.domain.recruitment.RecruitmentApplication
import com.edgerush.lootman.domain.recruitment.RecruitmentCharacter
import com.edgerush.lootman.domain.recruitment.RecruitmentComment
import com.edgerush.lootman.domain.recruitment.RecruitmentStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/recruitment")
class RecruitmentController(
    private val recruitmentService: RecruitmentService
) {

    @PostMapping("/applications")
    fun createApplication(
        @RequestParam guildId: String, // In real app, derived from context/token
        @RequestBody command: CreateApplicationCommand
    ): ResponseEntity<RecruitmentApplication> {
        val application = recruitmentService.createApplication(guildId, command)
        return ResponseEntity.ok(application)
    }

    @GetMapping("/applications")
    fun getApplications(
        @RequestParam guildId: String,
        @RequestParam(required = false) status: RecruitmentStatus?
    ): ResponseEntity<List<RecruitmentApplication>> {
        val applications = recruitmentService.getApplications(guildId, status)
        return ResponseEntity.ok(applications)
    }

    @GetMapping("/candidates/search")
    fun searchCandidate(
        @RequestParam name: String,
        @RequestParam realm: String,
        @RequestParam(defaultValue = "eu") region: String
    ): ResponseEntity<RecruitmentCharacter> {
        val candidate = recruitmentService.searchCandidate(name, realm, region)
        return ResponseEntity.ok(candidate)
    }

    @GetMapping("/applications/{id}")
    fun getApplication(@PathVariable id: String): ResponseEntity<RecruitmentApplication> {
        val application = recruitmentService.getApplication(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(application)
    }

    @PutMapping("/applications/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @RequestParam status: RecruitmentStatus,
        @RequestParam reviewer: String // In real app, from token
    ): ResponseEntity<RecruitmentApplication> {
        val application = recruitmentService.updateStatus(id, status, reviewer)
        return ResponseEntity.ok(application)
    }

    @PostMapping("/applications/{id}/comments")
    fun addComment(
        @PathVariable id: String,
        @RequestParam authorId: Long, // In real app, from token
        @RequestBody text: String
    ): ResponseEntity<RecruitmentComment> {
        val comment = recruitmentService.addComment(id, authorId, text)
        return ResponseEntity.ok(comment)
    }
}
