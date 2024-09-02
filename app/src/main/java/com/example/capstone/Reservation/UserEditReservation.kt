package com.example.capstone.Reservation

import android.app.DatePickerDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.capstone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UserEditReservation : AppCompatActivity() {
    private lateinit var spinner : Spinner
    private lateinit var purokSpinner: Spinner
    private lateinit var firstName : EditText
    private lateinit var lastName : EditText
    private lateinit var submit : Button
    private lateinit var df: DocumentReference
    private lateinit var fStore: FirebaseFirestore
    private lateinit var currentUserEmail: String
    private lateinit var currentDate: Date
    private lateinit var auth : FirebaseAuth
    private lateinit var appointmentsRef: CollectionReference
    private lateinit var picked : TextView
    private lateinit var picker : FrameLayout
    private lateinit var timeLayout: LinearLayout
    private val INVALID_TIME_ID = -1
    private lateinit var time : String
    private lateinit var pickTime1 : RelativeLayout
    private lateinit var pickTime2 : RelativeLayout
    private lateinit var pickTime3 : RelativeLayout
    private lateinit var pickTime4 : RelativeLayout
    private lateinit var pickTime5 : RelativeLayout
    private lateinit var pickTime6 : RelativeLayout
    private lateinit var pickTime7 : RelativeLayout
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit_reservation)
        fStore = FirebaseFirestore.getInstance()
        currentDate = Calendar.getInstance().time
        auth = FirebaseAuth.getInstance()
        currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
        appointmentsRef = FirebaseFirestore.getInstance().collection("Appointments")


        val appoinmentfirstName = intent.getStringExtra("first_name")
        val appoinmentlastName = intent.getStringExtra("last_name")
        val appoinmentDate = intent.getStringExtra("date")
        val appoinmentPurpose = intent.getStringExtra("purpose")
        val appoinmentPurok = intent.getStringExtra("purok")
        val appoinmentTime = intent.getStringExtra("time")

        firstName = findViewById(R.id.firstName)
        lastName = findViewById(R.id.lastName)

        firstName.setText(appoinmentfirstName)
        lastName.setText(appoinmentlastName)

        val otherFrame: FrameLayout = findViewById(R.id.others)
        val otherEditText: EditText = findViewById(R.id.otherPurpose)

        spinner = findViewById(R.id.purpose_spinner)
        val adapter = ArrayAdapter.createFromResource(this, R.array.purpose, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Check if the selected item is "Others"
                if (position == adapter.count - 1) {
                    // If "Others" is selected, show the EditText
                    otherFrame.visibility = View.VISIBLE
                } else {
                    // If any other item is selected, hide the EditText
                    otherFrame.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // This method is not being called when the Spinner's item is changed,
                // but we should add a body to this method for consistency.
                otherFrame.visibility = View.GONE
            }
        }


        purokSpinner = findViewById(R.id.purok_spinner)
        val purokAdapter = ArrayAdapter.createFromResource(this, R.array.Puroks, android.R.layout.simple_spinner_item)
        purokAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        purokSpinner.adapter = purokAdapter

        firstName = findViewById(R.id.firstName)
        lastName = findViewById(R.id.lastName)
        submit = findViewById(R.id.submit)
        picker = findViewById(R.id.pickDate)
        picked = findViewById(R.id.picked)
        timeLayout = findViewById(R.id.timeLayout)

        val tim1 = findViewById<TextView>(R.id.time1)
        val tim2 = findViewById<TextView>(R.id.time2)
        val tim3 = findViewById<TextView>(R.id.time3)
        val tim4 = findViewById<TextView>(R.id.time4)
        val tim5 = findViewById<TextView>(R.id.time5)
        val tim6 = findViewById<TextView>(R.id.time6)
        val tim7 = findViewById<TextView>(R.id.time7)

        val limitTv1 = findViewById<TextView>(R.id.limitTv1)
        val limitTv2 = findViewById<TextView>(R.id.limitTv2)
        val limitTv3 = findViewById<TextView>(R.id.limitTv3)
        val limitTv4 = findViewById<TextView>(R.id.limitTv4)
        val limitTv5 = findViewById<TextView>(R.id.limitTv5)
        val limitTv6 = findViewById<TextView>(R.id.limitTv6)
        val limitTv7 = findViewById<TextView>(R.id.limitTv7)

        val limit1 = findViewById<TextView>(R.id.limit1)
        val limit2 = findViewById<TextView>(R.id.limit2)
        val limit3 = findViewById<TextView>(R.id.limit3)
        val limit4 = findViewById<TextView>(R.id.limit4)
        val limit5 = findViewById<TextView>(R.id.limit5)
        val limit6 = findViewById<TextView>(R.id.limit6)
        val limit7 = findViewById<TextView>(R.id.limit7)

        pickTime1 = findViewById(R.id.pickTime1)
        pickTime2 = findViewById(R.id.pickTime2)
        pickTime3 = findViewById(R.id.pickTime3)
        pickTime4 = findViewById(R.id.pickTime4)
        pickTime5 = findViewById(R.id.pickTime5)
        pickTime6 = findViewById(R.id.pickTime6)
        pickTime7 = findViewById(R.id.pickTime7)

        val myCalendar = Calendar.getInstance()
        val myFormat = "MMMM dd, yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        val datepicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            // Check if the selected day is Saturday or Sunday
            if (selectedCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                selectedCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            ) {
                FancyToast.makeText(this,"Cannot Select Saturdays or Sundays",
                    FancyToast.LENGTH_LONG,
                    FancyToast.DEFAULT,false).show()
                // You can also reset the DatePickerDialog or take any other appropriate action
                return@OnDateSetListener
            }

            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            picked.text = sdf.format(myCalendar.time)
            timeLayout.visibility = View.VISIBLE
            updateAppointmentLimits(picked.text.toString())
        }

        picker.setOnClickListener{
            DatePickerDialog(
                this,
                datepicker,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                // Set the minimum date to today
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }

        pickTime1.setOnClickListener {
            time = "9:00 AM"
            selectPickTime(time, pickTime1, tim1, limitTv1, limit1)
            notSelectPickTime(pickTime7, tim7, limitTv7, limit7)
            notSelectPickTime(pickTime6, tim6, limitTv6, limit6)
            notSelectPickTime(pickTime5, tim5, limitTv5, limit5)
            notSelectPickTime(pickTime4, tim4, limitTv4, limit4)
            notSelectPickTime(pickTime3, tim3, limitTv3, limit3)
            notSelectPickTime(pickTime2, tim2, limitTv2, limit2)
        }

        pickTime2.setOnClickListener {
            time = "10:00 AM"
            selectPickTime(time, pickTime2, tim2, limitTv2, limit2)
            notSelectPickTime(pickTime7, tim7, limitTv7, limit7)
            notSelectPickTime(pickTime6, tim6, limitTv6, limit6)
            notSelectPickTime(pickTime5, tim5, limitTv5, limit5)
            notSelectPickTime(pickTime4, tim4, limitTv4, limit4)
            notSelectPickTime(pickTime3, tim3, limitTv3, limit3)
            notSelectPickTime(pickTime1, tim1, limitTv1, limit1)
        }

        pickTime3.setOnClickListener {
            time = "11:00 AM"
            selectPickTime(time, pickTime3, tim3, limitTv3, limit3)
            notSelectPickTime(pickTime7, tim7, limitTv7, limit7)
            notSelectPickTime(pickTime6, tim6, limitTv6, limit6)
            notSelectPickTime(pickTime5, tim5, limitTv5, limit5)
            notSelectPickTime(pickTime4, tim4, limitTv4, limit4)
            notSelectPickTime(pickTime2, tim2, limitTv2, limit2)
            notSelectPickTime(pickTime1, tim1, limitTv1, limit1)
        }

        pickTime4.setOnClickListener {
            time = "1:00 PM"
            selectPickTime(time, pickTime4, tim4, limitTv4, limit4)
            notSelectPickTime(pickTime7, tim7, limitTv7, limit7)
            notSelectPickTime(pickTime6, tim6, limitTv6, limit6)
            notSelectPickTime(pickTime5, tim5, limitTv5, limit5)
            notSelectPickTime(pickTime3, tim3, limitTv3, limit3)
            notSelectPickTime(pickTime2, tim2, limitTv2, limit2)
            notSelectPickTime(pickTime1, tim1, limitTv1, limit1)
        }

        pickTime5.setOnClickListener {
            time = "2:00 PM"
            selectPickTime(time, pickTime5, tim5, limitTv5, limit5)
            notSelectPickTime(pickTime7, tim7, limitTv7, limit7)
            notSelectPickTime(pickTime6, tim6, limitTv6, limit6)
            notSelectPickTime(pickTime4, tim4, limitTv4, limit4)
            notSelectPickTime(pickTime3, tim3, limitTv3, limit3)
            notSelectPickTime(pickTime2, tim2, limitTv2, limit2)
            notSelectPickTime(pickTime1, tim1, limitTv1, limit1)
        }

        pickTime6.setOnClickListener {
            time = "3:00 PM"
            selectPickTime(time, pickTime6, tim6, limitTv6, limit6)
            notSelectPickTime(pickTime7, tim7, limitTv7, limit7)
            notSelectPickTime(pickTime5,tim5, limitTv5, limit5)
            notSelectPickTime(pickTime4,tim4, limitTv4, limit4)
            notSelectPickTime(pickTime3,tim3, limitTv3, limit3)
            notSelectPickTime(pickTime2,tim2, limitTv2, limit2)
            notSelectPickTime(pickTime1,tim1, limitTv1, limit1)
        }

        pickTime7.setOnClickListener {
            time = "4:00 PM"
            selectPickTime(time, pickTime7, tim7, limitTv7, limit7)
            notSelectPickTime(pickTime6, tim6, limitTv6, limit6)
            notSelectPickTime(pickTime5, tim5, limitTv5, limit5)
            notSelectPickTime(pickTime4, tim4, limitTv4, limit4)
            notSelectPickTime(pickTime3, tim3, limitTv3, limit3)
            notSelectPickTime(pickTime2, tim2, limitTv2, limit2)
            notSelectPickTime(pickTime1, tim1, limitTv1, limit1)
        }

//        updateAppointmentLimits()

        submit.setOnClickListener {
            val fname = firstName.text.toString()
            val lname = lastName.text.toString()
            var purp = spinner.selectedItem.toString()

            purp = if (purp == "Other Purpose") {
                otherEditText.text.toString()
            } else {
                purp
            }

            // add function to check if user have purok endorsement
            val purok = purokSpinner.selectedItem.toString()
            val bundle: Bundle? = intent.extras
            val t = bundle?.getString("date")
            val p = bundle?.getString("purpose")

            if (firstName.text.isEmpty() or lastName.text.isEmpty()) {
                emptyField(firstName)
                emptyField(lastName)
            } else if (picked.text == "Pick Date") {
                FancyToast.makeText(this,"Select a Date",
                    FancyToast.LENGTH_LONG,
                    FancyToast.DEFAULT,false).show()
            } else {
                progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Updating Appointment...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                fStore.collection("Appointments")
                    .whereEqualTo("user_email", auth.currentUser?.uid)
                    .whereEqualTo("date", t)
                    .whereEqualTo("purpose", p)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val document =
                                querySnapshot.documents[0] // Assuming there's only one document
                            val appointmentId = document.id
                            val appointment = mapOf(
                                "user_email" to auth.currentUser?.uid,
                                "first_name" to fname,
                                "last_name" to lname,
                                "purpose" to purp,
                                "purok" to purok,
                                "date" to sdf.format(myCalendar.time),
                                "time" to time
                            )
                            fStore.collection("Appointments").document(appointmentId)
                                .update(appointment)
                                .addOnSuccessListener {
                                    FancyToast.makeText(
                                        this, "Appointment Updated",
                                        FancyToast.LENGTH_LONG,
                                        FancyToast.DEFAULT, false
                                    ).show()
                                    finish()
                                }
                                .addOnFailureListener { exception ->
                                    FancyToast.makeText(
                                        this, "Failed to Update Appointment",
                                        FancyToast.LENGTH_LONG,
                                        FancyToast.DEFAULT, false
                                    ).show()
                                }
                                .addOnCompleteListener {
                                    progressDialog.dismiss()
                                }
                        } else {
                            FancyToast.makeText(
                                this, "Failed to Update Appointment",
                                FancyToast.LENGTH_LONG,
                                FancyToast.DEFAULT, false
                            ).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        FancyToast.makeText(
                            this, "Failed to Update Appointment",
                            FancyToast.LENGTH_LONG,
                            FancyToast.DEFAULT, false
                        ).show()
                    }
            }
        }
    }

    private fun selectPickTime(selectedTime: String, pickTimeView: RelativeLayout, tim: TextView, limitTv: TextView, limit: TextView) {
        val pickedColor = ContextCompat.getColor(this, R.color.white)
        // Set the text color and background for the current selected pickTime
        time = selectedTime
        tim.setTextColor(pickedColor)
        limitTv.setTextColor(pickedColor)
        limit.setTextColor(pickedColor)
        pickTimeView.setBackgroundResource(R.drawable.background_primary)
    }

    private fun notSelectPickTime( pickTimeView: RelativeLayout, tim: TextView, limitTv: TextView, limit: TextView) {
        val notPickedColor = ContextCompat.getColor(this, R.color.black)
        // Reset the text color and background for the last selected pickTime
        tim.setTextColor(notPickedColor)
        limitTv.setTextColor(notPickedColor)
        limit.setTextColor(notPickedColor)
        pickTimeView.setBackgroundResource(R.drawable.border_primary)
    }

    private fun updateAppointmentLimits(date: String) {
        clearPreviousCounts()

        appointmentsRef.whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val countMap = mutableMapOf<String, Long>()

                for (document in querySnapshot) {
                    // Ensure the "time" field is present in the document
                    if (document.contains("time")) {
                        val time = document.getString("time") ?: ""
                        val count = countMap.getOrDefault(time, 0) + 1 //disregard error, works totally fine
                        countMap[time] = count

                        // Update the count on UI
                        updateLimitText(getLimitIdForTime(time), count)
                    } else {
                    }
                }
            }
            .addOnFailureListener { exception ->
            }
    }

    private fun getCurrentDay(): String {
        return SimpleDateFormat("d", Locale.getDefault()).format(currentDate)
    }

    private fun getLimitIdForTime(time: String): Int {
        try {
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(time)!!

            // Round down the time to the nearest hour
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Format the parsed and rounded time for logging
            val roundedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)

            return when (roundedTime) {
                "09:00 AM" -> R.id.limit1
                "10:00 AM" -> R.id.limit2
                "11:00 AM" -> R.id.limit3
                "01:00 PM" -> R.id.limit4
                "02:00 PM" -> R.id.limit5
                "03:00 PM" -> R.id.limit6
                "04:00 PM" -> R.id.limit7
                else -> {
                    INVALID_TIME_ID
                }
            }
        } catch (e: Exception) {
            return INVALID_TIME_ID
        }
    }

    private fun updateLimitText(limitId: Int, count: Long) {
        val limitTextView = findViewById<TextView>(limitId)

        if (limitTextView != null) {
            limitTextView.text = "$count"

            if (isLimitReached(limitId)) {
                disablePickTime(limitId)
            }
        }
    }

    private fun clearPreviousCounts() {
        // Clear the previous counts on UI
        val allLimits = intArrayOf(R.id.limit1, R.id.limit2, R.id.limit3, R.id.limit4, R.id.limit5, R.id.limit6, R.id.limit7)
        for (limitId in allLimits) {
            updateLimitText(limitId, 0)
        }
    }

    private fun limitReached(){

    }

    private fun emptyField(textField: EditText){
        if (textField.text.isEmpty()){
            textField.error = "Empty Field"
        }
    }

    private fun isLimitReached(limitId: Int): Boolean {
        val limitTextView = findViewById<TextView>(limitId)
        return limitTextView != null && limitTextView.text.toString().toInt() >= 10
    }

    private fun disablePickTime(limitId: Int) {
        when (limitId) {
            R.id.limit1 -> {
                pickTime1.isEnabled = false
                pickTime1.setBackgroundResource(R.drawable.background_grey)
                pickTime1.isVisible = false
            }
            R.id.limit2 -> {
                pickTime2.isEnabled = false
                pickTime2.setBackgroundResource(R.drawable.background_grey)
                pickTime2.isVisible = false
            }
            R.id.limit3 -> {
                pickTime3.isEnabled = false
                pickTime3.setBackgroundResource(R.drawable.background_grey)
                pickTime3.isVisible = false
            }
            R.id.limit4 -> {
                pickTime4.isEnabled = false
                pickTime4.setBackgroundResource(R.drawable.background_grey)
                pickTime4.isVisible = false
            }
            R.id.limit5 -> {
                pickTime5.isEnabled = false
                pickTime5.setBackgroundResource(R.drawable.background_grey)
                pickTime5.isVisible = false
            }
            R.id.limit6 -> {
                pickTime6.isEnabled = false
                pickTime6.setBackgroundResource(R.drawable.background_grey)
                pickTime6.isVisible = false
            }
            R.id.limit7 -> {
                pickTime7.isEnabled = false
                pickTime7.setBackgroundResource(R.drawable.background_grey)
                pickTime7.isVisible = false
            }
        }
    }
}