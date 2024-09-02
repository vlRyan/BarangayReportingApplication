package com.example.capstone

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.Adapters.calendarAdapter
import com.example.capstone.Dashboard.dashboardEvents
import com.example.capstone.List.Events
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class calendar : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsArray: ArrayList<Events>
    private lateinit var calendarAdapter: calendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        recyclerView = view.findViewById(R.id.upcomingEvents)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        eventsArray = arrayListOf()
        calendarAdapter = calendarAdapter(eventsArray)

        EventChangeListener()

        return view
    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("EventsAnnouncement")
            .orderBy("eventDate", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                for (dc: DocumentChange in value?.documentChanges!!) {
                    val event = dc.document.toObject(Events::class.java)
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if (!eventsArray.contains(event)) {
                                eventsArray.add(event)
                            }
                        }
                        DocumentChange.Type.REMOVED -> eventsArray.remove(event)
                        else -> {}
                    }
                }

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

                calendarAdapter.notifyDataSetChanged()
            }
    }

    override fun onResume() {
        super.onResume()
        EventChangeListener()
    }
}