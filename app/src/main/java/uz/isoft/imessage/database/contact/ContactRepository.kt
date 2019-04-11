package uz.isoft.imessage.database.contact

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import uz.isoft.imessage.ApiFactory
import uz.isoft.imessage.Contact

class ContactRepository(application: Application) {

    private var contactDao: ContactDao
    private var allContact: LiveData<List<Contact>>

    init {
        val database: ContactDatabase = ApiFactory.getContactDataBase(application.applicationContext)

        contactDao = database.contactDao()
        allContact = contactDao.getAll()
    }

//    fun update(contact: Contact){
//        UpdateMessageAsyncTask(contactDao).execute(message)
//    }

    fun insert(contact: Contact) {
        InsertMessageAsyncTask(contactDao).execute(contact)
    }

    fun getAll() = allContact

    private class InsertMessageAsyncTask(val contactDao: ContactDao) : AsyncTask<Contact, Unit, Unit>() {

        override fun doInBackground(vararg p0: Contact?) {
            contactDao.insert(p0[0]!!)
        }
    }

//    private class UpdateContactAsyncTask(val contactDao: ContactDao) : AsyncTask<Contact, Unit, Unit>() {
//
//        override fun doInBackground(vararg p0: Contact?) {
//            contactDao.update(1,p0[0]?.id?:0)
//        }
//    }

//    private class DeleteAllMessageAsyncTask(val messageDao: MessageDao) : AsyncTask<Unit, Unit, Unit>() {
//        override fun doInBackground(vararg p0: Unit?) {
//            messageDao.deleteAll()
//        }
//    }
}
