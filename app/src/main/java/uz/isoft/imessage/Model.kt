package uz.isoft.imessage

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

data class User(
    val id: Long? = null,
    val name: String? = null,
    val surname: String? = null,
    val phone: String? = null,
    val uid: String? = null,
    var image: String? = null
) : Serializable

data class BaseResponse<T>(
    val code: Int,
    val result: T?,
    val error: String
) : Serializable

@Entity(tableName = "contact")
data class Contact(
    val name: String? = null,
    val phone: String? = null,
    val flag: Int? = null,
    val uid: String? = null,
    val date: Long? = null,
    val image: String? = null,
    var count: Int? = null
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}

data class ContactWrapper(
    var contacts: ArrayList<Contact>? = null
)

@Entity(tableName = "message")
data class Message(
    var from: String? = null,
    var to: String? = null,
    val text: String? = null,
    val date: Long? = null,
    var flag: Int = 0,
    var status: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

@Entity(tableName = "temp_message")
data class TempMessage(
    val from: String? = null,
    val to: String? = null,
    val text: String? = null,
    val date: Long? = null,
    var flag: Int = 0,
    var status: Int = 0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}

data class ResponseMessage<T>(val code: Int,
                    val result: T?,
                    val error: String?):Serializable
