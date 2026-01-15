package com.edgerush.lootman.api.graphql.scalar

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Custom GraphQL scalar for java.time.Instant.
 *
 * Serializes to/from ISO-8601 format strings (e.g., "2026-01-14T10:30:00Z").
 */
val InstantScalar: GraphQLScalarType =
    GraphQLScalarType.newScalar()
        .name("Instant")
        .description("ISO-8601 instant timestamp (e.g., 2026-01-14T10:30:00Z)")
        .coercing(InstantScalarCoercing())
        .build()

/**
 * Custom GraphQL scalar for java.time.LocalDateTime.
 *
 * Serializes to/from ISO-8601 format strings without timezone (e.g., "2026-01-14T10:30:00").
 */
val LocalDateTimeScalar: GraphQLScalarType =
    GraphQLScalarType.newScalar()
        .name("LocalDateTime")
        .description("ISO-8601 local date-time without timezone (e.g., 2026-01-14T10:30:00)")
        .coercing(LocalDateTimeScalarCoercing())
        .build()

/**
 * Coercing implementation for Instant scalar.
 */
class InstantScalarCoercing : Coercing<Instant, String> {
    override fun serialize(dataFetcherResult: Any): String {
        return when (dataFetcherResult) {
            is Instant -> dataFetcherResult.toString()
            else -> throw CoercingSerializeException(
                "Expected an Instant but got: ${dataFetcherResult::class.simpleName}",
            )
        }
    }

    override fun parseValue(input: Any): Instant {
        return when (input) {
            is String ->
                try {
                    Instant.parse(input)
                } catch (e: DateTimeParseException) {
                    throw CoercingParseValueException("Invalid Instant format: $input", e)
                }
            else -> throw CoercingParseValueException(
                "Expected a String but got: ${input::class.simpleName}",
            )
        }
    }

    override fun parseLiteral(input: Any): Instant {
        return when (input) {
            is StringValue ->
                try {
                    Instant.parse(input.value)
                } catch (e: DateTimeParseException) {
                    throw CoercingParseLiteralException("Invalid Instant format: ${input.value}", e)
                }
            else -> throw CoercingParseLiteralException(
                "Expected a StringValue but got: ${input::class.simpleName}",
            )
        }
    }
}

/**
 * Coercing implementation for LocalDateTime scalar.
 */
class LocalDateTimeScalarCoercing : Coercing<LocalDateTime, String> {
    override fun serialize(dataFetcherResult: Any): String {
        return when (dataFetcherResult) {
            is LocalDateTime -> dataFetcherResult.toString()
            else -> throw CoercingSerializeException(
                "Expected a LocalDateTime but got: ${dataFetcherResult::class.simpleName}",
            )
        }
    }

    override fun parseValue(input: Any): LocalDateTime {
        return when (input) {
            is String ->
                try {
                    LocalDateTime.parse(input)
                } catch (e: DateTimeParseException) {
                    throw CoercingParseValueException("Invalid LocalDateTime format: $input", e)
                }
            else -> throw CoercingParseValueException(
                "Expected a String but got: ${input::class.simpleName}",
            )
        }
    }

    override fun parseLiteral(input: Any): LocalDateTime {
        return when (input) {
            is StringValue ->
                try {
                    LocalDateTime.parse(input.value)
                } catch (e: DateTimeParseException) {
                    throw CoercingParseLiteralException("Invalid LocalDateTime format: ${input.value}", e)
                }
            else -> throw CoercingParseLiteralException(
                "Expected a StringValue but got: ${input::class.simpleName}",
            )
        }
    }
}

/**
 * Schema generator hooks to register custom scalars.
 *
 * This component tells graphql-kotlin how to handle custom types
 * like Instant and LocalDateTime in the generated schema.
 */
@Component
class CustomScalarHooks : SchemaGeneratorHooks {
    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        return when (type.classifier as? KClass<*>) {
            Instant::class -> InstantScalar
            LocalDateTime::class -> LocalDateTimeScalar
            else -> null
        }
    }
}
