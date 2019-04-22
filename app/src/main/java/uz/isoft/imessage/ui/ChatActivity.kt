package uz.isoft.imessage.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_chat.*
import uz.imessage.adapter.MessageAdapter
import uz.isoft.imessage.*
import uz.isoft.imessage.database.message.MessageFactory
import uz.isoft.imessage.database.message.MessageViewModel

class ChatActivity : AppCompatActivity() {
    private var adapter = MessageAdapter()
    private var contact = Contact()
    private lateinit var messageViewModel: MessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        try {
            contact = intent.getSerializableExtra("contact") as Contact
        } catch (e: Exception) {
            Toast.makeText(this,"Error contact", Toast.LENGTH_SHORT).show()
        }
//        Toast.makeText(requireContext(), contact.toString(), Toast.LENGTH_SHORT).show()
        val lm = LinearLayoutManager(this)

        lm.stackFromEnd = true
        rv.layoutManager = lm
        rv.adapter = adapter

        messageViewModel = ViewModelProviders.of(this, MessageFactory(App.getInstance(), contact.uid ?: "", PManager.getUID())).get(MessageViewModel::class.java)
        messageViewModel.getChatMessage().observe(this,
            Observer<List<Message>> { t ->
                adapter.setData(t)
            }
        )

        ivSend.setOnClickListener {
            val s = Message(
                date = System.currentTimeMillis(),
                text = etMessage.text.toString(),
                from = PManager.getUID(),
                to = contact.uid,
                status = 0
            )

//            (requireActivity() as MainActivity).mService?.sendMsg(gson.toJson(s).toString())
            etMessage.setText("")
            messageViewModel.insert(s)
            rv.scrollToPosition(adapter.itemCount-1)
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
