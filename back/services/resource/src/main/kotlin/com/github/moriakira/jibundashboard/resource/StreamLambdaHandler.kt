package com.github.moriakira.jibundashboard.resource

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream

class StreamLambdaHandler : RequestStreamHandler {

    companion object {
        init {
            System.setProperty("spring.main.web-application-type", "servlet")
            System.setProperty(
                "spring.main.application-context-class",
                "org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext"
            )
            LambdaContainerHandler.getContainerConfig().initializationTimeout = 60_000
        }

        private val handler =
            SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(
                LambdaApplication::class.java,
            )
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        handler.proxyStream(input, output, context)
    }
}
