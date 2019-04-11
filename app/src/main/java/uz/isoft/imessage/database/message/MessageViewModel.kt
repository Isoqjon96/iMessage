package uz.isoft.imessage.database.message

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import uz.isoft.imessage.Message

class MessageViewModel(application: Application,val to: String,val from: String):AndroidViewModel(application){

    private var repository: MessageRepository = MessageRepository(application)
    private var allMessages: LiveData<List<Message>> = repository.getAllMessage()
    private var userMessageM:LiveData<List<Message>> = repository.getUserMessage(to,from)
    private var noSendMessage :LiveData<List<Message>> = repository.getNoSendMessage()
    private var noReadMessage:LiveData<List<Message>> = repository.getNoReads()

    constructor(application: Application) : this(application,"","")

    fun insert(message: Message) {
        repository.insert(message)
    }

    fun deleteAllMessages() {
        repository.deleteAllMessage()
    }

    fun updateMessage(message: Message){
        repository.update(message)
    }

    fun update(uid:String){
        repository.updateFlag(uid)
    }

    fun getAllMessage() = allMessages

    fun getNoSendMessage() = noSendMessage
    fun getNoReads() = noReadMessage

    fun getChatMessage()=userMessageM


}