package uz.isoft.imessage.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import uz.isoft.imessage.*
import uz.isoft.imessage.database.message.MessageViewModel
import uz.isoft.imessage.main.fragment.MainFragment
import uz.isoft.imessage.service.ChatService

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var mService: ChatService? = null
    private var mBound = false
    private lateinit var model: MessageViewModel
    private var data = ArrayList<Message>()
    private var gson = Gson()
    private var isMain = true
    private lateinit var runnable: Runnable
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, dl, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        dl.addDrawerListener(toggle)
        toggle.syncState()

        nv.setNavigationItemSelectedListener(this)
        setData()
        replaceFragment(MainFragment.getInstance(), "main")

        startService(Intent(applicationContext, ChatService::class.java))

        model = ViewModelProviders.of(this).get(MessageViewModel::class.java)

        model.getNoSendMessage().observe(this, Observer<List<Message>> {
            runnable = Runnable {
                if (!mBound) {
                    bindService(Intent(this, ChatService::class.java), mConnection, Context.BIND_AUTO_CREATE)
                    Toast.makeText(this, "mBound", Toast.LENGTH_SHORT).show()

                } else if (mService?.webSocket?.isOpen != true) {
                    mService?.connectWebSocket()
                    Toast.makeText(this, mService?.webSocket?.isOpen.toString() + "mService", Toast.LENGTH_SHORT).show()
                } else if (checkNetwork()) {
                    it.forEach { m ->

                        val temp = BaseResponse(code = 0,error = "",result = m)
                        if (mService?.sendMsg(gson.toJson(temp).toString()) == true && m.status != 1) {
                            m.status = 1
                            model.updateMessage(m)
                        }
                    }
                }
                if (data.size == 0) {
                    handler.removeCallbacks(runnable)
                }
            }
            handler.postDelayed(runnable, 1000)
        })
    }

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ChatService.MyBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mBound = false
            mService = null
        }
    }

    override fun onStart() {
        if (!mBound) {
            bindService(Intent(this, ChatService::class.java), mConnection, Context.BIND_AUTO_CREATE)
        }
        super.onStart()

    }

    override fun onStop() {
        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
        super.onStop()
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

        }

        dl.closeDrawer(GravityCompat.START)
        return true
    }

    fun replaceFragment(fragment: Fragment, tag: String) {
        if(tag=="main"){
            isMain = true
            dl.visibility = View.VISIBLE
            vChat.visibility = View.GONE
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment, tag)
                .commit()
        }else{
            isMain = false
            dl.visibility = View.GONE
            vChat.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .replace(R.id.container1, fragment, tag)
                .commit()
        }
    }

    override fun onBackPressed() {
        when {
            dl.isDrawerOpen(GravityCompat.START) -> dl.closeDrawer(GravityCompat.START)
            !isMain -> replaceFragment(MainFragment.getInstance(),"main")
            else -> super.onBackPressed()
        }
    }

    private fun setData() {
        var s = ""
        for (x in 0..12) {
            if (x == 4 || x == 6 || x == 9 || x == 11) s += " "
            s += PManager.getPhone()[x]
        }
        nv.getHeaderView(0).findViewById<AppCompatTextView>(R.id.tvName).text = PManager.getName()
        nv.getHeaderView(0).findViewById<AppCompatTextView>(R.id.tvPhone).text = s

        if (PManager.getImage().isNotEmpty()) {
            Picasso.get()
                .load(SERVER_IMAGE_ADDRESS + PManager.getImage())
                .placeholder(R.drawable.ic_account)
                .centerCrop()
                .fit()
                .into(nv.getHeaderView(0).findViewById<CircleImageView>(R.id.ivNavigationView))
        }
    }

    private fun checkNetwork(): Boolean {
        var wifiDataAvailable = false
        var mobileDataAvailable = false
        val conManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = conManager.allNetworkInfo
        for (netInfo in networkInfo) {
            if (netInfo.typeName.equals("WIFI", true))
                if (netInfo.isConnected)
                    wifiDataAvailable = true
            if (netInfo.typeName.equals("MOBILE", true))
                if (netInfo.isConnected)
                    mobileDataAvailable = true
        }
        return wifiDataAvailable || mobileDataAvailable
    }
}
