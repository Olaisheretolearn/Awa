package com.summerlockin.Awa.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {
    @Bean("passwordResetMailExecutor")
    fun passwordResetMailExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 1
        maxPoolSize = 4
        queueCapacity = 100
        setThreadNamePrefix("password-reset-mail-")
        initialize()
    }
}
