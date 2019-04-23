package uz.isoft.imessage.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import uz.isoft.imessage.*
import uz.isoft.imessage.database.message.MessageRepository
import uz.isoft.imessage.main.fragment.MainFragment
import uz.isoft.imessage.start.SplashActivity
import java.net.URI
import java.net.URISyntaxException

class ChatService : Service() {

    var webSocket: WebSocketClient? = null
    private var isOpen = false
    private var repository: MessageRepository? = null
    private var gson = Gson()

    override fun onCreate() {
        connectWebSocket()
        repository = MessageRepository(application)
        Toast.makeText(applicationContext, "onstart", Toast.LENGTH_SHORT).show()
        super.onCreate()
    }

    fun connectWebSocket() {
        Toast.makeText(applicationContext, "Service", Toast.LENGTH_SHORT).show()
        val uri: URI
        try {
            uri = URI(WEB_SOCKET_URL)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        val headers: Map<String, String> = mapOf("uid" to PManager.getUID())

        webSocket = object : WebSocketClient(uri, headers) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.i("websocket", "onOpen")
                this@ChatService.isOpen = true
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.i("websocket", "onclose")
                Toast.makeText(applicationContext, "onclose", Toast.LENGTH_SHORT).show()
                this@ChatService.isOpen = false
            }

            override fun onMessage(message: String?) {
                Log.i("websocketMes", message)
                val baseResponse = gson.fromJson(message, BaseResponse::class.java)
                if (baseResponse.code == 0) {
                    val messageT: Message = gson.fromJson(baseResponse.result.toString(), Message::class.java)
                    messageT.status=1
                    repository?.insert(messageT)
//                val t = MainFragment.adapter.getData().map {
//                    it.uid
//                }
//                if(!t.contains(temp.result?.from)){
//                    MainFragment.adapter.setData()
//                  }
//                    val pendingIntent = PendingIntent.getActivity(
//                        applicationContext, 0, Intent(applicationContext, SplashActivity::class.java),
//                        PendingIntent.FLAG_ONE_SHOT
//                    )
//
//                    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//
//                    val notificationBuilder = NotificationCompat.Builder(applicationContext)
//                        .setContentText(messageT.text)
//                        .setAutoCancel(true)
//                        .setSmallIcon(R.drawable.logo)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent)
//                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                    notificationManager.notify(0, notificationBuilder.build())
//                }
                    val pendingIntent = PendingIntent.getActivity(
                        applicationContext,
                        System.currentTimeMillis().toInt(),
                        Intent(applicationContext, SplashActivity::class.java)
                        ,
                        0
                    )

                    val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                    val inbox = NotificationCompat.InboxStyle()
                    inbox.setBigContentTitle(messageT.from)
                    inbox.addLine(messageT.text)

                    val notificationBuilder = NotificationCompat.Builder(applicationContext)
                        .setContentTitle("${messageT.from} dan xabar")
                        .setContentText(messageT.text)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setSmallIcon(R.drawable.ic_launcher_web)
                        .setSound(defaultSoundUri)
                        .setStyle(inbox)
                        .setContentIntent(pendingIntent)

                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(0, notificationBuilder.build())
                }
            }
            override fun onError(ex: Exception?) {
                Log.i("websocket", "onerror")
            }
        }
        webSocket?.connect()
    }

    fun sendMsg(s: String): Boolean {

        return if (isOpen && webSocket != null) {
            webSocket?.send(s)
            Log.i("websockets", s)
            true
        } else {
            connectWebSocket()
            false
        }
    }

    inner class MyBinder : Binder() {

        val service: ChatService
            get() = this@ChatService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return MyBinder()
    }


    override fun onDestroy() {
        Toast.makeText(applicationContext, "onclose service", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }
}