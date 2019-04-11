package uz.isoft.imessage.database.temp.message

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import uz.isoft.imessage.TempMessage

class TempViewModel(application: Application):AndroidViewModel(application){

    private var repository: TempRepository= TempRepository(application)
    private var allMessages: LiveData<List<TempMessage>> = repository.getAllMessage()

    fun insert(message: TempMessage) {
        repository.insert(message)
    }

    fun getAllTemp()=allMessages

    fun deleteAllMessages() {
        repository.deleteAllMessage()
    }

    fun deleteTemp(message: TempMessage){
        repository.deleteTemp(message)
    }

}