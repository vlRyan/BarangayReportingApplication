package com.example.capstone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.Adapters.AdminCheckAdapter
import com.example.capstone.List.Report
import com.google.firebase.database.*

class AdminCheck : Fragment() {
    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var adapter: AdminCheckAdapter
    private lateinit var reportsList: MutableList<Report>
    private lateinit var database: FirebaseDatabase
    private lateinit var reportsRef: DatabaseReference
    private lateinit var childEventListener: ChildEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_check, container, false)

        reportsRecyclerView = view.findViewById(R.id.reportsRecyclerView)
        reportsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reportsList = mutableListOf()
        adapter = AdminCheckAdapter(reportsList)
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
                    reportsList.add(it)
                    adapter.notifyItemInserted(reportsList.size - 1)
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

        reportsRef.orderByChild("status").equalTo("Pending").addChildEventListener(childEventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        reportsRef.removeEventListener(childEventListener)
    }
}
