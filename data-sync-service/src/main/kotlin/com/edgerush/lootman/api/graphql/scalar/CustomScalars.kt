package com.edgerush.lootman.api.graphql.scalar

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.language.IntValue
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLType
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
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
 * Custom GraphQL scalar for java.time.LocalDate.
 *
 * Serializes to/from ISO-8601 date strings (e.g., "2026-01-14").
 */
val LocalDateScalar: GraphQLScalarType =
    GraphQLScalarType.newScalar()
        .name("LocalDate")
        .description("ISO-8601 local date (e.g., 2026-01-14)")
        .coercing(LocalDateScalarCoercing())
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
 * Custom GraphQL scalar for kotlin.Long (64-bit integer).
 *
 * GraphQL's Int type is 32-bit, so we need a custom scalar for 64-bit Long values.
 * Serializes to/from numeric strings for JavaScript compatibility.
 */
val LongScalar: GraphQLScalarType =
    GraphQLScalarType.newScalar()
        .name("Long")
        .description("64-bit integer (serialized as string for JavaScript compatibility)")
        .coercing(LongScalarCoercing())
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
 * Coercing implementation for LocalDate scalar.
 */
class LocalDateScalarCoercing : Coercing<LocalDate, String> {
    override fun serialize(dataFetcherResult: Any): String {
        return when (dataFetcherResult) {
            is LocalDate -> dataFetcherResult.toString()
            else -> throw CoercingSerializeException(
                "Expected a LocalDate but got: ${dataFetcherResult::class.simpleName}",
            )
        }
    }

    override fun parseValue(input: Any): LocalDate {
        return when (input) {
            is String ->
                try {
                    LocalDate.parse(input)
                } catch (e: DateTimeParseException) {
                    throw CoercingParseValueException("Invalid LocalDate format: $input", e)
                }
            else -> throw CoercingParseValueException(
                "Expected a String but got: ${input::class.simpleName}",
            )
        }
    }

    override fun parseLiteral(input: Any): LocalDate {
        return when (input) {
            is StringValue ->
                try {
                    LocalDate.parse(input.value)
                } catch (e: DateTimeParseException) {
                    throw CoercingParseLiteralException("Invalid LocalDate format: ${input.value}", e)
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
 * Coercing implementation for Long scalar.
 */
class LongScalarCoercing : Coercing<Long, String> {
    override fun serialize(dataFetcherResult: Any): String {
        return when (dataFetcherResult) {
            is Long -> dataFetcherResult.toString()
            is Int -> dataFetcherResult.toLong().toString()
            else -> throw CoercingSerializeException(
                "Expected a Long but got: ${dataFetcherResult::class.simpleName}",
            )
        }
    }

    override fun parseValue(input: Any): Long {
        return when (input) {
            is Long -> input
            is Int -> input.toLong()
            is String ->
                try {
                    input.toLong()
                } catch (e: NumberFormatException) {
                    throw CoercingParseValueException("Invalid Long format: $input", e)
                }
            else -> throw CoercingParseValueException(
                "Expected a String or Number but got: ${input::class.simpleName}",
            )
        }
    }

    override fun parseLiteral(input: Any): Long {
        return when (input) {
            is IntValue -> input.value.toLong()
            is StringValue ->
                try {
                    input.value.toLong()
                } catch (e: NumberFormatException) {
                    throw CoercingParseLiteralException("Invalid Long format: ${input.value}", e)
                }
            else -> throw CoercingParseLiteralException(
                "Expected an IntValue or StringValue but got: ${input::class.simpleName}",
            )
        }
    }
}

/**
 * Schema generator hooks to register custom scalars.
 *
 * This component tells graphql-kotlin how to handle custom types
 * like Instant, LocalDate, LocalDateTime, and Long in the generated schema.
 *
 * The bean is named "schemaGeneratorHooks" as required by graphql-kotlin-spring-server.
 */
@Component("schemaGeneratorHooks")
class CustomScalarHooks : SchemaGeneratorHooks {
    override fun willGenerateGraphQLType(type: KType): GraphQLType? {
        return when (type.classifier as? KClass<*>) {
            Instant::class -> InstantScalar
            LocalDate::class -> LocalDateScalar
            LocalDateTime::class -> LocalDateTimeScalar
            Long::class -> LongScalar
            else -> null
        }
    }
}
