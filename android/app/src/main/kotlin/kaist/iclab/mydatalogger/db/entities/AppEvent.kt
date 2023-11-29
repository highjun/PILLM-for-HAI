package kaist.iclab.mydatalogger.db.entities

import androidx.room.Entity

@Entity(
    tableName = "APP_USAGE_EVENT",
    primaryKeys = ["start","packageName"]
)
data class AppEvent(
    val start: Long,
    val end: Long,
    val packageName: String,
)
