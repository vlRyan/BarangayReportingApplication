package com.example.capstone

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.Adapters.calendarAdapter
import com.example.capstone.Dashboard.dashboardEvents
import com.example.capstone.List.Events
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class AdminCalendarView : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsArray: ArrayList<Events>
    private lateinit var calendarAdapter: calendarAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_admin_calendar_view, container, false)

        val calendaradd:FloatingActionButton = view.findViewById(R.id.add)
        calendaradd.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        calendaradd.setOnClickListener {
            val add = Intent(requireContext(), calendarAdd::class.java)
            startActivity(add)
        }

        recyclerView = view.findViewById(R.id.upcomingEvents)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        eventsArray = arrayListOf()
        calendarAdapter = calendarAdapter(eventsArray)

        // Initialize RecyclerView and adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = calendarAdapter

        // Fetch events and update the RecyclerView
        EventChangeListener()

        return view
    }
    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("EventsAnnouncement")
            .orderBy("eventDate", Query.Direction.DESCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        when (dc.type) {
                            DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                // Adding or modifying an event
                                val newEvent = dc.document.toObject(Events::class.java)
                                if (!eventsArray.contains(newEvent)) {
                                    // Check if the event is not already in the list to avoid duplicates
                                    eventsArray.add(0, newEvent) // Add to the beginning of the list
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                // Removing an event
                                val removedEvent = dc.document.toObject(Events::class.java)
                                eventsArray.remove(removedEvent)
                            }
                        }
                    }

                    // Update the adapter
                    val adapter = calendarAdapter(eventsArray)
                    recyclerView.adapter = adapter
                    adapter.onItemClickListener(object : calendarAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val intent = Intent(context, dashboardEvents::class.java)
                            intent.putExtra("title", eventsArray[position].eventTitle)
                            intent.putExtra("description", eventsArray[position].eventDescription)
                            intent.putExtra("date", eventsArray[position].eventDate)
                            intent.putExtra("place", eventsArray[position].eventPlace)
                            intent.putExtra("time", eventsArray[position].eventTime)
                            intent.putExtra("imageUrl", eventsArray[position].imageUrl)
                            startActivity(intent)
                        }
                    })

                    // Notify adapter data has changed
                    adapter.notifyDataSetChanged()
                }
            })
    }
}