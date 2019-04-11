package uz.isoft.imessage

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

class App : MultiDexApplication() {
    companion object {
        private lateinit var app: App
        fun getInstance () : App = app

    }

    override fun onCreate() {
        app = this
        super.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }


}