package uz.isoft.imessage.database.temp.message

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.isoft.imessage.TempMessage

@Database(entities = [TempMessage::class],version = 1)
abstract class TempDatabase:RoomDatabase(){
    abstract fun tempMessageDao():TempMessageDao
}