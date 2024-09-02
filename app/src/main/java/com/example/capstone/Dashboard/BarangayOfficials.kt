package com.example.capstone.Dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.capstone.navigation

class BarangayOfficials : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val officialsCollection = firestore.collection("officials")
    private val storage: FirebaseStorage = Firebase.storage
    private lateinit var storageRef: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barangay_officials)
        auth = Firebase.auth
        storageRef = storage.reference

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener {
            // Navigate back to the dashboard
            val intent = Intent(this, navigation::class.java)
            startActivity(intent)
            finish()
        }

        val currentUser = auth.currentUser
        val isAdmin = currentUser?.email == "barangayirisan@gmail.com"

        // Set up UI components for Punong Barangay
        val editPunongBarangay: TextView = findViewById(R.id.edit_punong_barangay)
        val punongBarangayName: EditText = findViewById(R.id.punong_barangay_name)

        // Set initial visibility based on user type
        if (isAdmin) {
            // Admin can edit names
            editPunongBarangay.visibility = View.VISIBLE
            punongBarangayName.isEnabled = false
        } else {
            // Users can't edit names
            editPunongBarangay.visibility = View.GONE
            punongBarangayName.isEnabled = false
        }

        // Set click listener for editing Punong Barangay name
        editPunongBarangay.setOnClickListener {
            handleEditClick(isAdmin, editPunongBarangay, punongBarangayName, "punong_barangay")
        }

        // Retrieve data from Firestore and set it in the UI for Punong Barangay
        retrieveDataAndSetUI("punong_barangay", punongBarangayName)

        // Set up UI components for Barangay Members
        val memberEditTexts: Array<EditText> = arrayOf(
            findViewById(R.id.barangay_member_name_1),
            findViewById(R.id.barangay_member_name_2),
            findViewById(R.id.barangay_member_name_3),
            findViewById(R.id.barangay_member_name_4),
            findViewById(R.id.barangay_member_name_5),
            findViewById(R.id.barangay_member_name_6),
            findViewById(R.id.barangay_member_name_7)
        )

        val memberEditButtons: Array<TextView> = arrayOf(
            findViewById(R.id.edit_barangay_member_1),
            findViewById(R.id.edit_barangay_member_2),
            findViewById(R.id.edit_barangay_member_3),
            findViewById(R.id.edit_barangay_member_4),
            findViewById(R.id.edit_barangay_member_5),
            findViewById(R.id.edit_barangay_member_6),
            findViewById(R.id.edit_barangay_member_7)
        )

        for (i in 0 until memberEditTexts.size) {
            // Set initial visibility based on user type
            if (isAdmin) {
                // Admin can edit names
                memberEditButtons[i].visibility = View.VISIBLE
                memberEditTexts[i].isEnabled = false
            } else {
                // Users can't edit names
                memberEditButtons[i].visibility = View.GONE
                memberEditTexts[i].isEnabled = false
            }

            // Set click listener for editing Barangay Member names
            val memberDocumentName = "barangay_member_${i + 1}"
            memberEditButtons[i].setOnClickListener {
                handleEditClick(isAdmin, memberEditButtons[i], memberEditTexts[i], memberDocumentName)
            }

            // Retrieve data from Firestore and set it in the UI for Barangay Members
            retrieveDataAndSetUI(memberDocumentName, memberEditTexts[i])
        }

        // Set up UI components for SK Chair
        val editSKChair: TextView = findViewById(R.id.edit_sk_chair)
        val skChairName: EditText = findViewById(R.id.sk_chair_name)

        // Set initial visibility based on user type for SK Chair
        if (isAdmin) {
            // Admin can edit names
            editSKChair.visibility = View.VISIBLE
            skChairName.isEnabled = false
        } else {
            // Users can't edit names
            editSKChair.visibility = View.GONE
            skChairName.isEnabled = false
        }

        // Set click listener for editing SK Chair name
        editSKChair.setOnClickListener {
            handleEditClick(isAdmin, editSKChair, skChairName, "sk_chair")
        }

        // Retrieve data from Firestore and set it in the UI for SK Chair
        retrieveDataAndSetUI("sk_chair", skChairName)

        // Set up UI components for SK Kagawad
        val skKagawadEditTexts: Array<EditText> = arrayOf(
            findViewById(R.id.sk_kagawad_name_1),
            findViewById(R.id.sk_kagawad_name_2),
            findViewById(R.id.sk_kagawad_name_3),
            findViewById(R.id.sk_kagawad_name_4),
            findViewById(R.id.sk_kagawad_name_5),
            findViewById(R.id.sk_kagawad_name_6),
            findViewById(R.id.sk_kagawad_name_7)
        )

        val skKagawadEditButtons: Array<TextView> = arrayOf(
            findViewById(R.id.edit_sk_kagawad_1),
            findViewById(R.id.edit_sk_kagawad_2),
            findViewById(R.id.edit_sk_kagawad_3),
            findViewById(R.id.edit_sk_kagawad_4),
            findViewById(R.id.edit_sk_kagawad_5),
            findViewById(R.id.edit_sk_kagawad_6),
            findViewById(R.id.edit_sk_kagawad_7)
        )

        for (i in 0 until skKagawadEditTexts.size) {
            // Set initial visibility based on user type for SK Kagawad
            if (isAdmin) {
                // Admin can edit names
                skKagawadEditButtons[i].visibility = View.VISIBLE
                skKagawadEditTexts[i].isEnabled = false
            } else {
                // Users can't edit names
                skKagawadEditButtons[i].visibility = View.GONE
                skKagawadEditTexts[i].isEnabled = false
            }

            // Set click listener for editing SK Kagawad names
            val skKagawadDocumentName = "sk_kagawad_${i + 1}"
            skKagawadEditButtons[i].setOnClickListener {
                handleEditClick(isAdmin, skKagawadEditButtons[i], skKagawadEditTexts[i], skKagawadDocumentName)
            }

            // Retrieve data from Firestore and set it in the UI for SK Kagawad
            retrieveDataAndSetUI(skKagawadDocumentName, skKagawadEditTexts[i])
        }

        val uploadPunongBarangayProf: ImageButton = findViewById(R.id.upload_punong_barangay_prof)
        val uploadSkChairProf: ImageButton = findViewById(R.id.upload_sk_chair_prof)

        if (isAdmin) {
            uploadPunongBarangayProf.visibility = View.VISIBLE
            uploadSkChairProf.visibility = View.VISIBLE
        } else {
            uploadPunongBarangayProf.visibility = View.GONE
            uploadSkChairProf.visibility = View.GONE
        }

        val barangayMemberUploadButtons: Array<ImageButton> = arrayOf(
            findViewById(R.id.upload_member_prof_1),
            findViewById(R.id.upload_member_prof_2),
            findViewById(R.id.upload_member_prof_3),
            findViewById(R.id.upload_member_prof_4),
            findViewById(R.id.upload_member_prof_5),
            findViewById(R.id.upload_member_prof_6),
            findViewById(R.id.upload_member_prof_7)
        )

        val skKagawadUploadButtons: Array<ImageButton> = arrayOf(
            findViewById(R.id.upload_sk_member_profile_1),
            findViewById(R.id.upload_sk_member_profile_2),
            findViewById(R.id.upload_sk_member_profile_3),
            findViewById(R.id.upload_sk_member_profile_4),
            findViewById(R.id.upload_sk_member_profile_5),
            findViewById(R.id.upload_sk_member_profile_6),
            findViewById(R.id.upload_sk_member_profile_7)
        )

        if (isAdmin) {
            for (button in barangayMemberUploadButtons) {
                button.visibility = View.VISIBLE
            }
            for (button in skKagawadUploadButtons) {
                button.visibility = View.VISIBLE
            }
        } else {
            for (button in barangayMemberUploadButtons) {
                button.visibility = View.GONE
            }
            for (button in skKagawadUploadButtons) {
                button.visibility = View.GONE
            }
        }

        setUpImageFunctionality("punong_barangay")
        setUpImageFunctionality("barangay_member_1")
        setUpImageFunctionality("barangay_member_2")
        setUpImageFunctionality("barangay_member_3")
        setUpImageFunctionality("barangay_member_4")
        setUpImageFunctionality("barangay_member_5")
        setUpImageFunctionality("barangay_member_6")
        setUpImageFunctionality("barangay_member_7")

        setUpImageFunctionality("sk_chair")
        setUpImageFunctionality("sk_kagawad_1")
        setUpImageFunctionality("sk_kagawad_2")
        setUpImageFunctionality("sk_kagawad_3")
        setUpImageFunctionality("sk_kagawad_4")
        setUpImageFunctionality("sk_kagawad_5")
        setUpImageFunctionality("sk_kagawad_6")
        setUpImageFunctionality("sk_kagawad_7")
    }

    private fun handleEditClick(isAdmin: Boolean, editButton: TextView, editText: EditText, documentName: String) {
        if (isAdmin) {
            // Change UI for admin editing mode
            if (editButton.text == "Edit") {
                editButton.text = "Save"
                editText.isEnabled = true
                editText.requestFocus()
            } else {
                // Save changes to Firestore
                val updatedName = editText.text.toString()
                val memberDocument = officialsCollection.document(documentName)

                // Check if the document exists
                memberDocument.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            // Document exists, update it
                            memberDocument.update("name", updatedName)
                        } else {
                            // Document doesn't exist, create a new one
                            memberDocument.set(mapOf("name" to updatedName))
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle the error
                    }

                // Change UI back to normal
                editButton.text = "Edit"
                editText.isEnabled = false
            }
        }
    }

    private fun retrieveDataAndSetUI(documentName: String, editText: EditText) {
        officialsCollection.document(documentName).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name")
                    editText.setText(name)
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }

    private fun setUpImageFunctionality(documentName: String) {
        val profileImageView = findViewById<ImageView>(getProfileImageViewId(documentName))
        val uploadButton = findViewById<ImageButton>(getUploadButtonId(documentName))

        // Set up click listener for uploading image
        uploadButton.setOnClickListener {
            // Call an image picker or capture image logic here
            // For simplicity, you can use an Intent to pick an image from the gallery
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, getUploadRequestCode(documentName))
        }

        // Retrieve image URL from Firestore and display it
        retrieveImageAndSetUI(documentName, profileImageView)
    }

    private fun getProfileImageViewId(documentName: String): Int {
        return when (documentName) {
            "punong_barangay" -> R.id.punong_barangay_profile
            "barangay_member_1" -> R.id.member_profile_1
            "barangay_member_2" -> R.id.member_profile_2
            "barangay_member_3" -> R.id.member_profile_3
            "barangay_member_4" -> R.id.member_profile_4
            "barangay_member_5" -> R.id.member_profile_5
            "barangay_member_6" -> R.id.member_profile_6
            "barangay_member_7" -> R.id.member_profile_7

            "sk_chair" -> R.id.sk_chair_profile
            "sk_kagawad_1" -> R.id.sk_member_profile_1
            "sk_kagawad_2" -> R.id.sk_member_profile_2
            "sk_kagawad_3" -> R.id.sk_member_profile_3
            "sk_kagawad_4" -> R.id.sk_member_profile_4
            "sk_kagawad_5" -> R.id.sk_member_profile_5
            "sk_kagawad_6" -> R.id.sk_member_profile_6
            "sk_kagawad_7" -> R.id.sk_member_profile_7

            else -> 0 // Handle the default case
        }
    }

    private fun getUploadButtonId(documentName: String): Int {
        return when (documentName) {
            "punong_barangay" -> R.id.upload_punong_barangay_prof
            "barangay_member_1" -> R.id.upload_member_prof_1
            "barangay_member_2" -> R.id.upload_member_prof_2
            "barangay_member_3" -> R.id.upload_member_prof_3
            "barangay_member_4" -> R.id.upload_member_prof_4
            "barangay_member_5" -> R.id.upload_member_prof_5
            "barangay_member_6" -> R.id.upload_member_prof_6
            "barangay_member_7" -> R.id.upload_member_prof_7

            "sk_chair" -> R.id.upload_sk_chair_prof
            "sk_kagawad_1" -> R.id.upload_sk_member_profile_1
            "sk_kagawad_2" -> R.id.upload_sk_member_profile_2
            "sk_kagawad_3" -> R.id.upload_sk_member_profile_3
            "sk_kagawad_4" -> R.id.upload_sk_member_profile_4
            "sk_kagawad_5" -> R.id.upload_sk_member_profile_5
            "sk_kagawad_6" -> R.id.upload_sk_member_profile_6
            "sk_kagawad_7" -> R.id.upload_sk_member_profile_7
            else -> 0 // Handle the default case
        }
    }

    private fun getUploadRequestCode(documentName: String): Int {
        return when (documentName) {
            "punong_barangay" -> 1
            "barangay_member_1" -> 2
            "barangay_member_2" -> 3
            "barangay_member_3" -> 4
            "barangay_member_4" -> 5
            "barangay_member_5" -> 6
            "barangay_member_6" -> 7
            "barangay_member_7" -> 8

            "sk_chair" -> 9
            "sk_kagawad_1" -> 10
            "sk_kagawad_2" -> 11
            "sk_kagawad_3" -> 12
            "sk_kagawad_4" -> 13
            "sk_kagawad_5" -> 14
            "sk_kagawad_6" -> 15
            "sk_kagawad_7" -> 16

            else -> 0 // Handle the default case
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            val selectedImageUri: Uri = data.data!!
            val documentName = getDocumentNameFromRequestCode(requestCode)
            uploadImage(documentName, selectedImageUri)
        }
    }

    private fun uploadImage(documentName: String, imageUri: Uri) {
        val imagesRef = storageRef.child("$documentName/profile_image.jpg")

        imagesRef.putFile(imageUri)
            .addOnCompleteListener { uploadTask ->
                if (uploadTask.isSuccessful) {
                    // Image uploaded successfully, now get the download URL
                    imagesRef.downloadUrl
                        .addOnCompleteListener { uriTask ->
                            if (uriTask.isSuccessful) {
                                val downloadUri = uriTask.result
                                // Save the image URL to Firestore
                                officialsCollection.document(documentName)
                                    .update("profileImageUrl", downloadUri.toString())
                                    .addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            // Update the image view immediately
                                            val profileImageView = findViewById<ImageView>(getProfileImageViewId(documentName))
                                            Glide.with(this)
                                                .load(downloadUri)
                                                .transition(DrawableTransitionOptions.withCrossFade())
                                                .into(profileImageView)
                                        } else {
                                            // Handle the error in updating the Firestore document
                                        }
                                    }
                            } else {
                                // Handle the error in getting the download URL
                            }
                        }
                } else {
                    // Handle the error in uploading the image
                }
            }
    }

    private fun retrieveImageAndSetUI(documentName: String, imageView: ImageView) {
        officialsCollection.document(documentName).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val profileImageUrl = documentSnapshot.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrBlank()) {
                        // Load the profile image using Glide or your preferred image loading library
                        Glide.with(this)
                            .load(profileImageUrl)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageView)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }

    private fun getDocumentNameFromRequestCode(requestCode: Int): String {
        return when (requestCode) {
            1 -> "punong_barangay"
            2 -> "barangay_member_1"
            3 -> "barangay_member_2"
            4 -> "barangay_member_3"
            5 -> "barangay_member_4"
            6 -> "barangay_member_5"
            7 -> "barangay_member_6"
            8 -> "barangay_member_7"

            9 -> "sk_chair"
            10 -> "sk_kagawad_1"
            11 -> "sk_kagawad_2"
            12 -> "sk_kagawad_3"
            13 -> "sk_kagawad_4"
            14 -> "sk_kagawad_5"
            15 -> "sk_kagawad_6"
            16 -> "sk_kagawad_7"

            else -> "" // Handle the default case
        }
    }
}
