package kaist.iclab.mydatalogger

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.opencsv.CSVReader
import kaist.iclab.mydatalogger.db.dao.InsertDao
import kaist.iclab.mydatalogger.db.dao.QueryDao
import kaist.iclab.mydatalogger.db.entities.AppEvent
import kaist.iclab.mydatalogger.db.entities.AppInfo
import kaist.iclab.mydatalogger.db.entities.LocationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader

object InsertDB {


    fun insert(queryDao: QueryDao, insertDao: InsertDao, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            if (queryDao.getQueryResults(
                    SimpleSQLiteQuery("SELECT * FROM APP_USAGE_EVENT")
                ).first().isEmpty()
            ) {
                var inputStream: InputStream
                var reader: BufferedReader
                var line: String?

                inputStream = context.resources.openRawResource(R.raw.app_info)
                reader = BufferedReader(InputStreamReader(inputStream))
                val appInfos = mutableListOf<AppInfo>()
                while (reader.readLine().also { line = it } != null) {
                    val columns = line?.split(",")
                    appInfos.add(
                        AppInfo(
                            columns?.get(0).orEmpty(),
                            columns?.get(1).orEmpty(),
                            columns?.get(2).orEmpty()
                        )
                    )
                }
                reader.close()

                inputStream = context.resources.openRawResource(R.raw.app_usage_event)
                reader = BufferedReader(InputStreamReader(inputStream))
                val appEvents = mutableListOf<AppEvent>()
                while (reader.readLine().also { line = it } != null) {
                    val columns = line?.split(",")
                    appEvents.add(
                        AppEvent(
                            columns!!.get(0)!!.toLong(),
                            columns!!.get(1)!!.toLong(),
                            columns?.get(2).orEmpty()
                        )
                    )
                }
                reader.close()

                inputStream = context.resources.openRawResource(R.raw.location)
                reader = BufferedReader(InputStreamReader(inputStream))
                val locations = mutableListOf<LocationEvent>()
                while (reader.readLine().also { line = it } != null) {
                    val columns = line?.split(",")
                    locations.add(
                        LocationEvent(
                            columns!!.get(0)!!.toLong(),
                            columns!!.get(1)!!.toLong(),
                            columns!!.get(2).toInt(),
                            columns!!.get(3).toLong()
                        )
                    )
                }
                reader.close()

                CoroutineScope(Dispatchers.IO).launch {
                    insertDao.insertLocations(locations.toList())
                    insertDao.insertAppUsageEvent(appEvents.toList())
                    insertDao.insertAppInfos(appInfos.toList())
                }
            }
        }


    }
}