package com.edgerush.datasync.service.warcraftlogs

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class WarcraftLogsMetrics(
    private val meterRegistry: MeterRegistry
) {
    private val syncSuccessCounter: Counter = meterRegistry.counter("warcraft_logs.sync.success")
    private val syncFailureCounter: Counter = meterRegistry.counter("warcraft_logs.sync.failure")
    private val apiLatencyTimer: Timer = meterRegistry.timer("warcraft_logs.api.latency")
    private val cacheHitCounter: Counter = meterRegistry.counter("warcraft_logs.cache.hit")
    private val cacheMissCounter: Counter = meterRegistry.counter("warcraft_logs.cache.miss")
    private val reportsProcessedCounter: Counter = meterRegistry.counter("warcraft_logs.reports.processed")
    private val fightsProcessedCounter: Counter = meterRegistry.counter("warcraft_logs.fights.processed")
    private val performanceRecordsCounter: Counter = meterRegistry.counter("warcraft_logs.performance.records")
    
    fun recordSyncSuccess() {
        syncSuccessCounter.increment()
    }
    
    fun recordSyncFailure() {
        syncFailureCounter.increment()
    }
    
    fun recordApiCall(duration: Duration) {
        apiLatencyTimer.record(duration)
    }
    
    fun recordCacheHit() {
        cacheHitCounter.increment()
    }
    
    fun recordCacheMiss() {
        cacheMissCounter.increment()
    }
    
    fun recordReportsProcessed(count: Int) {
        reportsProcessedCounter.increment(count.toDouble())
    }
    
    fun recordFightsProcessed(count: Int) {
        fightsProcessedCounter.increment(count.toDouble())
    }
    
    fun recordPerformanceRecords(count: Int) {
        performanceRecordsCounter.increment(count.toDouble())
    }
}
