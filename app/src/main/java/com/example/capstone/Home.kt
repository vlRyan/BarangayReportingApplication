package com.example.capstone

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
import com.example.capstone.Adapters.ReportAdapter
import com.example.capstone.List.Report
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Home : Fragment() {
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var adapter: ReportAdapter
    private lateinit var reportsList: MutableList<Report>
    private lateinit var database: FirebaseDatabase
    private lateinit var reportsRef: DatabaseReference
    private lateinit var childEventListener: ChildEventListener

    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val auth = FirebaseAuth.getInstance()
        val add: FloatingActionButton = view.findViewById(R.id.addReport)
        if (auth.currentUser?.uid == "uFukrYZAXha9ehTt0bwIXrS9tvK2"){
            add.visibility = View.GONE
        }

        add.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))
        add.setOnClickListener {
            val intent = Intent(context, UserReport::class.java)
            startActivity(intent)
        }

        reportsRecyclerView = view.findViewById(R.id.postContainer)
        reportsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reportsList = mutableListOf()
        adapter = ReportAdapter(reportsList)
        reportsRecyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance()
        reportsRef = database.getReference("reports")

        setupDatabaseListener()

        return view
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