package uz.isoft.imessage.database.message

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import uz.isoft.imessage.ApiFactory
import uz.isoft.imessage.Message

class MessageRepository(application: Application) {

    private var messageDao: MessageDao
    private var allMessage: LiveData<List<Message>>
    private var noSendMessage: LiveData<List<Message>>
    private var noReadsMessage: LiveData<List<Message>>

    init {
        val database: MessageDatabase = ApiFactory.getMessage(application.applicationContext)

        messageDao = database.messageDao()
        allMessage = messageDao.getAll()
        noSendMessage = messageDao.getNoSendMessage()
        noReadsMessage= messageDao.getNoRead()
    }

    fun update(message: Message){
        UpdateMessageAsyncTask(messageDao).execute(message)
    }

    fun updateFlag(uid: String){
        UpdateFlagAsyncTask(messageDao).execute(uid)
    }

    fun insert(message: Message) {
        InsertMessageAsyncTask(messageDao).execute(message)
    }

    fun deleteAllMessage() {
        DeleteAllMessageAsyncTask(messageDao).execute()
    }

    fun getAllMessage() = allMessage

    fun getUserMessage(to: String, from: String) = messageDao.getUserMessage(to, from)

    fun getNoSendMessage() = noSendMessage

    fun getNoReads() = noReadsMessage

    private class InsertMessageAsyncTask(val messageDao: MessageDao) : AsyncTask<Message, Unit, Unit>() {

        override fun doInBackground(vararg p0: Message?) {
            messageDao.insert(p0[0]!!)
        }
    }

    private class UpdateMessageAsyncTask(val messageDao: MessageDao) : AsyncTask<Message, Unit, Unit>() {

        override fun doInBackground(vararg p0: Message?) {
            messageDao.update(1,p0[0]?.id?:0)
        }
    }
    private class UpdateFlagAsyncTask(val messageDao: MessageDao) : AsyncTask<String, Unit, Unit>() {

        override fun doInBackground(vararg p0: String?) {
            messageDao.updateFlag(p0[0]?:"")
        }
    }

    private class DeleteAllMessageAsyncTask(val messageDao: MessageDao) : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg p0: Unit?) {
            messageDao.deleteAll()
        }
    }
}