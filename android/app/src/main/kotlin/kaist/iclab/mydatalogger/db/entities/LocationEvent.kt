package kaist.iclab.mydatalogger.db.entities

import androidx.room.Entity

@Entity(
    tableName = "LOCATION",
    primaryKeys = ["start"]
)
data class LocationEvent(
    val start: Long,
    val end: Long,
    val poi: Int,
    val duration: Long
)
