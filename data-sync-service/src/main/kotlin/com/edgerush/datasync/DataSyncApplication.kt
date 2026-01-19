package com.edgerush.datasync

import com.edgerush.datasync.config.SyncProperties
import com.edgerush.lootman.api.common.PaginationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = ["com.edgerush.datasync", "com.edgerush.lootman"])
@EnableConfigurationProperties(SyncProperties::class, PaginationProperties::class)
@ComponentScan(basePackages = ["com.edgerush.datasync", "com.edgerush.lootman"])
@EnableJdbcRepositories(basePackages = ["com.edgerush.lootman.infrastructure.springdata"])
class DataSyncApplication

fun main(args: Array<String>) {
    runApplication<DataSyncApplication>(*args)
}
