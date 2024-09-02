package com.example.capstone.Adapters

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.List.Message
import com.example.capstone.List.User
import com.example.capstone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.coroutineContext

class UserAdapter(private var userList: ArrayList<User>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun onItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.user_layout, parent, false)

        return ViewHolder(view, mListener)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = userList[position]
        val db = FirebaseDatabase.getInstance().reference

        db.child("chats").child("${currentUser.uid}${FirebaseAuth.getInstance().currentUser?.uid}")
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (p0 in snapshot.children) {
                        val message = p0.getValue(Message::class.java)
                        holder.lastMessage.text = message?.message
                        holder.lastDate.text = message?.dateSent
                        if (currentUser.uid == message?.senderID){
                            holder.lastMessage.setTextColor(Color.parseColor("#000000"))
                            holder.textName.setTextColor(Color.parseColor("#000000"))
                            holder.textLname.setTextColor(Color.parseColor("#000000"))
                            holder.lastDate.setTextColor(Color.parseColor("#000000"))
                            holder.lastMessage.setTypeface(null, Typeface.BOLD)
                            holder.textName.setTypeface(null, Typeface.BOLD)
                            holder.textLname.setTypeface(null, Typeface.BOLD)
                            holder.lastDate.setTypeface(null, Typeface.BOLD)
                        }else{
                            holder.lastMessage.setTextColor(Color.parseColor("#6b6b6b"))
                            holder.textName.setTextColor(Color.parseColor("#6b6b6b"))
                            holder.textLname.setTextColor(Color.parseColor("#6b6b6b"))
                            holder.lastDate.setTextColor(Color.parseColor("#6b6b6b"))
                            holder.lastMessage.setTypeface(null, Typeface.NORMAL)
                            holder.textName.setTypeface(null, Typeface.NORMAL)
                            holder.textLname.setTypeface(null, Typeface.NORMAL)
                            holder.lastDate.setTypeface(null, Typeface.NORMAL)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        holder.textName.text = currentUser.fName
        holder.textLname.text = currentUser.lName
    }

    class ViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.fname)
        val textLname: TextView = itemView.findViewById(R.id.lname)
        val lastMessage: TextView = itemView.findViewById(R.id.last_message)
        val lastDate: TextView = itemView.findViewById(R.id.last_date)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}