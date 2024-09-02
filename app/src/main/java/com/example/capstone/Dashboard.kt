package com.example.capstone

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.Adapters.DashboardReportAdapter
import com.example.capstone.Adapters.dashboardEventsAdapter
import com.example.capstone.Dashboard.dashboardEvents
import com.example.capstone.List.Events
import com.example.capstone.List.Report
import com.example.capstone.LocalShops.FoodnBev
import com.example.capstone.LocalShops.Market
import com.example.capstone.LocalShops.Services
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Locale

class dashboard : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var eventsArray: ArrayList<Events>
    private lateinit var dashboardAdapter: dashboardEventsAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var childEventListener: ChildEventListener
    private lateinit var reportContainer: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var reportsRef: DatabaseReference
    private lateinit var reportsList: MutableList<Report>
    private lateinit var adapter: DashboardReportAdapter
    lateinit var barChart:BarChart
    private lateinit var viewChart : TextView
    private var currentItemIndex: Int = 0
    private val handler = Handler()
    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (currentItemIndex < eventsArray.size - 1) {
                currentItemIndex++
            } else {
                currentItemIndex = 0
            }
            recyclerView.smoothScrollToPosition(currentItemIndex)
            handler.postDelayed(this, 5000) // Auto-scroll every 5 seconds
        }
    }
    private var dashboardInteractionListener: DashboardInteractionListener? = null

    interface DashboardInteractionListener {
        fun onViewAllClicked()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure that the hosting activity implements the DashboardInteractionListener
        if (context is DashboardInteractionListener) {
            dashboardInteractionListener = context
        } else {
            throw RuntimeException("$context must implement DashboardInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        dashboardInteractionListener = null
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.currentEvents)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.setHasFixedSize(true)
        eventsArray = arrayListOf()
        dashboardAdapter = dashboardEventsAdapter(eventsArray)
        recyclerView.adapter = dashboardAdapter
//        barChart = view.findViewById(R.id.bar_chart)
        viewChart = view.findViewById(R.id.show_data)


        val currentUser = auth.currentUser
        val isAdmin = currentUser?.email == "barangayirisan@gmail.com"

        if (isAdmin){
            viewChart.isVisible = true
            viewChart.setOnClickListener {
                val intent = Intent(context, Statistics::class.java)
                startActivity(intent)
            }

        }else {
            viewChart.isGone = true
        }

        val viewall:TextView = view.findViewById(R.id.viewall)
        viewall.setOnClickListener {
            val homeFragment = Home()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.container, homeFragment)
            transaction.addToBackStack(null)  // If you want to add to back stack, so the user can navigate back
            transaction.commit()
            dashboardInteractionListener?.onViewAllClicked()
        }



        val FnB: LinearLayout = view.findViewById(R.id.food)
        FnB.setOnClickListener {
            val intent = Intent(context, FoodnBev::class.java)
            startActivity(intent)
        }
        val services: LinearLayout = view.findViewById(R.id.services)
        services.setOnClickListener {
            val intent = Intent(context, Services::class.java)
            startActivity(intent)
        }
        val stores: LinearLayout = view.findViewById(R.id.store)
        stores.setOnClickListener {
            val intent = Intent(context, Market::class.java)
            startActivity(intent)
        }

//        val list: ArrayList<BarEntry> = ArrayList()
//
//        list.add(BarEntry(100f,100f))
//        list.add(BarEntry(101f,200f))
//        list.add(BarEntry(102f,300f))
//        list.add(BarEntry(103f,400f))
//        list.add(BarEntry(104f,500f))
//
//
//        val barDataSet= BarDataSet(list,"List")
//
//        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)
//        barDataSet.valueTextColor=Color.BLACK
//
//        val barData= BarData(barDataSet)
//
//        barChart.setFitBars(true)
//
//        barChart.data= barData
//
//        barChart.description.text= "Bar Chart"
//
//        barChart.animateY(2000)

        EventChangeListener()

        val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -5)
        val tomorrow = format.format(cal.time)
        db = FirebaseFirestore.getInstance()
        db.collection("EventsAnnouncement").whereEqualTo("eventDate", tomorrow)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.delete()
                }
            }

        reportsRecyclerView = view.findViewById(R.id.reportContainer)
        reportsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        reportsList = mutableListOf()
        adapter = DashboardReportAdapter(reportsList)
        reportsRecyclerView.adapter = adapter
        database = FirebaseDatabase.getInstance()
        reportsRef = database.getReference("reports")

        setupDatabaseListener()

        // Calculate the date three days ago
        val calndr = Calendar.getInstance()
        calndr.add(Calendar.DATE, -3)
        val threeDaysAgo = format.format(calndr.time)
        // calculate one month ago then delete
        val reportDate = Calendar.getInstance()
        reportDate.add(Calendar.MONTH, -1)
        val oneMonthAgo = format.format(calndr.time)

        // Announcement auto slide
        handler.postDelayed(autoScrollRunnable, 5000)

        return view
    }

    private fun EventChangeListener() {
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        db = FirebaseFirestore.getInstance()

        db.collection("EventsAnnouncement")
            .whereEqualTo("eventDate", currentDate)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        return
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

                    val adapter = dashboardEventsAdapter(eventsArray)
                    recyclerView.adapter = adapter
                    adapter.onItemClickListener(object :
                        dashboardEventsAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val intent = Intent(context, dashboardEvents::class.java)
                            intent.putExtra("title", eventsArray[position].eventTitle)
                            intent.putExtra("date", eventsArray[position].eventDate)
                            intent.putExtra("description", eventsArray[position].eventDescription)
                            intent.putExtra("place", eventsArray[position].eventPlace)
                            intent.putExtra("time", eventsArray[position].eventTime)
                            intent.putExtra("imageUrl", eventsArray[position].imageUrl)
                            startActivity(intent)
                        }
                    })

                    dashboardAdapter.notifyDataSetChanged()

                    // Check if eventsArray is empty and set the elevation accordingly
                    if (eventsArray.isEmpty()) {
                        recyclerView.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            })
    }

    override fun onResume() {
        super.onResume()
        // Refresh the report container when the fragment is resumed
        EventChangeListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Stop the auto-scrolling when the fragment is destroyed
        handler.removeCallbacks(autoScrollRunnable)
    }

    private fun setupDatabaseListener() {
        childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val report = snapshot.getValue(Report::class.java)
                report?.let {
                    // Insert the new report at the appropriate position based on its timestamp
                    val insertionIndex = reportsList.binarySearch { report.timestamp.compareTo(it.timestamp) }
                    val index = if (insertionIndex < 0) -insertionIndex - 1 else insertionIndex
                    reportsList.add(index, it)
                    adapter.notifyItemInserted(index)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle if a report is changed (optional)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val report = snapshot.getValue(Report::class.java)
                report?.let {
                    val index = reportsList.indexOfFirst { it.timestamp == report.timestamp }
                    if (index != -1) {
                        reportsList.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle if a report is moved (optional)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        reportsRef.orderByChild("status").equalTo("Accepted").addChildEventListener(childEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        reportsRef.removeEventListener(childEventListener)
    }
}