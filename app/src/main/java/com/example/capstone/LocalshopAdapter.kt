package com.example.capstone

import android.app.AlertDialog
import android.content.Intent
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.List.LocalShopArray
import com.example.capstone.LocalShops.LocalShopEdit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shashank.sony.fancytoastlib.FancyToast

class LocalshopAdapter(private val context: Context, private val shopList: List<LocalShopArray>) :
    RecyclerView.Adapter<LocalshopAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shop_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = shopList[position]

        // Bind data to views
        holder.shopName.text = shop.ShopName
        holder.shopDes.text = shop.ShopDescription
        holder.contactNumber.text = shop.contactNumber
        holder.contactEmail.text = shop.contactEmail
        holder.purok.text = shop.ShopPurok
        holder.shopLoc.text = shop.ShopLocation

        // Handle click on mapView
        holder.mapView.setOnClickListener {
            // Navigate to MapsActivity with latitude and longitude
            val intent = Intent(holder.itemView.context, MapsActivityViewLocation::class.java)
            intent.putExtra("latitude", shop.latitude)
            intent.putExtra("longitude", shop.longitude)
            holder.itemView.context.startActivity(intent)
        }

        // Handle click on "Show more"
        holder.show.setOnClickListener {
            if (holder.phoneContainer.visibility == View.GONE) {
                // Views are currently hidden, show them
                holder.shopDes.maxLines = Int.MAX_VALUE
                holder.phoneContainer.visibility = View.VISIBLE
                holder.emailContainer.visibility = View.VISIBLE
                holder.locationContainer.visibility = View.VISIBLE
                holder.show.text = "Show less"
                holder.show.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.arrow_drop_up_icon, 0
                )
            } else {
                // Views are currently visible, hide them
                holder.shopDes.maxLines = 2
                holder.phoneContainer.visibility = View.GONE
                holder.emailContainer.visibility = View.GONE
                holder.locationContainer.visibility = View.GONE
                holder.show.text = "Show more"
                holder.show.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.arrow_drop_down_icon, 0
                )
            }
        }

        // Check if the current user can edit/delete the entry
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        if (currentUserEmail == "barangayirisan@gmail.com" || currentUserEmail == shop.userEmail) {
            // Show the "Edit" and "Delete" buttons
            holder.edit.visibility = View.VISIBLE
            holder.delete.visibility = View.VISIBLE

            // Set click listeners for Edit and Delete actions
            holder.edit.setOnClickListener {
               val intent = Intent(holder.itemView.context, LocalShopEdit::class.java)
                intent.putExtra("ShopName", shop.ShopName)
                intent.putExtra("ShopDescription", shop.ShopDescription)
                intent.putExtra("ShopPurok", shop.ShopPurok)
                intent.putExtra("ShopLocation", shop.ShopLocation)
                intent.putExtra("TypeOfBusiness", shop.TypeOfBusiness)
                intent.putExtra("contactEmail", shop.contactEmail)
                intent.putExtra("contactNumber", shop.contactNumber)
                intent.putExtra("userEmail", shop.userEmail)
                intent.putExtra("latitude", shop.latitude)
                intent.putExtra("longitude", shop.longitude)
                holder.itemView.context.startActivity(intent)
            }

            holder.delete.setOnClickListener {
                val fStore = FirebaseFirestore.getInstance()
                AlertDialog.Builder(context)
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure you want to delete this?")
                    .setPositiveButton("Yes") { _, _ ->
                        // User clicked "Yes" button, delete the document
                        fStore.collection("Shops")
                            .whereEqualTo("ShopName", shop.ShopName)
                            .whereEqualTo("ShopDescription", shop.ShopDescription)
                            .get()
                            .addOnSuccessListener { result ->
                                for (document in result) {
                                    document.reference.delete()
                                    FancyToast.makeText(holder.itemView.context,"Shop Deleted",
                                        FancyToast.LENGTH_LONG,
                                        FancyToast.DEFAULT,false).show()
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Handle error
                            }

                    }
                    .setNegativeButton("No") { _, _ ->
                        // User clicked "No" button, do nothing
                    }
                    .show()
            }
        } else {
            // Hide the "Edit" and "Delete" buttons
            holder.edit.visibility = View.GONE
            holder.delete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return shopList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shopName: TextView = itemView.findViewById(R.id.shopName)
        val shopDes: TextView = itemView.findViewById(R.id.shopDes)
        val contactNumber: TextView = itemView.findViewById(R.id.contactNumber)
        val contactEmail: TextView = itemView.findViewById(R.id.contactEmail)
        val purok: TextView = itemView.findViewById(R.id.purok)
        val shopLoc: TextView = itemView.findViewById(R.id.shopLoc)
        val mapView: TextView = itemView.findViewById(R.id.mapView)
        val show: TextView = itemView.findViewById(R.id.Show)
        val edit: TextView = itemView.findViewById(R.id.Edit)
        val delete: TextView = itemView.findViewById(R.id.Delete)
        val locationContainer: RelativeLayout = itemView.findViewById(R.id.locationContainer)
        val phoneContainer: LinearLayout = itemView.findViewById(R.id.phoneContainer)
        val emailContainer: LinearLayout = itemView.findViewById(R.id.emailContainer)
    }
}
