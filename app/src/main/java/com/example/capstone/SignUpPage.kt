package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.List.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast

class SignUpPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var fStore: FirebaseFirestore
    private lateinit var database: DatabaseReference
    private var valid: Boolean = false
    private lateinit var create: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_page)

        auth = Firebase.auth
        fStore = FirebaseFirestore.getInstance()

        create = findViewById(R.id.submit)
        create.setOnClickListener {
            create.isEnabled = false
            userSignUp()
        }
    }

    private fun userSignUp() {
        database =  Firebase.database.reference
        val store = fStore.collection("users")

        val fname: EditText = findViewById(R.id.firstName)
        val lname: EditText = findViewById(R.id.lastName)
        val email: EditText = findViewById(R.id.email)
        val phone_num: EditText = findViewById(R.id.cellnum)
        val pass: EditText = findViewById(R.id.password)
        val confirmPass: EditText = findViewById(R.id.confirm_pass)

        val Email = email.text.toString()
        val Password = pass.text.toString()
        val Confirm = confirmPass.text.toString()
        val firstName = fname.text.toString()
        val lastName = lname.text.toString()
        val pn = phone_num.text.toString()

        if (!isEmailValid(Email)) {
            create.isEnabled = true
            FancyToast.makeText(this,"Invalid Email Address",
                FancyToast.LENGTH_LONG,
                FancyToast.DEFAULT,false).show()
            return
        }

        if (email.text.isEmpty() or pass.text.isEmpty() or fname.text.isEmpty() or phone_num.text.isEmpty() or lname.text.isEmpty()){
            create.isEnabled = true
            FancyToast.makeText(this,"Fill All Needed Fields",
                FancyToast.LENGTH_LONG,
                FancyToast.DEFAULT,false).show()
            checkFields(fname)
            checkFields(lname)
            checkFields(email)
            checkFields(phone_num)
            checkFields(pass)
            checkFields(confirmPass)
            return
        } else if(Password == Confirm) {
            auth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this) { task ->
                    create.isEnabled = true
                    if (task.isSuccessful) {
                        // Sign in success, To navigation
                        val user = hashMapOf(
                            "uid" to auth.currentUser?.uid,
                            "email" to Email,
                            "code" to "1",
                            "password" to Password
                        )

                        database.child("users").child(auth.currentUser?.uid!!).setValue(User(firstName, lastName, Email, pn, auth.currentUser?.uid))
                        store.add(user)

                        val main = Intent(this, LoginPage::class.java)
                        startActivity(main)
                        FancyToast.makeText(this,"Account Successfully Created",
                            FancyToast.LENGTH_LONG,
                            FancyToast.DEFAULT,false).show()

                    }
                }
                .addOnFailureListener {
                }
        } else {
            create.isEnabled = true
            FancyToast.makeText(this,"Passwords Do Not Match",
                FancyToast.LENGTH_LONG,
                FancyToast.DEFAULT,false).show()
        }
    }

    private fun checkFields(text: EditText){
        valid = if(text.text.isEmpty()){
            create.isEnabled = true
            text.setError("Empty Field")
            false
        }else{
            true
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return emailRegex.matches(email)
    }
}