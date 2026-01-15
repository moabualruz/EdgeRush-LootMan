package com.edgerush.lootman.api.graphql.subscription

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for EventPublisherService.
 *
 * Tests the aggregated event publisher service following TDD principles.
 */
class EventPublisherServiceTest : UnitTest() {
    private val lootEventPublisher = LootEventPublisher()
    private val flpsEventPublisher = FlpsEventPublisher()
    private val penaltyEventPublisher = PenaltyEventPublisher()
    private val syncEventPublisher = SyncEventPublisher()

    private val eventPublisherService =
        EventPublisherService(
            loot = lootEventPublisher,
            flps = flpsEventPublisher,
            penalty = penaltyEventPublisher,
            sync = syncEventPublisher,
        )

    @Test
    fun `should provide access to loot event publisher`() {
        eventPublisherService.loot shouldNotBe null
    }

    @Test
    fun `should provide access to flps event publisher`() {
        eventPublisherService.flps shouldNotBe null
    }

    @Test
    fun `should provide access to penalty event publisher`() {
        eventPublisherService.penalty shouldNotBe null
    }

    @Test
    fun `should provide access to sync event publisher`() {
        eventPublisherService.sync shouldNotBe null
    }

    @Test
    fun `should inject same instances as provided`() {
        eventPublisherService.loot shouldBe lootEventPublisher
        eventPublisherService.flps shouldBe flpsEventPublisher
        eventPublisherService.penalty shouldBe penaltyEventPublisher
        eventPublisherService.sync shouldBe syncEventPublisher
    }
}
