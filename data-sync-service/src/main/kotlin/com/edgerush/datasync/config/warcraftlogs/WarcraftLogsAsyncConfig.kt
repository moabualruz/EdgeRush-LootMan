package com.edgerush.datasync.config.warcraftlogs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class WarcraftLogsAsyncConfig {
    @Bean("warcraftLogsExecutor")
    fun warcraftLogsExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = CORE_POOL_SIZE
            maxPoolSize = MAX_POOL_SIZE
            queueCapacity = QUEUE_CAPACITY
            setThreadNamePrefix("wcl-sync-")
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS)
            initialize()
        }
    }

    companion object {
        private const val CORE_POOL_SIZE = 2
        private const val MAX_POOL_SIZE = 5
        private const val QUEUE_CAPACITY = 100
        private const val AWAIT_TERMINATION_SECONDS = 60
    }
}
