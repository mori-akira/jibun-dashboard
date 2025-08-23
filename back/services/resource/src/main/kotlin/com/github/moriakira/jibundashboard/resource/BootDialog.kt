package com.github.moriakira.jibundashboard.resource

import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class BootDialog {
    @Bean
    fun runner(ctx: ApplicationContext) = ApplicationRunner {
        println(">> AppContext = ${ctx.javaClass.name}")  // 期待: AnnotationConfigServletWebServerApplicationContext
    }
}
