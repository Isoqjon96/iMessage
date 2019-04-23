//package uz.isoft.imessage.service
//
//import android.app.Service
//import android.content.Intent
//import android.os.Binder
//import android.os.IBinder
//import android.util.Log
//import io.ktor.client.HttpClient
//import io.ktor.client.features.websocket.WebSockets
//import io.ktor.client.features.websocket.ws
//import io.ktor.http.HttpMethod
//import io.ktor.http.cio.websocket.DefaultWebSocketSession
//import io.ktor.http.cio.websocket.Frame
//import io.ktor.http.cio.websocket.readText
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.channels.filterNotNull
//import kotlinx.coroutines.channels.map
//import kotlinx.coroutines.launch
//
//class KtorService:Service(){
//
//    var ws:DefaultWebSocketSession? = null
//    suspend fun sendMessage(text:String){
//        if(ws==null){
//            onCreateWebSocket()
//        }else{
//            ws?.send(Frame.Text(text))
//        }
//    }
//
//    private fun onCreateWebSocket(){
//        GlobalScope.launch(Dispatchers.IO) {
//            val client = HttpClient().config { install(WebSockets) }
//            client.ws(method = HttpMethod.Get, host = "95.182.79.120", port = 8080, path = "/ws") {
//                ws = this
//
//                //send(Frame.Text("Hello World"))
//                for (message in incoming.map { it as? Frame.Text }.filterNotNull()) {
////                    println("Server said: " + message.readText())
//                    Log.i("serverWeb",message.readText())
//                }
//            }
//        }
//    }
//
//    override fun onCreate() {
//        onCreateWebSocket()
//        super.onCreate()
//    }
//
//    override fun onBind(p0: Intent?): IBinder? {
//        return MyBinder()
//    }
//
//    inner class MyBinder : Binder() {
//
//        val service: KtorService
//            get() = this@KtorService
//    }
//}