package uz.isoft.imessage.database.contact

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.isoft.imessage.Contact

@Database(entities = [Contact::class], version = 1)
abstract class ContactDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
