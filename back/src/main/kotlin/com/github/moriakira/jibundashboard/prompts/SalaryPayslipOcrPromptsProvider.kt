package com.github.moriakira.jibundashboard.prompts

import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class SalaryPayslipOcrPromptsProvider(
    private val props: SalaryPayslipOcrPromptsProperties,
    private val resourceLoader: ResourceLoader,
) {

    fun systemPrompt(): String = readText(props.system)

    fun userPrompt(): String {
        val template = readText(props.user)
        val sample = sampleJson()
        return template.replace("{{ONE_SHOT_JSON}}", sample)
    }

    fun sampleJson(): String = readText(props.sample)

    private fun readText(location: String): String =
        resourceLoader.getResource(location).inputStream.use { it.readAllBytes().toString(StandardCharsets.UTF_8) }
}
