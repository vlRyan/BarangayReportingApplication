package com.example.capstone.Dashboard

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.capstone.R
import com.example.capstone.calendarEdit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast

class dashboardEvents : AppCompatActivity() {
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_events)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val title: TextView = findViewById(R.id.title)
        val description: TextView = findViewById(R.id.description)
        val date: TextView = findViewById(R.id.whenday)
        val location: TextView = findViewById(R.id.location)
        val time: TextView = findViewById(R.id.whentime)
        val image: ImageView = findViewById(R.id.image)
        val edit: TextView = findViewById(R.id.edit)
        val delete: TextView = findViewById(R.id.delete)
        val edLayout: LinearLayout = findViewById(R.id.editDelete)

        val bundle : Bundle?= intent.extras
        val titl = bundle?.getString("title")
        val desc = bundle?.getString("description")
        val dat = bundle?.getString("date")
        val loca = bundle?.getString("place")
        val tim = bundle?.getString("time")
        val imageUrl = bundle?.getString("imageUrl")

        title.text = titl
        date.text = dat
        description.text = desc
        location.text = loca
        time.text = tim

        // Load image using Glide
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.announcement_placeholder)
            .error(R.drawable.announcement_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(image)

        if (auth.currentUser?.uid == "uFukrYZAXha9ehTt0bwIXrS9tvK2"){
            edLayout.visibility = View.VISIBLE

            edit.setOnClickListener {
                val intent = Intent(this, calendarEdit::class.java)
                intent.putExtra("title", titl)
                intent.putExtra("date", dat)
                intent.putExtra("place", loca)
                intent.putExtra("description", desc)
                intent.putExtra("time", tim)
                intent.putExtra("imageUrl", imageUrl)
                startActivity(intent)
            }

            delete.setOnClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirmation")
                builder.setMessage("Do you really want to delete this Announcement?")

                builder.setPositiveButton("Yes") { dialog, _ ->
                    db.collection("EventsAnnouncement")
                        .whereEqualTo("eventTitle", titl)
                        .whereEqualTo("eventDate", dat)
                        .get()
                        .addOnSuccessListener { result ->
                            for (document in result) {
                                document.reference.delete()
                                FancyToast.makeText(this,"Event Deleted",
                                    FancyToast.LENGTH_LONG,
                                    FancyToast.DEFAULT,false).show()

                            }
                            dialog.dismiss()
                            finish()
                        }
                        .addOnFailureListener { exception ->
                            // Handle error
                        }
                }

                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
            }

        }
    }
}