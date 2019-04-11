package uz.isoft.imessage.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_chat.*
import uz.imessage.adapter.MessageAdapter
import uz.isoft.imessage.*
import uz.isoft.imessage.database.message.MessageFactory
import uz.isoft.imessage.database.message.MessageViewModel
import uz.isoft.imessage.database.temp.message.TempViewModel
import uz.isoft.imessage.main.MainActivity

class ChatFragment : Fragment() {

    private var adapter = MessageAdapter()
    private var contact = Contact()
    private lateinit var messageViewModel: MessageViewModel


    companion object {
        fun getInstance(contact: Contact) = ChatFragment().apply {
            arguments = Bundle().apply {
                putSerializable("contact", contact)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            contact = arguments?.getSerializable("contact") as Contact
        } catch (e: Exception) {
            Toast.makeText(requireContext(),"Error contact",Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(requireContext(), contact.toString(), Toast.LENGTH_SHORT).show()
        val lm = LinearLayoutManager(requireContext())

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

        super.onViewCreated(view, savedInstanceState)
    }

}
