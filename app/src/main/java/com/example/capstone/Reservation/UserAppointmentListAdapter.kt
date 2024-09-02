package com.example.capstone.Reservation

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.List.UserAppointmentData
import com.example.capstone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast

class UserAppointmentListAdapter(private val appointmentList: ArrayList<UserAppointmentData>) :
    RecyclerView.Adapter<UserAppointmentListAdapter.UserAppointmentViewHolder>() {
    inner class UserAppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val firstName: TextView = itemView.findViewById(R.id.textFirstName)
        val lastName: TextView = itemView.findViewById(R.id.textLastName)
        val purpose: TextView = itemView.findViewById(R.id.textPurpose)
        val purok: TextView = itemView.findViewById(R.id.textPurok)
        val day: TextView = itemView.findViewById(R.id.textDay)
        val time: TextView = itemView.findViewById(R.id.textTime)
        val edit: TextView = itemView.findViewById(R.id.editButton)
        val delete: TextView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAppointmentViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_appointment_item, parent, false)
        return UserAppointmentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserAppointmentViewHolder, position: Int) {
        val currentItem = appointmentList[position]

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()

        holder.firstName.text = currentItem.firstName
        holder.lastName.text = currentItem.lastName
        holder.purpose.text = currentItem.purpose
        holder.purok.text = currentItem.purok
        holder.day.text = currentItem.date
        holder.time.text = currentItem.time

        holder.edit.setOnClickListener {
            val intent = Intent(holder.itemView.context, UserEditReservation::class.java)
            intent.putExtra("first_name", currentItem.firstName)
            intent.putExtra("last_name", currentItem.lastName)
            intent.putExtra("date", currentItem.date)
            intent.putExtra("purok", currentItem.purok)
            intent.putExtra("purpose", currentItem.purpose)
            intent.putExtra("time", currentItem.time)
            holder.itemView.context.startActivity(intent)
        }

        holder.delete.setOnClickListener {
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure you want to delete this appointment?")

            builder.setPositiveButton("Yes") { dialog, _ ->
                db.collection("Appointments")
                    .whereEqualTo("user_email", uid)
                    .whereEqualTo("first_name", holder.firstName.text)
                    .whereEqualTo("last_name", holder.lastName.text)
                    .whereEqualTo("purok", holder.purok.text)
                    .whereEqualTo("purpose", holder.purpose.text)
                    .whereEqualTo("date", holder.day.text.toString())
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            document.reference.delete()
                            appointmentList.removeAt(position)
                            notifyDataSetChanged()
                            FancyToast.makeText(holder.itemView.context,"Appointment Deleted",
                                FancyToast.LENGTH_LONG,
                                FancyToast.DEFAULT,false).show()
                        }
                        dialog.dismiss()
                    }
                    .addOnFailureListener { exception ->
                        // Handle error
                    }
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }
}
