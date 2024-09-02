package com.example.capstone

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.shashank.sony.fancytoastlib.FancyToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserReport : AppCompatActivity() {
    private lateinit var fAuth: FirebaseAuth
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var uploadMediaButton: LinearLayout
    private lateinit var uploadMediaImage: ImageView
    private lateinit var imagePickerActivityResult: ActivityResultLauncher<Intent>
    private lateinit var user: String
    private lateinit var database: FirebaseDatabase
    private var mediaUri: Uri? = null
    private var submissionInProgress = false
    private lateinit var progressDialog: ProgressDialog
    private lateinit var spinner: Spinner

    companion object {
        const val PICK_MEDIA_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_report)

        fAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val sdf = SimpleDateFormat("MMMM dd, yyyy / HH:mm a", Locale.getDefault())
        val sdf1 = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate1 = sdf1.format(Date())
        val currentDate = sdf.format(Date())
        val reportsRef = database.getReference("reports")
        val checkBox = findViewById<CheckBox>(R.id.checkBox2)
        val db = Firebase.firestore

        titleEditText = findViewById<EditText>(R.id.topicReportInput)
        descriptionEditText = findViewById<EditText>(R.id.descriptionReportInput)
        uploadMediaButton = findViewById<LinearLayout>(R.id.uploadMediaButton)
        uploadMediaImage = findViewById<ImageView>(R.id.uploadMediaImage)
        submitButton = findViewById<Button>(R.id.submitButton)

        imagePickerActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the selected media
                    mediaUri = result.data?.data
                    // Update the UI or do something with the selected media
                    uploadMediaImage.setImageResource(R.drawable.selected_image_icon)
                }
            }

        uploadMediaButton.setOnClickListener {
            // Open a file picker or camera app to select image
            val mediaIntent = Intent(Intent.ACTION_PICK)
            mediaIntent.type = "image/*"
            imagePickerActivityResult.launch(mediaIntent)
        }

        spinner = findViewById(R.id.report_type)
        val adapter = ArrayAdapter.createFromResource(this, R.array.typeOfReport, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        submitButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            user = fAuth.currentUser?.email.toString()
            val problem = spinner.selectedItem.toString()


            if (titleEditText.text.isEmpty() or descriptionEditText.text.isEmpty()) {
                checkFields(titleEditText)
                checkFields(descriptionEditText)
                FancyToast.makeText(this,"Fill All Needed Fields",
                    FancyToast.LENGTH_LONG,
                    FancyToast.DEFAULT,false).show()
            }
            else if(problem == "Problem Type"){
                FancyToast.makeText(this,"Please Select Problem Type",
                    FancyToast.LENGTH_LONG,
                    FancyToast.DEFAULT,false).show()
            }
            else {

                user = if (checkBox.isChecked) {
                    "Anonymous"
                } else {
                    fAuth.currentUser?.email.toString()
                }

                // Prevent double submission
                if (submissionInProgress) {
                    return@setOnClickListener
                }

                progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Submitting Report...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                if (mediaUri != null) {
                    submissionInProgress = true

                    val storageRef = FirebaseStorage.getInstance().reference
                    val mediaFileName = UUID.randomUUID().toString()
                    val mediaRef = storageRef.child(mediaFileName)

                    mediaRef.putFile(mediaUri!!)
                        .addOnSuccessListener { taskSnapshot ->
                            // Media uploaded successfully; get the download URL
                            mediaRef.downloadUrl
                                .addOnSuccessListener { uri ->
                                    val mediaURL = uri.toString()
                                    // Create a new report entry in Realtime Database
                                    val reportId = reportsRef.push().key
                                    val report = Report(
                                        fAuth.currentUser?.uid,
                                        title,
                                        description,
                                        mediaURL,
                                        currentDate,
                                        "Pending",
                                        problem,
                                        user
                                    )
                                    val reportStat = Report(
                                        fAuth.currentUser?.uid,
                                        title,
                                        description,
                                        null, // No media URL
                                        currentDate1,
                                        "Pending",
                                        problem,
                                        user
                                    )
                                    db.collection("reportStatistics").document().set(reportStat)

                                    reportId?.let { id ->
                                        reportsRef.child(id).setValue(report)
                                            .addOnSuccessListener {
                                                // Report added successfully
                                                FancyToast.makeText(this,"Report Submitted",
                                                    FancyToast.LENGTH_LONG,
                                                    FancyToast.DEFAULT,false).show()
                                                onBackPressed()
                                            }
                                            .addOnFailureListener { e ->
                                                // Handle the error
                                                FancyToast.makeText(this,"Report Submission Failed",
                                                    FancyToast.LENGTH_LONG,
                                                    FancyToast.DEFAULT,false).show()
                                            }
                                            .addOnCompleteListener {
                                                submissionInProgress = false // Reset the submission flag
                                                progressDialog.dismiss()
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle the error in getting the download URL
                                }
                        }
                        .addOnFailureListener { e ->
                            // Handle the error in uploading media
                        }
                } else {
                    // Media is not selected, you can proceed without media
                    val reportId = reportsRef.push().key
                    val report = Report(
                        fAuth.currentUser?.uid,
                        title,
                        description,
                        null, // No media URL
                        currentDate,
                        "Pending",
                        problem,
                        user
                    )
                    val reportStat = Report(
                        fAuth.currentUser?.uid,
                        title,
                        description,
                        null, // No media URL
                        currentDate1,
                        "Pending",
                        problem,
                        user
                    )
                    db.collection("reportStatistics").document().set(reportStat)

                    reportId?.let { id ->
                        reportsRef.child(id).setValue(report)
                            .addOnSuccessListener {
                                // Report added successfully
                                FancyToast.makeText(this,"Report Submitted",
                                    FancyToast.LENGTH_LONG,
                                    FancyToast.DEFAULT,false).show()
                                onBackPressed()
                            }
                            .addOnFailureListener { e ->
                                // Handle the error
                                FancyToast.makeText(this,"Report Submission Failed",
                                    FancyToast.LENGTH_LONG,
                                    FancyToast.DEFAULT,false).show()
                            }
                            .addOnCompleteListener {
                                submissionInProgress = false
                                progressDialog.dismiss()
                            }
                    }
                }
            }
        }
    }

    private fun checkFields(text: EditText) {
        if (text.text.isEmpty()){
            text.error = "Empty Field"
        }
    }
}

data class Report(
    val uid: String?,
    val title: String,
    val description: String,
    val mediaURL: String?,
    val timestamp: String,
    val status: String,
    val reportType: String,
    val userID: String
)