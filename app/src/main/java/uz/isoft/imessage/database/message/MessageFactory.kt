package uz.isoft.imessage.database.message

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MessageFactory(private val mApplication: Application, private val to: String,private val from: String) :
    ViewModelProvider.Factory {


    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MessageViewModel(mApplication, to, from) as T
    }
}