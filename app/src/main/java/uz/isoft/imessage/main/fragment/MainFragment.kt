package uz.isoft.imessage.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_main.*
import uz.isoft.imessage.Contact
import uz.isoft.imessage.Message
import uz.isoft.imessage.R
import uz.isoft.imessage.adapter.ContactAdapter
import uz.isoft.imessage.database.contact.ContactViewModel
import uz.isoft.imessage.database.message.MessageViewModel
import uz.isoft.imessage.ui.MainActivity

class MainFragment : Fragment(), ContactAdapter.CallBack {

    companion object {
        private val fragment = MainFragment()
        fun getInstance() = fragment
        var adapter = ContactAdapter()
    }

    private lateinit var contactView: ContactViewModel
    private lateinit var model: MessageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter
        adapter.setCallBack(this)

        contactView = ViewModelProviders.of(this).get(ContactViewModel::class.java)
        contactView.getAll().observe(this, Observer {
            adapter.setData(it as ArrayList<Contact>)
        })
        model = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        model.getNoReads().observe(this, Observer<List<Message>> {
            adapter.setCount(it as ArrayList<Message>)
        })
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onItemClick(contactModel: Contact) {
        val a = Contact(name = contactModel.name, uid = contactModel.uid, phone = contactModel.phone)
        model.update(contactModel.uid?:"")
        (requireContext() as MainActivity).replaceFragment(ChatFragment.getInstance(a), "chat")
    }
}