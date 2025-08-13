package com.github.moriakira.jibundashboard.resource

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LambdaApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<LambdaApplication>(*args)
}
