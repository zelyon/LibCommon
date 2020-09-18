package bzh.zelyon.lib.util.api

import retrofit2.Call
import retrofit2.Response

class CallError<T>(private val call: Call<T>, private val response: Response<T>? = null, private val throwable: Throwable? = null) {

    val httpCode get() = response?.code() ?: 0
    val method get() = call.request().method
    val url get() = call.request().url.toUrl().toString()
    val errorBody get() = response?.errorBody().toString()
    val message get() = response?.message() ?: throwable?.localizedMessage ?: throwable?.message.orEmpty()
}