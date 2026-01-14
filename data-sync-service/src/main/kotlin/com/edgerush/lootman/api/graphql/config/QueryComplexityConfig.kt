package com.edgerush.lootman.api.graphql.config

import graphql.analysis.MaxQueryComplexityInstrumentation
import graphql.analysis.MaxQueryDepthInstrumentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for GraphQL query complexity and depth limiting.
 *
 * Prevents denial of service attacks by limiting:
 * - Query complexity: Total cost of fields in a query
 * - Query depth: Maximum nesting level of queries
 *
 * These limits protect the API from expensive or deeply nested queries
 * that could impact server performance.
 */
@Configuration
class QueryComplexityConfig {

    @Value("\${graphql.complexity.max-complexity:100}")
    private var maxComplexity: Int = 100

    @Value("\${graphql.complexity.max-depth:10}")
    private var maxDepth: Int = 10

    /**
     * Creates instrumentation to limit total query complexity.
     *
     * Each field in a query contributes to the total complexity score.
     * Queries exceeding the maximum complexity are rejected.
     *
     * @return MaxQueryComplexityInstrumentation configured with the maximum complexity
     */
    @Bean
    fun maxQueryComplexityInstrumentation(): MaxQueryComplexityInstrumentation {
        return MaxQueryComplexityInstrumentation(maxComplexity)
    }

    /**
     * Creates instrumentation to limit query nesting depth.
     *
     * Prevents deeply nested queries that could cause stack overflow
     * or excessive processing time.
     *
     * @return MaxQueryDepthInstrumentation configured with the maximum depth
     */
    @Bean
    fun maxQueryDepthInstrumentation(): MaxQueryDepthInstrumentation {
        return MaxQueryDepthInstrumentation(maxDepth)
    }
}
