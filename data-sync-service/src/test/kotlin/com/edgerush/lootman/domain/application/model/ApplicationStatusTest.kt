package com.edgerush.lootman.domain.application.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApplicationStatus enum.
 */
class ApplicationStatusTest : UnitTest() {
    @Test
    fun `should have PENDING status`() {
        ApplicationStatus.PENDING.name shouldBe "PENDING"
    }

    @Test
    fun `should have UNDER_REVIEW status`() {
        ApplicationStatus.UNDER_REVIEW.name shouldBe "UNDER_REVIEW"
    }

    @Test
    fun `should have APPROVED status`() {
        ApplicationStatus.APPROVED.name shouldBe "APPROVED"
    }

    @Test
    fun `should have REJECTED status`() {
        ApplicationStatus.REJECTED.name shouldBe "REJECTED"
    }

    @Test
    fun `should have WITHDRAWN status`() {
        ApplicationStatus.WITHDRAWN.name shouldBe "WITHDRAWN"
    }

    @Test
    fun `should have exactly 5 statuses`() {
        ApplicationStatus.entries.size shouldBe 5
    }

    @Test
    fun `should convert from string`() {
        ApplicationStatus.valueOf("PENDING") shouldBe ApplicationStatus.PENDING
        ApplicationStatus.valueOf("APPROVED") shouldBe ApplicationStatus.APPROVED
    }
}
