package com.edgerush.lootman.api.common

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for EntityMapper interface contract.
 *
 * Tests verify the expected behavior of mapper implementations.
 */
class EntityMapperTest : UnitTest() {

    // Test entity
    data class TestEntity(
        val id: Long? = null,
        val name: String,
        val description: String?,
    )

    // Test DTOs
    data class CreateRequest(val name: String, val description: String?)
    data class UpdateRequest(val name: String, val description: String?)
    data class Response(val id: Long, val name: String, val description: String?)

    // Concrete mapper implementation for testing
    class TestEntityMapper : EntityMapper<TestEntity, CreateRequest, UpdateRequest, Response> {
        override fun toEntity(request: CreateRequest): TestEntity {
            return TestEntity(
                id = null,
                name = request.name,
                description = request.description,
            )
        }

        override fun updateEntity(entity: TestEntity, request: UpdateRequest): TestEntity {
            return entity.copy(
                name = request.name,
                description = request.description,
            )
        }

        override fun toResponse(entity: TestEntity): Response {
            return Response(
                id = entity.id ?: 0,
                name = entity.name,
                description = entity.description,
            )
        }
    }

    private val mapper = TestEntityMapper()

    @Nested
    inner class ToEntityTests {

        @Test
        fun `should convert create request to entity`() {
            // Given
            val request = CreateRequest(name = "Test Entity", description = "A test")

            // When
            val entity = mapper.toEntity(request)

            // Then
            entity.id shouldBe null
            entity.name shouldBe "Test Entity"
            entity.description shouldBe "A test"
        }

        @Test
        fun `should handle null description in create request`() {
            // Given
            val request = CreateRequest(name = "Test", description = null)

            // When
            val entity = mapper.toEntity(request)

            // Then
            entity.name shouldBe "Test"
            entity.description shouldBe null
        }
    }

    @Nested
    inner class UpdateEntityTests {

        @Test
        fun `should update entity from update request`() {
            // Given
            val entity = TestEntity(id = 1, name = "Original", description = "Old desc")
            val request = UpdateRequest(name = "Updated", description = "New desc")

            // When
            val updated = mapper.updateEntity(entity, request)

            // Then
            updated.id shouldBe 1
            updated.name shouldBe "Updated"
            updated.description shouldBe "New desc"
        }

        @Test
        fun `should preserve entity id during update`() {
            // Given
            val entity = TestEntity(id = 42, name = "Original", description = null)
            val request = UpdateRequest(name = "Changed", description = "Added")

            // When
            val updated = mapper.updateEntity(entity, request)

            // Then
            updated.id shouldBe 42
        }
    }

    @Nested
    inner class ToResponseTests {

        @Test
        fun `should convert entity to response`() {
            // Given
            val entity = TestEntity(id = 1, name = "Entity", description = "Desc")

            // When
            val response = mapper.toResponse(entity)

            // Then
            response.id shouldBe 1
            response.name shouldBe "Entity"
            response.description shouldBe "Desc"
        }

        @Test
        fun `should handle null id as zero in response`() {
            // Given
            val entity = TestEntity(id = null, name = "New", description = null)

            // When
            val response = mapper.toResponse(entity)

            // Then
            response.id shouldBe 0
        }
    }

    @Nested
    inner class ToResponseListTests {

        @Test
        fun `should convert list of entities to responses`() {
            // Given
            val entities = listOf(
                TestEntity(id = 1, name = "First", description = "A"),
                TestEntity(id = 2, name = "Second", description = "B"),
                TestEntity(id = 3, name = "Third", description = null),
            )

            // When
            val responses = mapper.toResponseList(entities)

            // Then
            responses.size shouldBe 3
            responses[0].id shouldBe 1
            responses[0].name shouldBe "First"
            responses[1].id shouldBe 2
            responses[1].name shouldBe "Second"
            responses[2].id shouldBe 3
            responses[2].description shouldBe null
        }

        @Test
        fun `should return empty list for empty input`() {
            // Given
            val entities = emptyList<TestEntity>()

            // When
            val responses = mapper.toResponseList(entities)

            // Then
            responses shouldBe emptyList()
        }
    }
}

/**
 * Unit tests for GuildScopedEntityMapper interface contract.
 */
class GuildScopedEntityMapperTest : UnitTest() {

    data class GuildEntity(
        val id: Long? = null,
        val name: String,
        val guildId: String,
    )

    data class CreateRequest(val name: String)
    data class UpdateRequest(val name: String)
    data class Response(val id: Long, val name: String, val guildId: String)

    class TestGuildMapper : GuildScopedEntityMapper<GuildEntity, CreateRequest, UpdateRequest, Response> {
        override fun toEntity(request: CreateRequest): GuildEntity {
            throw UnsupportedOperationException("Use toEntityWithGuild instead")
        }

        override fun toEntityWithGuild(request: CreateRequest, guildId: String): GuildEntity {
            return GuildEntity(
                id = null,
                name = request.name,
                guildId = guildId,
            )
        }

        override fun updateEntity(entity: GuildEntity, request: UpdateRequest): GuildEntity {
            return entity.copy(name = request.name)
        }

        override fun toResponse(entity: GuildEntity): Response {
            return Response(
                id = entity.id ?: 0,
                name = entity.name,
                guildId = entity.guildId,
            )
        }
    }

    private val mapper = TestGuildMapper()

    @Nested
    inner class ToEntityWithGuildTests {

        @Test
        fun `should create entity with guild context`() {
            // Given
            val request = CreateRequest(name = "Guild Entity")
            val guildId = "guild-123"

            // When
            val entity = mapper.toEntityWithGuild(request, guildId)

            // Then
            entity.id shouldBe null
            entity.name shouldBe "Guild Entity"
            entity.guildId shouldBe "guild-123"
        }

        @Test
        fun `should assign different guilds to different entities`() {
            // Given
            val request = CreateRequest(name = "Shared Name")

            // When
            val entity1 = mapper.toEntityWithGuild(request, "guild-1")
            val entity2 = mapper.toEntityWithGuild(request, "guild-2")

            // Then
            entity1.guildId shouldBe "guild-1"
            entity2.guildId shouldBe "guild-2"
        }
    }
}

/**
 * Unit tests for RaiderScopedEntityMapper interface contract.
 */
class RaiderScopedEntityMapperTest : UnitTest() {

    data class RaiderEntity(
        val id: Long? = null,
        val value: Int,
        val raiderId: Long,
    )

    data class CreateRequest(val value: Int)
    data class UpdateRequest(val value: Int)
    data class Response(val id: Long, val value: Int, val raiderId: Long)

    class TestRaiderMapper : RaiderScopedEntityMapper<RaiderEntity, CreateRequest, UpdateRequest, Response> {
        override fun toEntity(request: CreateRequest): RaiderEntity {
            throw UnsupportedOperationException("Use toEntityWithRaider instead")
        }

        override fun toEntityWithRaider(request: CreateRequest, raiderId: Long): RaiderEntity {
            return RaiderEntity(
                id = null,
                value = request.value,
                raiderId = raiderId,
            )
        }

        override fun updateEntity(entity: RaiderEntity, request: UpdateRequest): RaiderEntity {
            return entity.copy(value = request.value)
        }

        override fun toResponse(entity: RaiderEntity): Response {
            return Response(
                id = entity.id ?: 0,
                value = entity.value,
                raiderId = entity.raiderId,
            )
        }
    }

    private val mapper = TestRaiderMapper()

    @Nested
    inner class ToEntityWithRaiderTests {

        @Test
        fun `should create entity with raider context`() {
            // Given
            val request = CreateRequest(value = 100)
            val raiderId = 42L

            // When
            val entity = mapper.toEntityWithRaider(request, raiderId)

            // Then
            entity.id shouldBe null
            entity.value shouldBe 100
            entity.raiderId shouldBe 42L
        }

        @Test
        fun `should assign different raiders to different entities`() {
            // Given
            val request = CreateRequest(value = 50)

            // When
            val entity1 = mapper.toEntityWithRaider(request, 1L)
            val entity2 = mapper.toEntityWithRaider(request, 2L)

            // Then
            entity1.raiderId shouldBe 1L
            entity2.raiderId shouldBe 2L
        }
    }
}
