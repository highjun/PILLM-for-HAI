package kaist.iclab.mydatalogger.db.entities

import androidx.room.Entity

@Entity(
    tableName = "APP_INFO",
    primaryKeys = ["packageName"]
)
data class AppInfo(
    val packageName: String,
    val name: String,
    val custom_category: String,
)

