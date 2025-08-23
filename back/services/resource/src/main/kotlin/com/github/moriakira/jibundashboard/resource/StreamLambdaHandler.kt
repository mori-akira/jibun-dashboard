package com.github.moriakira.jibundashboard.resource

import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream

class StreamLambdaHandler : RequestStreamHandler {

    companion object {
        private val handler = SpringBootProxyHandlerBuilder<AwsProxyRequest>()
            .defaultHttpApiV2Proxy()
            .springBootApplication(LambdaApplication::class.java)
            .buildAndInitialize()
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        handler.proxyStream(input, output, context)
    }
}
