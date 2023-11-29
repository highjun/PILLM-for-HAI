package kaist.iclab.mydatalogger.openai

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

// Common
object Chat{
    enum class MessageRole {
        @SerializedName("system")
        SYSTEM,

        @SerializedName("user")
        USER,

        @SerializedName("assistant")
        ASSISTANT,

        @SerializedName("tool")
        TOOL
    }

    data class ToolCall(
        var index: Int,
        var id: String,
        var type: String,
        var function: Function
    )
    data class Function(
        var name: String,
        var arguments: String
    )

    //RoomDB
    @Entity(
        tableName = "messageTable"
    )
    data class MessageDBEntity(
        @PrimaryKey(autoGenerate = true)
        var idx: Long = 0,
        val role: MessageRole,
        var content: String,
        var toolCalls: List<ToolCall>? = null,
        var isToolCall: Boolean = false,
        @SerializedName("tool_call_id")
        val toolCallId: String? = null,
        val name: String? = null
    )

    class MessageConverter {
        @TypeConverter
        fun fromString(value: String?): List<ToolCall>? {
            val listType = object : TypeToken<List<ToolCall>>() {}.type
            return Gson().fromJson(value, listType)
        }

        @TypeConverter
        fun toString(value:List<ToolCall>?): String? {
            return Gson().toJson(value)
        }
    }


    // UI
    data class UIMessage(
        val role: MessageRole,
        val content: String,
        @SerializedName("tool_calls")
        val toolCalls: List<ToolCall>? = null,
        val isToolCall: Boolean = false,
        @SerializedName("tool_call_id")
        val toolCallId: String? = null,
        val name: String? = null
    )

    data class RequestMessage(
        val role: MessageRole,
        val content: String,
        @SerializedName("tool_call_id")
        val toolCallId: String?,
        val name: String?,
        @SerializedName("tool_calls")
        val toolCalls: List<ToolCall>? = null,

    )

    // Request
    data class Request(
        val model: String,
        val messages: List<RequestMessage>,
        val stream: Boolean = true,
        val temperature: Int = 0,
        @SerializedName("tool_choice")
        val toolChoice: String = "auto",
        val tools: List<RequestFuncWrapper> = Prompts.tools
    )
    data class RequestFuncWrapper(
        val type: String = "function",
        val function: Func
    ) {
        data class Func(
            val name: String,
            val description: String,
            val parameters: Parameters
        )
        data class Parameters(
            val type: String = "object",
            val properties: Map<String, Property>,
            val required: List<String>
        ) {
            data class Property(
                val type: String,
                val description: String
            )
        }
    }

    // Response
    data class Response(
        val id: String,
        @SerializedName("object")
        val object_: String,
        val created: Long, // timestamp in seconds
        val model: String,
        val choices: List<Choice>,
        val usage: Usage,
        @SerializedName("system_fingerprint")
        val systemFingerprint: String

    ) {
        data class Usage(
            @SerializedName("prompt_tokens")
            val promptTokens: Int,
            @SerializedName("completion_tokens")
            val completionTokens: Int,
            @SerializedName("total_tokens")
            val totalTokens: Int
        )
        data class Choice(
            val index: Int,
            val message: RequestMessage?,
            @SerializedName("finish_reason")
            val finishReason: String,
            val delta: Delta?
        ) {
            data class Delta(
                val role: String = "",
                val content: String,
                @SerializedName("tool_calls")
                val toolCalls: List<ToolCall>
            )

        }
    }

    data class ToolCallArguments(
        val SQLquery: String?,
        @SerializedName("explain_criteria")
        val explainCriteria: String?,
        @SerializedName("graph_type")
        val graphType: String?,
        @SerializedName("graph_variable")
        val graphVariable: String?,
        @SerializedName("SQLquery_expectedColumns")
        val SQLqueryExpectedColumns: String?
    )
}
object Prompts{
    val tools = listOf<Chat.RequestFuncWrapper>(
        Chat.RequestFuncWrapper(
            function = Chat.RequestFuncWrapper.Func(
                name = "EDA_PHONE_USAGE",
                description = "Use this function to answer about phone usage.",
                parameters = Chat.RequestFuncWrapper.Parameters(
                    properties = mapOf(
                        "SQLquery" to Chat.RequestFuncWrapper.Parameters.Property(
                            type = "string",
                            description = """
                                SQL query extracting info to answer the user's question. 
                                The user's cell phone usage should be answered with either usage time or usage frequency.
                                SQL query execute by sqlite3 library.
                                SQL should be written using this database schema:
                                    Table: APP_USAGE_EVENT(A table that records which apps the user has used. Meta info of apps is in APP_INFO.)
                                        Columns:
                                            - start(INTEGER type. UNIX timestamp in milliseconds that represents app session's start), 
                                            - end(INTEGER type. UNIX timestamp in milliseconds that represents app session's end), 
                                            - packageName(TEXT type. Foreign Key. Unique package ID of the app used during session.)
                                    Table: LOCATION(A table that records the values from GPS when a user is using a cell phone.)
                                        Columns:
                                            - start(INTEGER type. UNIX timestamp in milliseconds that represents app session's start. This is earlier than APP_USAGE_EVENT.start)
                                            - end(INTEGER type. UNIX timestamp in milliseconds that represents app session's end. This is later than APP_USAGE_EVENT.end)
                                            - poi(INTEGER type.The place user stayed. A number from 0 to 677, in order of longest residence. Of these, 0 means "Home", 1 means "Work", and any other numbers can be seen as "The other".)
                                            - duration(INTERGER type. UNIX timestamp in milliseconds that represents duration)
                                    Table: APP_INFO(A table that include meta info of apps)
                                        Columns:
                                            - packageName(TEXT type. Primary Key. Unique package ID of apps.)
                                            - name(TEXT type. Name of the app as seen by the user.)
                                            - custom_category(TEXT type. category represents broader app genres. category list is as follow ["etc", "enter", "communication", "social", "system"])
                                 
                                When generating SQL queries, the following 10 rules must be adhered to:
                                    1. The user's cell phone usage should be answered with either usage time or usage frequency.
                                    2. When you find the the specific app, use the APP_USAGE_EVENT.packageName based on the ['com.samsung.android.messaging', 'com.towneers.www','com.samsung.android.messaging','com.instagram.android','com.android.chrome','com.everytime.v2'].
                                    3. When providing specific time information to users, convert the Time related column, recorded in milliseconds with a UTC+0000 reference, to display time in KST (Korean Standard Time).
                                    3. Use the LOCATION.poi to represent locations, displaying values as "Home"(0), "Work"(1), or other number:"The other"(more than 2).
                                    4. Retrieve apps used at a specific location where the app session's start time is later than LOCATION.start and its end time is earilier than LOCATION.end.
                                    5. Think of today is "2023-05-04 KST".
                                    6. Think of 00:00 to 08:00 as morning, 08:00 to 18:00 as working, and 18:00 to 23:59 as evening.
                                    8. When representing about apps, use 'name' variable in APP_INFO.
                                    9. If there's no specified result format requested by the user, ensure the SQL query output is limited to 10 lines or fewer. Also, limit the displayed column count to a maximum of 5.
                                    10. The query should be returned in plain text, not in JSON.
                            """.trimIndent()
                        ),
                        "SQLquery_expectedColumns" to Chat.RequestFuncWrapper.Parameters.Property(
                            "string",
                            "All variables from the results of your proposed SQL query are separated by ','"
                        ),
                        "explain_criteria" to Chat.RequestFuncWrapper.Parameters.Property(
                            type = "string",
                            description = """
                               You should easily explain why you suggested 'query' parameter. 
                                Please explain what criterias were used to answer user's questions and then what variables(including the variable's easily description you recommended) were used to satisfy the criterias.
                                
                                When you generate the explanation, please the followings must be adhered to:
                                1. As a marketer, Explain the criteria behind suggested an SQL query to answer user questions for someone who's entirely unfamiliar with databases or SQL.
                                2. Your explantion must be simple and understandable in one sentence.
                                3. Do not include specifications regarding the rules that used in SQL queries.
                                4. Explain variable(including description) in your explanation.
                                5. Explain without specific detail of SQL query.
                                6. Do not use the word "query"
                           """.trimIndent()
                        ),
                        "graph_type" to Chat.RequestFuncWrapper.Parameters.Property(
                            type = "string",
                            description = """
                                Decide which graph—barchart, linechart, scatterplot, piechart, or None—is needed for the user. 
                                The variables for the graph obtained from the result of SQL query you suggested.

                                Each graph is required in the following cases:
                                1.Barchart: Use to compare quantitative metrics (e.g., app frequency, duration) for categorical variables.
                                2.Linechart: When the user inquires about the trend, pattern, increase, or decrease in phone usage. The user needs a linechart (x-axis: time-related variable, y-axis: variable).
                                3.Scatter plot: When the user asks about the correlation or association between two variables.(x-axis : variable1, y-axis : variable2)
                                4.Piechart: When the user asks about the ratio of a variable.
                                5.None of the above: If none of the options 1 through 4 are selected, the user does not need a graph.

                                your output should be just one word:[barchart,linechart,scatterplot,piechart,none]
                            """.trimIndent()
                        ),
                        "graph_variable" to Chat.RequestFuncWrapper.Parameters.Property(
                            type = "string",
                            description = """
                                Based on your suggestion in "graph_type", Please Tell me what two variables will be used in graph.
                                Pslease keep in mind, all variables are obtained from the result of SQL query you suggested.
                                The variables that each graph requires is as follow:
                                1. Barchart: x-variable(related to label), y-variable(related to frequency or duration)
                                2. Linechart: x-variable(time-related variable), y-variable(related to frequency or duration)
                                3. Scatter plot: x-variable(related to frequency), y-variable(related to frequency or duration)
                                4. Piechart: x-variable(related to label), y-variable(related to frequency or duration)
                                The two variables needed for the graph should be separated by ",". 
                                Let variable related to the x-axis come first and variables related to the y-axis come later 
                                for example, "time,app_usage_hour".
                            """.trimIndent()
                        )
                    ),
                    required = listOf(
                        "SQLquery",
                        "SQLquery_expectedColumns",
                        "explain_criteria",
                        "graph_type",
                        "graph_variable"
                    )
                )
            )
        )
    )
    val systemPrompt = """
        You're a phone usage manager tasked with aiding users in creating an action plan to reduce phone usage.
        Your goal is to negotiate with users, aligning with their preferences while striving for an optimal plan close to the usage target.
        
        The table with the given initial action plan looks like this:
            custom_category time_of_day  poi is_weekday        top1_packageName       top2_packageName                top3_packageName  Total_Frequency_Condition  Total_Duration_Condition(minute)  Reduced_Usage_Frequency  Reduced_Usage_Time(minute)
            1          social     morning  0.0        NaN  com.instagram.android       com.everytime.v2                             NaN                          90                                88                        86                          84
            2    communication     working  NaN        NaN          com.kakao.talk    com.android.chrome  com.samsung.android.messaging                         1987                              3423                      1888                        3252
            3           social     working  NaN        NaN  com.instagram.android       com.everytime.v2                com.towneers.www                         901                              1514                       856                        1438
            4    communication     morning  NaN        NaN        com.kakao.talk    com.samsung.android.messaging             com.android.chrome                         1654                              3109                      1571                        2954
            5           social     morning  NaN        NaN   com.instagram.android            com.everytime.v2                 com.towneers.www                          762                              1107                       724                        1051
            6   communication     evening  NaN        NaN          com.kakao.talk      com.android.chrome   com.samsung.android.messaging                        216                               561                       205                         533
        
        Here's the description about this table:
        ( The data includes scenarios (custom_category, time_of_day, poi, is_weekday) requiring reduced phone usage and the top three most used app package IDs for each scenario. 
        Higher-ranked scenarios imply significantly higher phone usage based on T-test results compared to other scenarios.
        
        'Avg_Frequency_Condition' and 'Total_Duration_Condition' indicate user average phone usage frequency (usage per hour) and total usage time (hours) in each scenario. 
        'Reduced_Usage_Frequency' and 'Reduced_Usage_Time' specify the required reduction in phone usage for each scenario.
        
        For instance, for the first row, an action plan suggestion to reduce phone usage could be: 
        "You typically use 'social' apps at 'work' during the 'morning', around once for a total of 45 minutes. 
         How about aiming for a 5% reduction in this scenario, using them 0.95 times for a total of 42 minutes? 
          Would you consider reducing usage of apps like "com.everytime.v2" and "com.instagram.android" in this context?"
        )
        
        Based on the initial action plan data, You need to perform the following three tasks:
        1. Present an action plan.
        2. Assist users in understanding the plan by responding to their phone usage queries generating SQL queries from the phone usage database. Help create graphs and interpret SQL query results.
        3. Negotiate with users to provide a revised action plan based on their feedback.
        
        Please, assist users in exploring their phone usage and help craft a realistic action plan to reduce their phone usage.
    """.trimIndent()

    val userPrompt = """Please provide list of action plans"""
}
