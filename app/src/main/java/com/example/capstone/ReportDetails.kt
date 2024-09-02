package com.example.capstone

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.capstone.List.Report
import com.google.firebase.database.*
import com.shashank.sony.fancytoastlib.FancyToast

class ReportDetails : AppCompatActivity() {
    private lateinit var reportsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)

        val titleTextView = findViewById<TextView>(R.id.reportTitleTextView)
        val dateTextView = findViewById<TextView>(R.id.reportDateTextView)
        val descriptionTextView = findViewById<TextView>(R.id.reportDescriptionTextView)
        val mediaImageView = findViewById<ImageView>(R.id.mediaImageView)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val reporterTextView = findViewById<TextView>(R.id.reporterTextView)

        val title = intent.getStringExtra("title")
        val date = intent.getStringExtra("timestamp")
        val description = intent.getStringExtra("description")
        val mediaURL = intent.getStringExtra("mediaURL")
        val timestamp = intent.getStringExtra("timestamp")
        val uid = intent.getStringExtra("uid")
        val email = intent.getStringExtra("userID")
        val status = intent.getStringExtra("status")

        titleTextView.text = title
        dateTextView.text = date
        descriptionTextView.text = description
        statusTextView.text = status
        reporterTextView.text = email

        if (!mediaURL.isNullOrEmpty()) {
            mediaImageView.visibility = ImageView.VISIBLE
            Glide.with(this)
                .load(mediaURL)
                .into(mediaImageView)
        } else {
            // Load default image (placeholder) when mediaURL is empty or null
            mediaImageView.visibility = ImageView.VISIBLE
            Glide.with(this)
                .load(R.drawable.report_img_placeholder)
                .into(mediaImageView)
        }

        val acceptButton = findViewById<TextView>(R.id.acceptButton)
        val rejectButton = findViewById<TextView>(R.id.rejectButton)

        // Initialize Firebase database reference
        reportsRef = FirebaseDatabase.getInstance().getReference("reports")

        acceptButton.setOnClickListener {
            // Update report status to Accepted
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to Accept this report?")
                .setPositiveButton("Yes") { _, _ ->
                    reportsRef
                        .orderByChild("title").equalTo(title)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (reportSnapshot in snapshot.children) {
                                    val report = reportSnapshot.getValue(Report::class.java)
                                    if (report?.description == description) {
                                        reportSnapshot.ref.child("status").setValue("Accepted").addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                FancyToast.makeText(applicationContext,"Report Accepted",
                                                    FancyToast.LENGTH_LONG,
                                                    FancyToast.DEFAULT,false).show()
                                                finish()
                                            } else {
                                                FancyToast.makeText(applicationContext,"Failed to Accept Report",
                                                    FancyToast.LENGTH_LONG,
                                                    FancyToast.DEFAULT,false).show()
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
                .setNegativeButton("No", null)
                .show()
        }

        rejectButton.setOnClickListener {
            // Show confirmation dialog before deleting the report
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to Reject this report?")
                .setPositiveButton("Yes") { _, _ ->
                    reportsRef
                        .orderByChild("title").equalTo(title)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (reportSnapshot in snapshot.children) {
                                    val report = reportSnapshot.getValue(Report::class.java)
                                    if (report?.description == description) {
                                        reportSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                FancyToast.makeText(applicationContext,"Report Rejected",
                                                    FancyToast.LENGTH_LONG,
                                                    FancyToast.DEFAULT,false).show()
                                                finish()
                                            } else {
                                                FancyToast.makeText(applicationContext,"Failed Reject Report",
                                                    FancyToast.LENGTH_LONG,
                                                    FancyToast.DEFAULT,false).show()
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}