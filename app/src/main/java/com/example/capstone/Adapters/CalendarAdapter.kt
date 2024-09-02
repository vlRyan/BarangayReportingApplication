package com.example.capstone.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.capstone.List.Events
import com.example.capstone.R

class calendarAdapter(private val eventsList: ArrayList<Events>) : RecyclerView.Adapter<calendarAdapter.MyViewHolder>() {
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun onItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val Title: TextView = itemView.findViewById(R.id.titleTextView)
        val Description: TextView = itemView.findViewById(R.id.descriptionTextView)
        val Date: TextView = itemView.findViewById(R.id.dateTextView)
        val time: TextView = itemView.findViewById(R.id.time)
        val img: ImageView = itemView.findViewById((R.id.mediaImageView))
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.anouncement_view, parent, false)
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.Title.text = eventsList[position].eventTitle
        holder.Date.text = eventsList[position].eventDate
        holder.Description.text = eventsList[position].eventDescription
        holder.time.text = eventsList[position].eventTime

        // Load the image using Glide
        val events: Events = eventsList[position]
        Glide.with(holder.itemView.context)
            .load(events.imageUrl)
            .placeholder(R.drawable.announcement_placeholder) // Placeholder if the image is not loaded yet
            .error(R.drawable.announcement_placeholder) // Placeholder if there's an error loading the image
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Disable caching
            .into(holder.img)

    }

    override fun getItemCount(): Int = eventsList.size
}