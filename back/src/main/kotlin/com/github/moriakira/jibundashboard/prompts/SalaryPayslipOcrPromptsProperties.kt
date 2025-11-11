package com.github.moriakira.jibundashboard.prompts

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.openai.prompts.salary-payslip-ocr")
data class SalaryPayslipOcrPromptsProperties(
    val system: String,
    val user: String,
    val sample: String,
)
