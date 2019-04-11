package uz.isoft.imessage.database.contact

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Maybe
import uz.isoft.imessage.Contact

@Dao
interface ContactDao{
    @Query("SELECT * FROM contact")
    fun getAll() : LiveData<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(contacts:ArrayList<Contact>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contact: Contact)

//    @Query("UPDATE contact SET  status = :status WHERE id=:id")
//    fun updateContact()

    @Query("DELETE FROM contact")
    fun deleteAll()
}
