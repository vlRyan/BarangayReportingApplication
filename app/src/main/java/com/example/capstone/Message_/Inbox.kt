package com.example.capstone.message

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.Adapters.UserAdapter
import com.example.capstone.List.User
import com.example.capstone.Message_.AdminMessage
import com.example.capstone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Inbox : Fragment() {
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var userList: ArrayList<User>
    private lateinit var adapter: UserAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val db = database.child("users")
        val chatList = database.child("ChatList")

        userList = arrayListOf()
        adapter = UserAdapter(userList)
        messagesRecyclerView = view.findViewById(R.id.messages)
        messagesRecyclerView.layoutManager = LinearLayoutManager(context)
        messagesRecyclerView.adapter = adapter

        db.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(User::class.java)

                    chatList.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val chatListChildIds = mutableListOf<String>()

                            for (p0 in snapshot.children) {
                                // Get the key (child ID) and add it to the list
                                val childId = p0.key
                                if (childId != null) {
                                    chatListChildIds.add(childId)
                                }

                                if (currentUser?.uid.equals(childId.toString())) {
                                    userList.add(currentUser!!)
                                }
                            }
                            val adapter = UserAdapter(userList)
                            messagesRecyclerView.adapter = adapter
                            adapter.onItemClickListener(object : UserAdapter.onItemClickListener {
                                override fun onItemClick(position: Int) {
                                    val intent = Intent(context, AdminMessage::class.java)
                                    intent.putExtra("fName", userList[position].fName)
                                    intent.putExtra("lName", userList[position].lName)
                                    intent.putExtra("uid", userList[position].uid)
                                    startActivity(intent)
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        return view
    }
}