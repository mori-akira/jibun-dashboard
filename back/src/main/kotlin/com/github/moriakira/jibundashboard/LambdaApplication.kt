package com.github.moriakira.jibundashboard

import com.github.moriakira.jibundashboard.prompts.SalaryPayslipOcrPromptsProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(SalaryPayslipOcrPromptsProperties::class)
class LambdaApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<LambdaApplication>(*args)
}
