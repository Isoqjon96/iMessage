package uz.imessage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_message.view.*
import uz.isoft.imessage.Message
import uz.isoft.imessage.PManager
import uz.isoft.imessage.R
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var message = ArrayList<Message>()

    fun setData(message: List<Message>){
        this.message = message as ArrayList<Message>
        notifyDataSetChanged()
    }
//    fun addData(messages: List<Message>){
//        this.message.addAll(messages)
//        notifyDataSetChanged()
//    }

    override fun getItemViewType(position: Int): Int {
        return if (message[position].from.equals(PManager.getUID())) {
            0
        } else {
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            MyMessageHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message_other, parent, false)
            OtherMessageHolder(v)
        }
    }

    override fun getItemCount() = message.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (message[position].from.equals(PManager.getUID()) ) {
            (holder as MyMessageHolder).onBind(message = message[position])
        } else {
            (holder as OtherMessageHolder).onBind(message = message[position])
        }
    }

    inner class MyMessageHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun onBind(message: Message) {
            itemView.apply {

                tvMessage.text = message.text
                tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.date?:0))
                ivSend.setOnClickListener {
                    Toast.makeText(this.context ,"asa",Toast.LENGTH_SHORT).show()
                }
            }
//            when(message.status){
//                0->Picasso.get()
//                    .load(R.drawable.ic_access_time)
//                    .into(itemView.ivSend)
//                1->Picasso.get()
//                    .load(R.drawable.ic_check)
//                    .into(itemView.ivSend)
//
//                else->Picasso.get()
//                    .load(R.drawable.ic_check_all)
//                    .into(itemView.ivSend)
//
//            }
        }
    }

    inner class OtherMessageHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun onBind(message: Message) {
            itemView.apply {
                tvMessage.text = message.text
                tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.date?:0))
            }
        }
    }
}