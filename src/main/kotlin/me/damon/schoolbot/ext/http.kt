package me.damon.schoolbot.ext

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val logger: Logger = LoggerFactory.getLogger("Requester")!!

suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                logger.debug("Request to ${call.request().url()} succeeded with status code ${response.code()}")
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                logger.error("Error has occurred while trying to make a request o ${call.request().url()}", e)
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
        }
    }
}


class HttpException(
    route: String, status: Int, meaning: String
) : Exception("$route > $status: $meaning")

class NotAuthorized(
    response: Response
) : Exception("Authorization failed. Code: ${response.code()} Body: ${response.body()?.string()}")

fun Response.asException() = HttpException(request().url().toString(), code(), message())

inline fun post(url: String, form: FormBody.Builder.() -> Unit): Request
{
    val body = FormBody.Builder()
    body.form()
    return Request.Builder().url(url).method("POST", body.build()).build()
}

fun get(url: String): Request
{
    return Request.Builder().url(url).method("GET", null).build()
}

fun Response.bodyAsString() = this.body()?.string().also {
    this.body()?.close()
}