package com.example.capstone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.capstone.List.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class Profile : Fragment()  {
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchAndDisplayUserData(currentUser.uid)
        }

        return view
    }

    private fun fetchAndDisplayUserData(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val userReference = database.reference.child("users").child(uid)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User data found in the database
                    val user = snapshot.getValue(User::class.java)

                    // Update the UI with the user data
                    if (user != null) {
                        updateProfile(user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun updateProfile(user: User) {
        val fullName = "${user.fName} ${user.lName}"
        val nameTextView = view?.findViewById<TextView>(R.id.myName)
        val emailTextView = view?.findViewById<TextView>(R.id.myEmail)
        val phoneTextView = view?.findViewById<TextView>(R.id.myPhone)

        nameTextView?.text = fullName
        emailTextView?.text = user.email
        phoneTextView?.text = user.phoneNum
    }
}