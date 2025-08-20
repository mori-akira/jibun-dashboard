package com.github.moriakira.jibundashboard.resource

import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.InputStream
import java.io.OutputStream

class StreamLambdaHandler : RequestStreamHandler {

    companion object {
        val handler: SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> = try {
            SpringBootLambdaContainerHandler.getAwsProxyHandler(LambdaApplication::class.java)
        } catch (e: ContainerInitializationException) {
            @Suppress("TooGenericExceptionThrown")
            throw RuntimeException("Could not initialize Spring Boot application", e)
        }
    }

    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        handler.proxyStream(input, output, context)
    }
}
