package de.oschwald.mofilegpttranslator.services

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Retryable
import org.springframework.retry.support.RetrySynchronizationManager
import org.springframework.stereotype.Service

/**
 * As Spring-Retry does not work when invoked within the same class (it uses AOP, so needs to work on a Bean-Proxy),
 * we add the @Retryable methods here.
 */
@Service
class HttpRequestService {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    val httpClient = OkHttpClient()
        .newBuilder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    /**
     * GPT fails sometimes. Use Spring-Retry to retry the request on failures.
     */
    @Retryable
    fun performRequest(request: Request): Response {
        if (RetrySynchronizationManager.getContext() != null && RetrySynchronizationManager.getContext().retryCount > 0) {
            log.warn("Retrying request for the {}. time", RetrySynchronizationManager.getContext().retryCount)
        }
        val response = httpClient.newCall(request).execute()
        log.info("Remaining GPT-API code words {}", response.headers.get("X-Ratelimit-Remaining"))
        if (!response.isSuccessful) {
            throw RuntimeException("Unexpected code ${response.code} message: ${response.message} headers: ${response.headers}")
        }
        return response
    }
}