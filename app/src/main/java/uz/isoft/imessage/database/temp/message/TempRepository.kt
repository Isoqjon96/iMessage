package uz.isoft.imessage.database.temp.message

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import uz.isoft.imessage.ApiFactory
import uz.isoft.imessage.TempMessage

class TempRepository(application:Application){
    private var messageDao: TempMessageDao
    private var allMessage: LiveData<List<TempMessage>>

    init {
        val database: TempDatabase = ApiFactory.getTemp(application.applicationContext)

        messageDao = database.tempMessageDao()
        allMessage = messageDao.getAll()

    }

    fun insert(message: TempMessage) {
        InsertTempAsyncTask(messageDao).execute(message)
    }

    fun deleteTemp(message: TempMessage){
        DeleteTempAsyncTask(messageDao).execute(message)
    }

    fun deleteAllMessage() {
        DeleteAllTempAsyncTask(messageDao).execute()
    }

    fun getAllMessage() = allMessage

    private class InsertTempAsyncTask(val tempDao: TempMessageDao) : AsyncTask<TempMessage, Unit, Unit>() {

        override fun doInBackground(vararg p0: TempMessage?) {
            tempDao.insert(p0[0]!!)
        }
    }

    private class DeleteAllTempAsyncTask(val messageDao: TempMessageDao) : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg p0: Unit?) {
            messageDao.deleteAll()
        }
    }

    private class DeleteTempAsyncTask(val messageDao: TempMessageDao) : AsyncTask<TempMessage, Unit, Unit>() {
        override fun doInBackground(vararg p0: TempMessage?) {
            messageDao.delete(p0[0]!!)
        }
    }
}