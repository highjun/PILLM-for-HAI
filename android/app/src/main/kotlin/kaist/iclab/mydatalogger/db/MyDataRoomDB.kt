package kaist.iclab.mydatalogger.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kaist.iclab.mydatalogger.db.dao.InsertDao
import kaist.iclab.mydatalogger.db.dao.MessageDao
import kaist.iclab.mydatalogger.db.dao.QueryDao
import kaist.iclab.mydatalogger.db.entities.AppEvent
import kaist.iclab.mydatalogger.db.entities.AppInfo
import kaist.iclab.mydatalogger.db.entities.LocationEvent
import kaist.iclab.mydatalogger.openai.Chat

@Database(
    version = 64,
    entities = [
        AppInfo::class,
        AppEvent::class,
        LocationEvent::class,
        Chat.MessageDBEntity::class
    ],
    exportSchema = false,
)
@TypeConverters(Chat.MessageConverter::class)
abstract class MyDataRoomDB : RoomDatabase() {
//    abstract fun collectorDao(): CollectorDao
    abstract fun queryDao(): QueryDao

    abstract fun messageDao():MessageDao

    abstract fun insertDao(): InsertDao
}