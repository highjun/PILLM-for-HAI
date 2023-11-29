package kaist.iclab.mydatalogger.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kaist.iclab.mydatalogger.openai.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.ABORT, entity = Chat.MessageDBEntity::class)
    suspend fun insertMessage(message: Chat.MessageDBEntity): Long

    @Query("SELECT * FROM messageTable")
    fun queryMessages(): Flow<List<Chat.MessageDBEntity>>

    @Update
    suspend fun updateMessage(message: Chat.MessageDBEntity)
}