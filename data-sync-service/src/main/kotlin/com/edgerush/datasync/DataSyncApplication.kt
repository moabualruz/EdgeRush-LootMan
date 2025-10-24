package com.edgerush.datasync

import com.edgerush.datasync.config.SyncProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
@EnableConfigurationProperties(SyncProperties::class)
class DataSyncApplication

fun main(args: Array<String>) {
    runApplication<DataSyncApplication>(*args)
}
