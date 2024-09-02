package com.example.capstone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class adminResponse : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_response)

        val title = intent.getStringExtra("title")
        val date = intent.getStringExtra("date")
        val description = intent.getStringExtra("description")
        val uid = intent.getStringExtra("uid")

        val barangayResponseEditText = findViewById<EditText>(R.id.barangayResponseEditText)
        val submitResponseButton = findViewById<Button>(R.id.submitResponseButton)

        submitResponseButton.setOnClickListener {
//            val barangayResponse = barangayResponseEditText.text.toString()
//
//            // Update the report with the barangay response
//            updateReportWithResponse(title, barangayResponse)
//
//            updateReportStatus("Accepted")
//
//            // Optionally, you can navigate back to another activity
//            val intent = Intent(this, navigation::class.java)
//            startActivity(intent)
//            finish()
            Toast.makeText(this, "$title \n $description", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateReportWithResponse(title: String?, barangayResponse: String) {
        if (title == null) {
            // Handle the error, maybe show a toast or log a message
            return
        }

        // Update the report in the Firestore database with the barangay response
        val db = FirebaseFirestore.getInstance()
        val reportsCollection = db.collection("reports")

        reportsCollection
            .whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    // Update the "barangayResponse" field
                    document.reference.update("barangayResponse", barangayResponse)
                        .addOnSuccessListener {
                            // Update successful
                            Toast.makeText(this, "Report Posted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            // Handle the error
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }

    private fun updateReportStatus(newStatus: String) {
        val db = FirebaseFirestore.getInstance()
        val reportsCollection = db.collection("reports")

        val title = intent.getStringExtra("title")

        // Update the report status
        reportsCollection.whereEqualTo("title", title)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val reportId = document.id
                    reportsCollection.document(reportId).update("status", newStatus)
                        .addOnSuccessListener {
                            // Report status updated successfully
                        }
                        .addOnFailureListener { e ->
                            // Handle the error
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }

}