package com.example.capstone

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shashank.sony.fancytoastlib.FancyToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class calendarEdit : AppCompatActivity() {
    private var selectedImage: Uri? = null
    private lateinit var df: DocumentReference
    private lateinit var fStore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var datePicker: Button
    private lateinit var selectedDate: TextView
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var eventId: String
    private lateinit var imageUrl: String
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_edit)

        fStore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        datePicker = findViewById(R.id.datePicker)
        selectedDate = findViewById(R.id.selected)

        val title: EditText = findViewById(R.id.eventTitle)
        val desc: EditText = findViewById(R.id.eventDescription)
        val loc: EditText = findViewById(R.id.eventLocation)
        val timePicker: TimePicker = findViewById(R.id.time_picker)

        val ttle = intent.getStringExtra("title")
        val date = intent.getStringExtra("date")
        val place = intent.getStringExtra("place")
        val description = intent.getStringExtra("description")
        val image = intent.getStringExtra("imageUrl")

        eventId = intent.getStringExtra("eventId") ?: ""
        imageUrl = intent.getStringExtra("imageUrl") ?: ""

        title.setText(ttle)
        desc.setText(description)
        selectedDate.text = date
        loc.setText(place)

        val add: Button = findViewById(R.id.add)
        val uploadMediaButton: ImageView = findViewById(R.id.uploadMediaImage)

        val myCalendar = Calendar.getInstance()
        val myFormat = "MMMM dd, yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        val datepicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            selectedDate.text = sdf.format(myCalendar.time)
        }

        datePicker.setOnClickListener {
            DatePickerDialog(
                this, datepicker, myCalendar.get(Calendar.YEAR), myCalendar.get(
                    Calendar.MONTH
                ), myCalendar.get(Calendar.DAY_OF_MONTH)
            )
                .show()
        }

        add.setOnClickListener {
            val c = Calendar.getInstance()

            if (title.text.isEmpty() or desc.text.isEmpty()) {
                emptyField(title)
                emptyField(desc)
            } else {
                val hour = timePicker.hour.toString()
                val min = timePicker.minute.toString()

                val time = if (timePicker.hour >= 12) {
                    "$hour:$min PM"
                } else {
                    "$hour:$min AM"
                }

                progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Updating Event...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                fStore.collection("EventsAnnouncement")
                    .whereEqualTo("eventDate", date)
                    .whereEqualTo("eventTitle", ttle)
                    .whereEqualTo("eventPlace", place)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val eventTitle = title.text.toString()
                        val description = desc.text.toString()
                        val location = loc.text.toString()
                        if (!querySnapshot.isEmpty) {
                            val document =
                                querySnapshot.documents[0] // Assuming there's only one document
                            val eventId = document.id

                            fStore.collection("EventsAnnouncement")
                                .document(eventId)
                                .update(
                                    mapOf(
                                        "eventTitle" to eventTitle,
                                        "eventPlace" to location,
                                        "eventDescription" to description,
                                        "eventDate" to sdf.format(myCalendar.time),
                                        "eventTime" to time,
                                        "imageUrl" to imageUrl // Retain the original image URL
                                    )
                                )
                                .addOnSuccessListener {
                                    FancyToast.makeText(this,"Event Updated",
                                        FancyToast.LENGTH_LONG,
                                        FancyToast.DEFAULT,false).show()
                                    val intent = Intent(this, navigation::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { exception ->
                                    FancyToast.makeText(this,"Failed to Update",
                                        FancyToast.LENGTH_LONG,
                                        FancyToast.DEFAULT,false).show()
                                }
                        } else {
                            FancyToast.makeText(this,"Failed to Update",
                                FancyToast.LENGTH_LONG,
                                FancyToast.DEFAULT,false).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        FancyToast.makeText(this,"Failed to Update",
                            FancyToast.LENGTH_LONG,
                            FancyToast.DEFAULT,false).show()
                    }
                    .addOnCompleteListener {
                        progressDialog.dismiss()
                    }
            }
        }

        uploadMediaButton.setOnClickListener {
            openImagePicker()
        }

    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImage = data?.data
            val uploadMediaButton: ImageView = findViewById(R.id.uploadMediaImage)

            // Change the icon to a checkmark or any other desired icon
            uploadMediaButton.setImageResource(R.drawable.selected_image_icon)

            // Set a click listener to reset the icon on click
            uploadMediaButton.setOnClickListener {
                // Open the image picker to select a new image
                openImagePicker()

                // Reset the icon to the original upload icon
                uploadMediaButton.setImageResource(R.drawable.image_icon)

                // Remove the click listener to prevent unnecessary icon changes
                uploadMediaButton.setOnClickListener(null)
            }
        }
    }

    private fun emptyField(textField: EditText) {
        if (textField.text.isEmpty()) {
            textField.error = "Empty Field"
        }
    }
}