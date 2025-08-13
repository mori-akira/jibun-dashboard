package com.github.moriakira.jibundashboard.resource

import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream

class StreamLambdaHandler : RequestStreamHandler {
    companion object {
        private val handler: SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> =
            SpringBootLambdaContainerHandler.getAwsProxyHandler(LambdaApplication::class.java)
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        handler.proxyStream(input, output, context)
    }
}
