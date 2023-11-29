package kaist.iclab.mydatalogger.db.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kaist.iclab.mydatalogger.db.entities.AppEvent
import kaist.iclab.mydatalogger.db.entities.AppInfo
import kaist.iclab.mydatalogger.db.entities.LocationEvent
import kotlinx.coroutines.flow.Flow


@Dao
interface QueryDao {
    @RawQuery(observedEntities = [AppInfo::class, LocationEvent::class, AppEvent::class])
    fun getQueryResults(query: SupportSQLiteQuery): Flow<List<String>>
}