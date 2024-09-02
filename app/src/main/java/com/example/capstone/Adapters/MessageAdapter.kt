package com.example.capstone.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.List.Message
import com.example.capstone.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(val context: Context, private var messageList : ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ITEM_RECEIVE = 1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1){
            val view : View = LayoutInflater.from(context).inflate(R.layout.received, parent, false)
            ReceivedViewHolder(view)
        }else{
            val view : View = LayoutInflater.from(context).inflate(R.layout.sent, parent, false)
            SendViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]

        if(holder.javaClass == SendViewHolder::class.java){
            val viewHolder = holder as SendViewHolder

            holder.sentMessage.text = currentMessage.message
            holder.date.text = currentMessage.dateSent

        }else{
            val viewHolder = holder as ReceivedViewHolder
            holder.receivedMessage.text = currentMessage.message
            holder.date.text = currentMessage.dateSent
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]

        return if (FirebaseAuth.getInstance().currentUser?.uid == currentMessage.senderID){
            ITEM_SENT
        }else{
            ITEM_RECEIVE
        }
    }

    class SendViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val sentMessage = itemView.findViewById<TextView>(R.id.sent)
        val date = itemView.findViewById<TextView>(R.id.date)
    }

    class ReceivedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receivedMessage = itemView.findViewById<TextView>(R.id.received)
        val date = itemView.findViewById<TextView>(R.id.date)
    }

}