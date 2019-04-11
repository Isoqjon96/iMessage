package uz.isoft.imessage.database.message

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.room.*
import uz.isoft.imessage.Message

@Database(entities = [Message::class],version = 1)
abstract class MessageDatabase:RoomDatabase(){
    abstract fun messageDao():MessageDao
}

class PopulateDbAsyncTaskMessage(db: MessageDatabase?) : AsyncTask<Unit, Unit, Unit>() {
    private val noteDao = db?.messageDao()
    override fun doInBackground(vararg p0: Unit?) {

    }
}

@Dao
interface MessageDao{

    @Query("SELECT * FROM message")
    fun getAll(): LiveData<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(messages:List<Message>)

    @Query("SELECT * FROM message WHERE status=0 ")
    fun getNoSendMessage():LiveData<List<Message>>

    @Query("SELECT * FROM message WHERE flag=0")
    fun getNoRead():LiveData<List<Message>>

    @Query("UPDATE message SET  status = :status WHERE id=:id")
    fun update(status: Int, id:Int)

    @Query("UPDATE message SET flag=1 WHERE `to`=:uid")
    fun updateFlag(uid:String)

    @Query("SELECT * FROM message WHERE (`to` = :to AND `from`= :from) OR (`to` = :from AND `from`= :to)")
    fun getUserMessage(to:String,from:String):LiveData<List<Message>>

    @Delete
    fun delete(message: Message)

    @Query("DELETE FROM message")
    fun deleteAll()
}
