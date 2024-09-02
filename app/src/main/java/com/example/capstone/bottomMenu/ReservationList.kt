package com.example.capstone.bottomMenu

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.List.UserAppointmentData
import com.example.capstone.R
import com.example.capstone.Reservation.UserAppointmentListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ReservationList : Fragment() {
    private lateinit var appointmentList: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserAppointmentListAdapter
    private var appointments = ArrayList<UserAppointmentData>()
    private var firestoreListener: ListenerRegistration? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_reservation_list, container, false)

        val appointBtn = view.findViewById<FloatingActionButton>(R.id.AppointBtn)
        appointBtn.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        appointBtn.setOnClickListener {
            val intent = Intent(context, Reservation::class.java)
            startActivity(intent)
        }

        db = FirebaseFirestore.getInstance()

        appointmentList = view.findViewById(R.id.AppointmentList)
        appointmentList.layoutManager = LinearLayoutManager(context)
        appointmentList.setHasFixedSize(true)
        adapter = UserAppointmentListAdapter(appointments)
        appointmentList.adapter = adapter

        // Fetch user appointments and update the RecyclerView
        fetchUserAppointments()

        return view
    }

    private fun fetchUserAppointments() {
        // Get current user's UID
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userEmail ->
            // Query appointments for the current user
            firestoreListener = db.collection("Appointments")
                .whereEqualTo("user_email", userEmail)
                .orderBy("date", Query.Direction.ASCENDING)
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    // Clear appointments list before updating
                    appointments.clear()

                    // Populate appointments list with data from Firestore
                    value?.documents?.forEach { document ->
                        val data = document.data
                        if (data != null) {
                            val firstName = data["first_name"] as String
                            val lastName = data["last_name"] as String
                            val purpose = data["purpose"] as String
                            val purok = data["purok"] as String
                            val date = data["date"] as String
                            val time = data["time"] as String

                            val userAppointment = UserAppointmentData(
                                firstName,
                                lastName,
                                purpose,
                                purok,
                                date,
                                time
                            )
                            appointments.add(userAppointment)
                        }
                    }

                    // Notify adapter that the data set has changed
                    adapter.notifyDataSetChanged()
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // Start listening for changes in Firestore data
        firestoreListener?.let {
            it.remove() // Remove existing listener to avoid duplicates
        }
        fetchUserAppointments() // Re-register the listener
    }

    override fun onStop() {
        super.onStop()
        // Stop listening for changes in Firestore data
        firestoreListener?.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener when the fragment is destroyed
        firestoreListener?.remove()
    }
}
