package kaist.iclab.mydatalogger.react


import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.google.gson.Gson
import kaist.iclab.mydatalogger.db.dao.QueryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class DataQueryModule(
    reactAppContext: ReactApplicationContext,
    private val queryDao: QueryDao
): AbstractReactListener(reactAppContext) {
    override fun getName(): String = javaClass.simpleName
    private val TAG = javaClass.simpleName
    private val gson = Gson()
    private var emitter: Job? = null

    @ReactMethod
    fun getRawQueryOnce(query: String,expectedColumns: ReadableArray, callback: Callback){
        Log.d(TAG, "getResultOnce: ${query}")
        val annotatedQuery = annotateQuery(query, expectedColumns)
        Log.d(TAG, "annotatedQuery: ${annotatedQuery}")
        getResult(annotatedQuery).onStart {
            callback.invoke()
        }
    }

    @ReactMethod
    fun addRawQueryListener(query: String,expectedColumns: ReadableArray, eventName: String){
        val annotatedQuery = annotateQuery(query, expectedColumns)
        emitter = CoroutineScope(Dispatchers.IO).launch {
            getResult(annotatedQuery).collect{
                sendEvent(reactApplicationContext, eventName, gson.toJson(it))
            }
        }
    }

    fun getResult(query: String): Flow<List<String>>{
        return queryDao.getQueryResults(SimpleSQLiteQuery(query))
    }

    private fun annotateQuery(query: String, expectedColumns: ReadableArray):String{
        var aggregateQuery = "SELECT"
        expectedColumns.toArrayList().forEach {
            aggregateQuery += " ${it} || ', ' ||"
        }
        aggregateQuery = aggregateQuery.removeSuffix(" || ', ' ||")
        aggregateQuery += " AS ret FROM (${query.removeSuffix(";")});"
        return aggregateQuery
    }
}