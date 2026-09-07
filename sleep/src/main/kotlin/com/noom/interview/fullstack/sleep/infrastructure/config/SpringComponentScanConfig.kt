package com.noom.interview.fullstack.sleep.infrastructure.config

import com.noom.interview.fullstack.sleep.common.annotation.UseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import java.time.Clock

@Configuration
@ComponentScan(
    basePackages = ["com.sleeplogger"],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [UseCase::class]
        )
    ]
)
class SpringComponentScanConfig {

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}