package com.edgerush.lootman.api.graphql.error

import com.edgerush.lootman.domain.shared.GuildNotFoundException
import com.edgerush.lootman.domain.shared.ItemNotFoundException
import com.edgerush.lootman.domain.shared.LootBanActiveException
import com.edgerush.lootman.domain.shared.RaiderNotFoundException
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

/**
 * GraphQL exception handler for converting domain exceptions to GraphQL errors.
 *
 * Transforms domain-specific exceptions into properly formatted GraphQL errors
 * with appropriate error codes in the extensions field.
 */
@Component
class GraphQLExceptionHandler : DataFetcherExceptionHandler {

    override fun handleException(
        handlerParameters: DataFetcherExceptionHandlerParameters
    ): CompletableFuture<DataFetcherExceptionHandlerResult> {
        val exception = handlerParameters.exception
        val sourceLocation = handlerParameters.sourceLocation
        val path = handlerParameters.path

        val error = when (exception) {
            // NOT_FOUND errors
            is RaiderNotFoundException -> createError(
                message = exception.message ?: "Raider not found",
                code = ErrorCode.NOT_FOUND,
                sourceLocation = sourceLocation,
                path = path
            )
            is GuildNotFoundException -> createError(
                message = exception.message ?: "Guild not found",
                code = ErrorCode.NOT_FOUND,
                sourceLocation = sourceLocation,
                path = path
            )
            is ItemNotFoundException -> createError(
                message = exception.message ?: "Item not found",
                code = ErrorCode.NOT_FOUND,
                sourceLocation = sourceLocation,
                path = path
            )
            is NoSuchElementException -> createError(
                message = exception.message ?: "Resource not found",
                code = ErrorCode.NOT_FOUND,
                sourceLocation = sourceLocation,
                path = path
            )

            // BAD_REQUEST errors
            is IllegalArgumentException -> createError(
                message = exception.message ?: "Invalid request parameters",
                code = ErrorCode.BAD_REQUEST,
                sourceLocation = sourceLocation,
                path = path
            )

            // CONFLICT errors
            is LootBanActiveException -> createError(
                message = exception.message ?: "Operation conflicts with current state",
                code = ErrorCode.CONFLICT,
                sourceLocation = sourceLocation,
                path = path
            )
            is IllegalStateException -> createError(
                message = exception.message ?: "Operation conflicts with current state",
                code = ErrorCode.CONFLICT,
                sourceLocation = sourceLocation,
                path = path
            )

            // Unknown errors - don't expose internal details
            else -> createError(
                message = "An unexpected error occurred",
                code = ErrorCode.INTERNAL_ERROR,
                sourceLocation = sourceLocation,
                path = path
            )
        }

        val result = DataFetcherExceptionHandlerResult.newResult()
            .error(error)
            .build()

        return CompletableFuture.completedFuture(result)
    }

    private fun createError(
        message: String,
        code: ErrorCode,
        sourceLocation: graphql.language.SourceLocation?,
        path: graphql.execution.ResultPath?
    ): GraphQLError {
        val builder = GraphqlErrorBuilder.newError()
            .message(message)
            .extensions(mapOf("code" to code.name))

        sourceLocation?.let { builder.location(it) }
        path?.let { builder.path(it.toList()) }

        return builder.build()
    }
}

/**
 * Error codes for GraphQL responses.
 *
 * These codes are included in the error extensions to allow clients
 * to programmatically handle different error types.
 */
enum class ErrorCode {
    NOT_FOUND,
    BAD_REQUEST,
    CONFLICT,
    UNAUTHORIZED,
    FORBIDDEN,
    INTERNAL_ERROR
}
