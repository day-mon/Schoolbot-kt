package me.damon.schoolbot.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resumeWithException

inline fun <T : AutoCloseable, R> T.useCatching(fn: () -> R) = runCatching {
    fn()
}.also { close() }

suspend inline fun <T> Call.await(scope: CoroutineScope, crossinline callback: suspend (Response) -> T) =
    suspendCancellableCoroutine<T> { sink ->
        sink.invokeOnCancellation { cancel() }
        enqueue(object : Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                sink.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response)
            {
                scope.launch {
                    response.useCatching {
                        callback(response)
                    }.also(sink::resumeWith)
                }
            }
        })
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

fun Response.bodyAsString() = this.body()?.string()