package com.example.capstone.Adapters

import android.view.LayoutInflater
import android.view.View
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.capstone.List.Report
import com.example.capstone.NotYetAvailable.ReportInformation
import com.example.capstone.R

class ReportAdapter(private val reportsList: List<Report>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mediaImageView: ImageView = itemView.findViewById(R.id.mediaImageView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

        fun bind(report: Report, context: Context) {
            // Bind report data to views
            if (report.mediaURL != null) {
                mediaImageView.visibility = View.VISIBLE
                Glide.with(itemView)
                    .load(report.mediaURL)
                    .placeholder(R.drawable.image_icon) // Placeholder image
                    .error(R.drawable.image_icon) // Error image (if loading fails)
                    .into(mediaImageView)
            } else {
                mediaImageView.visibility = View.VISIBLE
                mediaImageView.setImageResource(R.drawable.report_img_placeholder)
            }

            dateTextView.text = report.timestamp.toString() // Modify this based on your date format
            titleTextView.text = report.title
            descriptionTextView.text = report.description

            itemView.setOnClickListener{
                val intent = Intent(context, ReportInformation::class.java).apply {
                    putExtra("title", report.title)
                    putExtra("date", report.timestamp)
                    putExtra("description", report.description)
                    putExtra("mediaURL", report.mediaURL)
                    putExtra("UserID", report.uid)
                    putExtra("userID", report.userID)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.report_item, parent, false)
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
