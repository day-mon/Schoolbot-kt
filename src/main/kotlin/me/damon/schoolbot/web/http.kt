package me.damon.schoolbot.web

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resumeWithException


class http
{
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
}