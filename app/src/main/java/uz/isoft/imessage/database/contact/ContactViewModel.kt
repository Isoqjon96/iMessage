package uz.isoft.imessage.database.contact

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import uz.isoft.imessage.Contact

class ContactViewModel(application: Application):AndroidViewModel(application){

    private var repository: ContactRepository = ContactRepository (application)
    private var allContacts: LiveData<List<Contact>> = repository.getAll()

    fun insert(contact: Contact) {
        repository.insert(contact)
    }

//    fun deleteAllMessages() {
//        repository.deleteAllMessage()
//    }

//    fun updateMessage(message: Message){
//        repository.update(message)
//    }

    fun getAll() = allContacts


}