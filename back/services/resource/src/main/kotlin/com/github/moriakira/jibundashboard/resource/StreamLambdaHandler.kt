package com.github.moriakira.jibundashboard.resource

import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream

class StreamLambdaHandler : RequestStreamHandler {

    companion object {
        init {
            System.setProperty("spring.main.web-application-type", "servlet")
            LambdaContainerHandler.getContainerConfig().initializationTimeout = 60_000
        }

        private val handler = SpringBootProxyHandlerBuilder<HttpApiV2ProxyRequest>()
            .defaultHttpApiV2Proxy()
            .springBootApplication(LambdaApplication::class.java)
            .buildAndInitialize()
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        handler.proxyStream(input, output, context)
    }
}
