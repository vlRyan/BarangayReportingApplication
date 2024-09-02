package com.example.capstone.NotYetAvailable

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.capstone.List.Report
import com.example.capstone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shashank.sony.fancytoastlib.FancyToast

class ReportInformation : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var reportsRef: DatabaseReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_information)

        database = FirebaseDatabase.getInstance()
        reportsRef = database.getReference("reports")

        val titletxt: TextView = findViewById(R.id.reporttitle)
        val descriptiontxt: TextView = findViewById(R.id.reportdesc)
        val datetxt: TextView = findViewById(R.id.date)
        val image: ImageView = findViewById(R.id.mediaImageView)
        val username: TextView = findViewById(R.id.userEmail)
        val imageFrame: FrameLayout = findViewById(R.id.imageFrame)
        val deleteBtn: TextView = findViewById(R.id.deleteBtn)

        val title = intent.getStringExtra("title")
        val date = intent.getStringExtra("date")
        val description = intent.getStringExtra("description")
        val mediaURL = intent.getStringExtra("mediaURL")
        val userMail = intent.getStringExtra("userID")

        // Check if the current user can edit/delete the entry
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail == "admin@email.com") {
            // Show the "Delete" button
            deleteBtn.visibility = View.VISIBLE

            // Set click listeners for Delete actions
            deleteBtn.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirmation")
                builder.setMessage("Do you really want to delete this Report?")
                builder.setPositiveButton("Yes") { dialog, _ ->
                    reportsRef
                        .orderByChild("title").equalTo(title)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (reportSnapshot in snapshot.children) {
                                    val report = reportSnapshot.getValue(Report::class.java)
                                    if (report?.description == description) {
                                        reportSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                FancyToast.makeText(applicationContext,"Report Deleted",FancyToast.LENGTH_LONG,FancyToast.DEFAULT,false).show()
                                                finish()
                                            } else {
                                                FancyToast.makeText(applicationContext,"Failed to Delete Report",FancyToast.LENGTH_LONG,FancyToast.DEFAULT,false).show()
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                FancyToast.makeText(applicationContext,"Failed to Delete Report",
                                    FancyToast.LENGTH_LONG,
                                    FancyToast.DEFAULT,false).show()
                            }
                        })
                }

                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }
        }

        titletxt.text = title
        datetxt.text = date
        descriptiontxt.text = description
        username.text = userMail

        if (!mediaURL.isNullOrEmpty()) {
            imageFrame.visibility = ImageView.VISIBLE
            Glide.with(this)
                .load(mediaURL)
                .into(image)
        } else {
            // Load default image (placeholder) when mediaURL is empty or null
            imageFrame.visibility = ImageView.VISIBLE
            Glide.with(this)
                .load(R.drawable.report_img_placeholder)
                .into(image)
        }

    }
}