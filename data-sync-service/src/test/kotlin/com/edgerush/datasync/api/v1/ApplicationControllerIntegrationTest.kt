package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.ApplicationDto
import com.edgerush.datasync.api.dto.ReviewApplicationRequest
import com.edgerush.datasync.application.applications.*
import com.edgerush.datasync.domain.applications.model.*
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.OffsetDateTime

@WebMvcTest(ApplicationController::class)
@Import(ApplicationControllerIntegrationTest.TestConfig::class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var getApplicationsUseCase: GetApplicationsUseCase

    @Autowired
    private lateinit var reviewApplicationUseCase: ReviewApplicationUseCase

    @Autowired
    private lateinit var withdrawApplicationUseCase: WithdrawApplicationUseCase

    @TestConfiguration
    class TestConfig {
        @Bean
        fun getApplicationsUseCase(): GetApplicationsUseCase = mockk()

        @Bean
        fun reviewApplicationUseCase(): ReviewApplicationUseCase = mockk()

        @Bean
        fun withdrawApplicationUseCase(): WithdrawApplicationUseCase = mockk()
    }

    @Test
    fun `should get application by id`() {
        // Given
        val application = createTestApplication()
        every { getApplicationsUseCase.execute(any<GetApplicationQuery>()) } returns Result.success(application)

        // When/Then
        mockMvc.perform(get("/api/v1/applications/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    fun `should return 404 when application not found`() {
        // Given
        every { getApplicationsUseCase.execute(any<GetApplicationQuery>()) } returns 
            Result.failure(ApplicationNotFoundException(ApplicationId(999L)))

        // When/Then
        mockMvc.perform(get("/api/v1/applications/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should get applications by status`() {
        // Given
        val applications = listOf(createTestApplication(), createTestApplication(ApplicationId(2L)))
        every { getApplicationsUseCase.execute(any<GetApplicationsByStatusQuery>()) } returns Result.success(applications)

        // When/Then
        mockMvc.perform(get("/api/v1/applications?status=PENDING"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `should approve application`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.APPROVED)
        every { reviewApplicationUseCase.execute(any()) } returns Result.success(application)

        // When/Then
        mockMvc.perform(
            post("/api/v1/applications/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action": "APPROVE"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("APPROVED"))

        verify(exactly = 1) { reviewApplicationUseCase.execute(any()) }
    }

    @Test
    fun `should return 422 when application requirements not met`() {
        // Given
        every { reviewApplicationUseCase.execute(any()) } returns 
            Result.failure(ApplicationRequirementsNotMetException(ApplicationId(1L)))

        // When/Then
        mockMvc.perform(
            post("/api/v1/applications/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"action": "APPROVE"}""")
        )
            .andExpect(status().isUnprocessableEntity)
    }

    @Test
    fun `should withdraw application`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.WITHDRAWN)
        every { withdrawApplicationUseCase.execute(any()) } returns Result.success(application)

        // When/Then
        mockMvc.perform(post("/api/v1/applications/1/withdraw"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("WITHDRAWN"))

        verify(exactly = 1) { withdrawApplicationUseCase.execute(any()) }
    }

    private fun createTestApplication(
        id: ApplicationId = ApplicationId(1L),
        status: ApplicationStatus = ApplicationStatus.PENDING
    ): Application {
        val appliedAt = OffsetDateTime.now()
        val applicantInfo = ApplicantInfo(25, "USA", "player#1234", "discord123", "DPS")
        val mainCharacter = CharacterInfo("TestChar", "TestRealm", "US", "Warrior", "Tank", "Human", "Alliance", 80)

        return Application(
            id = id,
            appliedAt = appliedAt,
            status = status,
            applicantInfo = applicantInfo,
            mainCharacter = mainCharacter
        )
    }
}
