package com.example.capstone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.shashank.sony.fancytoastlib.FancyToast

class TermsAndCondition : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_condition)

        val read = findViewById<CheckBox>(R.id.checkbox)

        val disagree = findViewById<TextView>(R.id.disagreeButton)
        disagree.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        val agree = findViewById<Button>(R.id.agreeButton)
        agree.setOnClickListener {
            if (read.isChecked) {
                val intent = Intent(this, SignUpPage::class.java)
                startActivity(intent)
            } else {
                FancyToast.makeText(this,"Agree to the Terms and Conditions to Proceed",
                    FancyToast.LENGTH_LONG,
                    FancyToast.DEFAULT,false).show()
            }
        }
    }
}