package kaist.iclab.mydatalogger.openai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class OpenAIRepository {
    private val TAG = javaClass.simpleName
    private val TOKEN = ""
    private val MODEL = "gpt-4-1106-preview"
    private val BASE_URL = "https://api.openai.com/v1/chat/completions"
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var eventSource: EventSource? = null
    private val client = providesOkHttpClient()

    fun cancelCompletions(){
        eventSource?.cancel()
    }

    suspend fun postCompletions(messages: List<Chat.UIMessage>): Flow<SSEEvent> {
        val requestBody = gson.toJson(Chat.Request(MODEL, messages.map{ Chat.RequestMessage(it.role, it.content,it.toolCallId, it.name, it.toolCalls)}))
        val request = Request.Builder()
            .url(BASE_URL)
            .header("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${TOKEN}")
            .post(requestBody.toRequestBody("application/json; charset=UTF-8".toMediaTypeOrNull()))
            .build()
//        Log.d(TAG, "REQUESTBODY: BEFOREGSON ${gson.toJson(messages)}")
//        Log.d(TAG, "REQUESTBODY: " + requestBody)
        return callbackFlow {
            val listener = object: EventSourceListener(){
                override fun onClosed(eventSource: EventSource) {
                    super.onClosed(eventSource)
                    trySend(SSEEvent.Closed)
                    close()
                    Log.d(TAG,"CLOSED")
                }

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    super.onEvent(eventSource, id, type, data)
//                    Log.d(TAG,"EVENT: $data")
                    if(data!= "[DONE]"){
                        val response = gson.fromJson(data, Chat.Response::class.java)
                        trySend(SSEEvent.Event(response))
                    }else{
                        trySend(SSEEvent.Closed)
                        close()
                    }

                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?
                ) {
                    super.onFailure(eventSource, t, response)
                    Log.d(TAG,"FAILURE: $response, $t")
                    trySend(SSEEvent.Failure(t, response))
                    close()
                }

                override fun onOpen(eventSource: EventSource, response: Response) {
                    super.onOpen(eventSource, response)
                    Log.d(TAG,"OPEN")
                    trySend(SSEEvent.Open)
                }

            }
            eventSource = EventSources.createFactory(client)
                .newEventSource(request, listener)

            awaitClose { eventSource?.cancel() }
        }.cancellable()
    }
    private fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.MINUTES)
            .connectTimeout(10, TimeUnit.MINUTES)
            .build()
    }
}