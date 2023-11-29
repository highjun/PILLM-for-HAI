package kaist.iclab.mydatalogger.openai

import okhttp3.Response


sealed interface SSEEvent {
    object Open : SSEEvent
    data class Event(val response: Chat.Response) : SSEEvent
    data class Failure(val e: Throwable?, val response: Response?) : SSEEvent
    object Closed : SSEEvent
}