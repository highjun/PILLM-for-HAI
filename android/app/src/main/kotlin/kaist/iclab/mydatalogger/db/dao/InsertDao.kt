package kaist.iclab.mydatalogger.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kaist.iclab.mydatalogger.db.entities.AppEvent
import kaist.iclab.mydatalogger.db.entities.AppInfo
import kaist.iclab.mydatalogger.db.entities.LocationEvent
import kotlinx.coroutines.flow.Flow


@Dao
interface InsertDao {
    @Insert
    suspend fun insertLocations(locations: List<LocationEvent>)

    @Insert
    suspend fun insertAppUsageEvent(appEvents: List<AppEvent>)

    @Insert
    suspend fun insertAppInfos(appInfos: List<AppInfo>)

}