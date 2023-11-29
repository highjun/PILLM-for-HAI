package kaist.iclab.mydatalogger

import androidx.room.Room
import kaist.iclab.mydatalogger.db.MyDataRoomDB
import kaist.iclab.mydatalogger.openai.OpenAIRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val koinModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MyDataRoomDB::class.java,
            "MyDataRoomDB"
        )
            .fallbackToDestructiveMigration() // TODO:For Dev Phase!
            .build()
    }

    single {
        get<MyDataRoomDB>().queryDao()
    }

    single {
        get<MyDataRoomDB>().messageDao()
    }

    single {
        get<MyDataRoomDB>().insertDao()
    }
    single {
        OpenAIRepository()
    }
}