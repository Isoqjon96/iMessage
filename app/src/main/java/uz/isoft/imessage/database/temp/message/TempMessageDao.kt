package uz.isoft.imessage.database.temp.message

import androidx.lifecycle.LiveData
import androidx.room.*
import uz.isoft.imessage.TempMessage

@Dao
interface TempMessageDao{

    @Query("SELECT * FROM temp_message")
    fun getAll(): LiveData<List<TempMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: TempMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(messages:List<TempMessage>)

    @Query("SELECT * FROM temp_message WHERE (`to` = :to AND `from`= :from) OR (`to` = :from AND `from`= :to)")
    fun getUserMessage(to:String,from:String): LiveData<List<TempMessage>>

    @Delete
    fun delete(message: TempMessage)

    @Query("DELETE FROM temp_message")
    fun deleteAll()
}