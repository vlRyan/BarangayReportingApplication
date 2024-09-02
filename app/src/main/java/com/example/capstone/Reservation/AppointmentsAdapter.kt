package com.example.capstone.bottomMenu

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.capstone.List.Appointment
import com.example.capstone.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.shashank.sony.fancytoastlib.FancyToast
import org.w3c.dom.Text

class AppointmentsAdapter : RecyclerView.Adapter<AppointmentsAdapter.AppointmentViewHolder>() {
    private var onItemClickListener: ((Appointment) -> Unit)? = null
    private var appointmentsList: List<Appointment> = emptyList()

    fun setAppointments(appointments: List<Appointment>) {
        appointmentsList = appointments
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Appointment) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointmentsList[position]
        holder.bind(appointment)


        // Handle click events for the success and unsuccess buttons
        holder.successButton.setOnClickListener {
            // Update the appointment status to success
            updateAppointmentStatus(position, "success")
            FancyToast.makeText(holder.itemView.context,"Appointment Successful",
                FancyToast.LENGTH_LONG, FancyToast.DEFAULT, false).show()
        }

        holder.unsuccessButton.setOnClickListener {
            // Update the appointment status to unsuccess
            updateAppointmentStatus(position, "unsuccess")
            FancyToast.makeText(holder.itemView.context,"Appointment Failed",
                FancyToast.LENGTH_LONG, FancyToast.DEFAULT, false).show()
        }
//        val storageReference: StorageReference = FirebaseStorage.getInstance().reference.child("images/${appointment.image}")
    }

    private fun updateAppointmentStatus(position: Int, status: String) {
        val appointment = appointmentsList[position]
        val db = FirebaseFirestore.getInstance()
        appointment.id?.let {
            db.collection("Appointments")
                .document(it)
                .update("status", status)
                .addOnSuccessListener {
                    // Update the appointment status in the local list
                    appointment.status = status
                    // Notify the adapter that the data has changed at the specified position
                    notifyItemChanged(position)
                }
                .addOnFailureListener { e ->
                }
        }
    }

    override fun getItemCount(): Int = appointmentsList.size

    class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private val timeTextView: TextView = itemView.findViewById(R.id.time)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val purposeTv: TextView = itemView.findViewById(R.id.purpose)
        private val purokTv: TextView = itemView.findViewById(R.id.purok)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val successButton: TextView = itemView.findViewById(R.id.successButton)
        val unsuccessButton: TextView = itemView.findViewById(R.id.unsuccessButton)

        fun bind(appointment: Appointment) {
            val fullName = "${appointment.firstName} ${appointment.lastName}"
            nameTextView.text = fullName
            purposeTv.text = appointment.purpose
            dateTextView.text = appointment.purok
            timeTextView.text = appointment.date
            purokTv.text = appointment.time

//            Glide.with(itemView.context)
//                .load(appointment.image)
//                .placeholder(R.drawable.announcement_placeholder) // Placeholder if the image is not loaded yet
//                .error(R.drawable.announcement_placeholder) // Placeholder if there's an error loading the image
//                .diskCacheStrategy(DiskCacheStrategy.ALL) // Disable caching
//                .into(proof)

            if (appointment.status == "success") {
                cardView.setBackgroundColor(itemView.context.resources.getColor(R.color.primary))
            } else if (appointment.status == "unsuccess") {
                cardView.setBackgroundColor(itemView.context.resources.getColor(R.color.reject))
            }
        }
    }
}