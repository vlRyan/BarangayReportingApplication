package com.example.capstone.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.List.Report
import com.example.capstone.R
import com.example.capstone.ReportDetails

class AdminCheckAdapter(private val reportsList: List<Report>) :
    RecyclerView.Adapter<AdminCheckAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(report: Report, context: Context) {
            titleTextView.text = report.title
            dateTextView.text = report.timestamp
            descriptionTextView.text = report.description

            itemView.setOnClickListener {
                val intent = Intent(context, ReportDetails::class.java).apply {
                    putExtra("title", report.title)
                    putExtra("description", report.description)
                    putExtra("mediaURL", report.mediaURL)
                    putExtra("status", report.status)
                    putExtra("timestamp", report.timestamp)
                    putExtra("uid", report.uid)
                    putExtra("userID", report.userID)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.admin_check_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val currentReport = reportsList[position]
        holder.bind(currentReport, holder.itemView.context)
    }

    override fun getItemCount(): Int {
        return reportsList.size
    }
}
