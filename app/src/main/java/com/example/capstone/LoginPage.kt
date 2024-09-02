package com.example.capstone

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast

class LoginPage : AppCompatActivity() {
    private lateinit var fAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var loginbtn: Button

    companion object {
        const val ADMIN_EMAIL = "barangayirisan@gmail.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        fAuth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference.child("users")

        // Check if the user is already authenticated
            if (fAuth.currentUser != null) {
                // User is already signed in, proceed to the app
                startNavigationActivity()
            }

        val signup: TextView = findViewById(R.id.signup)
        signup.setOnClickListener {
            val signuppage = Intent(this, TermsAndCondition::class.java)
            startActivity(signuppage)
        }

        loginbtn = findViewById(R.id.LoginBtn)
        loginbtn.setOnClickListener {
            loginbtn.isEnabled = false // Prevent multiple login
            login()
        }
    }

    private fun login() {
        val Uname: EditText = findViewById(R.id.Username)
        val Pswrd: EditText = findViewById(R.id.Password)
        val login_email = Uname.text.toString()
        val login_pass = Pswrd.text.toString()

        if (Uname.text.isEmpty() or Pswrd.text.isEmpty()) {
            loginbtn.isEnabled = true
            FancyToast.makeText(
                baseContext, "Fill All Needed Fields",
                FancyToast.LENGTH_LONG,
                FancyToast.DEFAULT, false
            ).show()
            return
        }

        fAuth.signInWithEmailAndPassword(login_email, login_pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Save the user's login state
                    fAuth.currentUser?.let { user ->
                        val userEmail = user.email
                        fetchAndDisplayUserFirstName(userEmail)
                        startNavigationActivity()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    loginbtn.isEnabled = true
                    FancyToast.makeText(
                        baseContext, "Failed to Login",
                        FancyToast.LENGTH_LONG,
                        FancyToast.DEFAULT, false
                    ).show()
                }
            }
    }

    private fun fetchAndDisplayUserFirstName(email: String?) {
        email?.let {
            database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val fname = ds.child("fname").value.toString()
                        FancyToast.makeText(
                            baseContext, "Welcome $fname",
                            FancyToast.LENGTH_LONG, FancyToast.DEFAULT, false
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors
                    FancyToast.makeText(
                        baseContext, "Failed to fetch user data",
                        FancyToast.LENGTH_LONG, FancyToast.DEFAULT, false
                    ).show()
                }
            })
        }
    }

    private fun startNavigationActivity() {
        val intent = Intent(this, navigation::class.java)
        startActivity(intent)
        finish() // Finish the LoginActivity so the user cannot go back to it without signing out
    }
}