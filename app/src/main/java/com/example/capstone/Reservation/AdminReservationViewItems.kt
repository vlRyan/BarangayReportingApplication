package com.example.capstone.Reservation

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.List.Appointment
import com.example.capstone.R
import com.example.capstone.bottomMenu.AppointmentsAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.text.SimpleDateFormat

class AdminReservationViewItems : AppCompatActivity() {
    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var appointmentsAdapter: AppointmentsAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_reservation_view_items)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener {
            // Navigate back to the dashboard
            onBackPressed()
        }

        // Retrieve the selected time from the intent
        val selectedTime = intent.getStringExtra("selectedTime")
        val selectedDate = intent.getStringExtra("selectedDate")

        // Initialize RecyclerView and its adapter
        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        appointmentsAdapter = AppointmentsAdapter()

        // Set up RecyclerView
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentsRecyclerView.adapter = appointmentsAdapter

        // Fetch and display appointments for the selected time
        fetchAndDisplayAppointments(selectedTime, selectedDate)
    }

    private fun fetchAndDisplayAppointments(selectedTime: String?, selectedDate: String?) {
        if (selectedTime.isNullOrEmpty()) {
            return
        }

        val appointmentsRef = FirebaseFirestore.getInstance().collection("Appointments")
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.time = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).parse(selectedDate)!!
        val selectedFullTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(selectedCalendar.time)

        // Remove leading zero in the hour part of the selected time
        val selectedTimeWithoutLeadingZero = selectedTime.replace("^0".toRegex(), "")

        // Query appointments for the selected time and date
        appointmentsRef.whereEqualTo("time", selectedTimeWithoutLeadingZero)
            .whereEqualTo("date", SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(selectedCalendar.time))
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Handle the query results and update the UI with appointment details
                val appointmentsList = mutableListOf<Appointment>()

                for (document in querySnapshot) {
                    val id = document.id
                    val firstName = document.getString("first_name") ?: ""
                    val lastName = document.getString("last_name") ?: ""
                    val purpose = document.getString("purpose") ?: ""
                    val date = document.getString("date") ?: ""
                    val time = document.getString("time") ?: ""
                    val purok = document.getString("purok") ?: ""
                    val status = document.getString("status") ?: ""

                    val appointment = Appointment(id, firstName, lastName, purpose, date, time, purok, status)
                    appointmentsList.add(appointment)
                }

                // Update the RecyclerView with the list of appointments
                appointmentsAdapter.setAppointments(appointmentsList)
            }
            .addOnFailureListener { exception ->
            }
    }
}