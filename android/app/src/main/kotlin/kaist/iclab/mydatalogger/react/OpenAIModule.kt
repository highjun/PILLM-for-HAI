package kaist.iclab.mydatalogger.react

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kaist.iclab.mydatalogger.db.dao.MessageDao
import kaist.iclab.mydatalogger.db.dao.QueryDao
import kaist.iclab.mydatalogger.openai.Chat
import kaist.iclab.mydatalogger.openai.OpenAIRepository
import kaist.iclab.mydatalogger.openai.Prompts
import kaist.iclab.mydatalogger.openai.SSEEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Exception

class OpenAIModule(
    reactApplicationContext: ReactApplicationContext,
    val openAIRepository: OpenAIRepository,
    val messageDao: MessageDao,
    val queryDao: QueryDao
) : AbstractReactListener(reactApplicationContext) {
    override fun getName() = javaClass.simpleName
    private val TAG = javaClass.simpleName
    private var messageFlow: Flow<List<Chat.UIMessage>>? = null
    private var messageEmitter: Job? = null
    private val gson = Gson()

    @ReactMethod
    fun initMessageEmitter() {
        if (messageEmitter == null) {
            messageEmitter = CoroutineScope(Dispatchers.IO).launch {
                messageFlow = messageDao.queryMessages().map { messageDBEntities ->
                    messageDBEntities.map {
                        Chat.UIMessage(
                            it.role,
                            it.content,
                            it.toolCalls,
                            it.isToolCall,
                            it.toolCallId,
                            it.name,
                        )
                    }
                }
                // Give initial message
                if (messageFlow!!.first().isEmpty()) {
                    messageDao.insertMessage(
                        Chat.MessageDBEntity(
                            role = Chat.MessageRole.SYSTEM,
                            content = Prompts.systemPrompt,
                        )
                    )
                    request(Prompts.userPrompt)
                }
                // Update Message
                messageFlow!!.collect {
                    sendEvent(reactApplicationContext, name, gson.toJson(it))
                }
            }
        }
    }

    private fun annotateQuery(query: String, expectedColumns: List<String>): String {
        val removedSuffix = query.removeSuffix(";")
        var aggregateQuery = "SELECT"
        expectedColumns.forEach {
            aggregateQuery += " ${it} || ', ' ||"
        }
        aggregateQuery = aggregateQuery.removeSuffix(" || ', ' ||")
        aggregateQuery += " AS ret FROM (${removedSuffix});"
        return aggregateQuery
    }

    @ReactMethod
    fun request(request: String) {
        Log.d(TAG, "REQUEST: ${request}")
        CoroutineScope(Dispatchers.IO).launch {

            messageDao.insertMessage(
                Chat.MessageDBEntity(
                    role = Chat.MessageRole.USER,
                    content = request
                )
            )
            val messages_ = messageFlow!!.first()
            val response = Chat.MessageDBEntity(
                role = Chat.MessageRole.ASSISTANT,
                content = ""
            )
            response.idx = messageDao.insertMessage(response)
            var toolCalls: List<Chat.ToolCall>? = null
            openAIRepository.postCompletions(messages_).collect {
//                Log.d(TAG, it.toString())
                if (it is SSEEvent.Open) {

                } else if (it is SSEEvent.Event) {
                    val delta: Chat.Response.Choice.Delta? =
                        it.response.choices.firstOrNull()?.delta
//                   Check it is toolcalls or not.
                    if (delta?.toolCalls != null) {
                        if (toolCalls == null) {
                            toolCalls = delta.toolCalls
                            response.content = "Calling Function..."
                            messageDao.updateMessage(response)
//                            Log.d(TAG, "TOOLCALLS_AFTER_INIT: ${toolCalls}")
                        } else {
                            toolCalls?.let {
                                delta.toolCalls.let { deltaToolCall ->
                                    it.firstOrNull()?.function?.arguments += deltaToolCall.firstOrNull()?.function?.arguments
                                }
                            }
                        }
                    } else {
                        response.content += delta?.content ?: ""
                        messageDao.updateMessage(response)
                    }

                    if (it.response.choices.firstOrNull()?.finishReason == "tool_calls") {
                        toolCalls?.firstOrNull()?.let {
                            Log.d(TAG, it.function.arguments)
                            val data = Gson().fromJson(
                                it.function.arguments,
                                Chat.ToolCallArguments::class.java
                            )
                            response.content = Gson().toJson(it.function)
                            response.toolCalls = toolCalls
                            response.isToolCall = true
                            messageDao.updateMessage(response)

//                            Log.d(TAG, "Response = ${response}")

                            CoroutineScope(Dispatchers.IO).launch {
//                                Log.d(TAG, "TRY SQL Query: ${data.SQLquery}")
                                var messageContent: String = ""
                                if (data.SQLquery == null) {
                                    messageContent = "ERROR: SQLquery parameter is not provided"
                                    Log.e(TAG, messageContent)
                                }
                                if (data.SQLqueryExpectedColumns == null) {
                                    messageContent =
                                        "ERROR: SQLquery_expectedColumns parameter is not provided"
                                    Log.e(TAG, messageContent)
                                } else {
                                    try {
                                        val tmp = data.SQLqueryExpectedColumns.split(",")
                                    } catch (e: Exception) {
                                        messageContent =
                                            "ERROR: SQLquery_expectedColumns is not list of two words"
                                        Log.e(
                                            TAG,
                                            "${messageContent}: ${data.SQLqueryExpectedColumns}"
                                        )

                                    }
                                }
                                if (data.graphType == null) {
                                    messageContent = "ERROR: graph_type parameter is not provided"
                                    Log.e(TAG, messageContent)

                                } else {
                                    if (!listOf(
                                            "barchart",
                                            "piechart",
                                            "scatterplot",
                                            "linechart",
                                            "none"
                                        ).contains(data.graphType)
                                    ) {
                                        messageContent =
                                            """ERROR: graph_type parameter is not one of ["barchart","piechart","scatterplot","linechart"] """
                                        Log.e(TAG, "${messageContent}: ${data.graphType}")
                                    }
                                    if (data.graphType != "none") {
                                        if (data.graphVariable == null) {
                                            messageContent =
                                                "ERROR: graph_variable parameter is not provided"
                                            Log.e(TAG, messageContent)
                                        } else {
                                            if (data.graphVariable.split(",").size != 2) {
                                                messageContent =
                                                    "ERROR: graph_variable is not list of two words"
                                                Log.e(
                                                    TAG,
                                                    "${messageContent}: ${data.graphVariable}"
                                                )
                                            }
                                        }
                                    }
                                }
                                if (data.explainCriteria == null) {
                                    messageContent =
                                        "ERROR: explain_criteria parameter is not provided"
                                    Log.e(TAG, messageContent)

                                }
//                               All variable is valid
                                if (messageContent == "") {
                                    try {
                                        val valuesList: List<String> =
                                            data.SQLqueryExpectedColumns!!.split(",").toList()
//                                        Log.d(TAG, "TRY QUERY: ${data.SQLquery!!}")
//                                        Log.d(TAG, "TRY QUERY: ${data.SQLqueryExpectedColumns!!}")
                                        val annotatedQuery = annotateQuery(
                                            data.SQLquery!!,
                                            valuesList
                                        )
                                        Log.d(TAG, "TRY QUERY: ${annotatedQuery}")
                                        val result = queryDao.getQueryResults(
                                            SimpleSQLiteQuery(
                                                annotatedQuery
                                            )
                                        ).first()
//                                        Log.d(TAG, "TRY QUERY: " + result.toString())
                                        val data_ = result.map{
                                            val tmps = it.split(",")
                                            val ret = mutableMapOf<String, Any>()
                                            for((idx, value) in valuesList.withIndex()){
                                                ret[value] = tmps[idx]
                                            }
                                            return@map ret
                                        }

                                        val emitData = mapOf(
                                            "data" to gson.toJson(data_),
                                            "graph_variable" to data.graphVariable,
                                            "explain_criteria" to data.explainCriteria,
                                            "SQLquery" to data.SQLquery,
                                            "graph_type" to data.graphType,
                                            "SQLquery_expectedColumns" to data.SQLqueryExpectedColumns
                                        )
                                        messageContent = gson.toJson(emitData)
                                    } catch (e: Exception) {
                                        messageContent = "ERROR: SQLquery is invalid SQL statement"
                                        Log.e(TAG, messageContent)
                                    }
                                }
                                messageDao.insertMessage(
                                    Chat.MessageDBEntity(
                                        role = Chat.MessageRole.TOOL,
                                        content = messageContent,
                                        toolCallId = it.id,
                                        name = it.function.name
                                    )
                                )
                            }
                        }
                    }

                } else if (it is SSEEvent.Failure) {
                    val response_cotent = it.response?.message.orEmpty()
                    response.content =
                        if (response_cotent.isEmpty()) "Sorry, currently LLM is not available" else response_cotent
                    messageDao.updateMessage(response)
                    this.cancel()
                } else if (it is SSEEvent.Closed) {
                    this.cancel()
                }
            }
        }
    }

    @ReactMethod
    override fun removeListeners(count: Int) {
        super.removeListeners(count)
        if (!listenerExist()) {
            openAIRepository.cancelCompletions()
            messageEmitter?.cancel()
        }
    }

    @ReactMethod
    override fun addListener(eventName: String) {
        super.addListener(eventName)
    }
}