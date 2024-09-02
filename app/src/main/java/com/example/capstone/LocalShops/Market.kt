package com.example.capstone.LocalShops

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.List.LocalShopArray
import com.example.capstone.LocalshopAdapter
import com.example.capstone.R
import com.example.capstone.navigation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class Market : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var shoplist : ArrayList<LocalShopArray>
    private lateinit var shopAdapter : LocalshopAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)

        val addShop: FloatingActionButton = findViewById(R.id.addShop)
        addShop.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
        addShop.setOnClickListener {
            val intent = Intent(this, MarketPost::class.java)
            startActivity(intent)
        }

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        backBtn.setOnClickListener {
            // Navigate back to the dashboard
            val intent = Intent(this, navigation::class.java)
            startActivity(intent)
            finish()
        }

        recyclerView = findViewById(R.id.shops)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        shoplist = arrayListOf()
        shopAdapter = LocalshopAdapter(this, shoplist)
        recyclerView.adapter = shopAdapter
        db = FirebaseFirestore.getInstance()

        // Call the initial fetch function
        fetchShops()
        
        db.collection("Shops").whereEqualTo("TypeOfBusiness", "Market")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                shoplist.clear() // Clear the list before adding new shops

                snapshot?.let { nonNullSnapshot ->
                    for (document in nonNullSnapshot.documents) {
                        shoplist.add(document.toObject(LocalShopArray::class.java)!!)
                    }

                    // Update the adapter
                    shopAdapter.notifyDataSetChanged()
                }
            }
    }

    // Fetch the initial shops data
    private fun fetchShops() {
        db.collection("Shops").whereEqualTo("TypeOfBusiness", "Market")
            .get()
            .addOnSuccessListener { result ->
                shoplist.clear() // Clear the list before adding new shops

                for (document in result) {
                    shoplist.add(document.toObject(LocalShopArray::class.java)!!)
                }

                // Update the adapter
                shopAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
            }
    }
}
