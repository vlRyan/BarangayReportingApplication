package com.example.capstone

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.capstone.List.Report
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.shashank.sony.fancytoastlib.FancyToast

class Statistics : AppCompatActivity() {

    //    private lateinit var currentDate: Date
//    private lateinit var dateDisplay: TextView
    private lateinit var barChart: BarChart
    private lateinit var database: FirebaseFirestore
    private lateinit var reportsList: MutableList<Report>
    private lateinit var start_date: TextView
    private lateinit var end_date: TextView
    private lateinit var view: Button


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

//        dateDisplay = findViewById(R.id.dateDisplay)
        barChart = findViewById(R.id.bar_chart)
        reportsList = mutableListOf()
        start_date = findViewById(R.id.start_id)
        end_date = findViewById(R.id.end_id)
        view = findViewById(R.id.view_butt)

        database = FirebaseFirestore.getInstance()

        val myCalendar = Calendar.getInstance()
        val myFormat = "MMMM dd, yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        val sdatepicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            // Check if the selected day is Saturday or Sunday

            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            start_date.text = sdf.format(myCalendar.time)
        }

        start_date.setOnClickListener {
            DatePickerDialog(
                this,
                sdatepicker,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                show()
            }
        }
        val edatepicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)

            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            end_date.text = sdf.format(myCalendar.time)
        }

        end_date.setOnClickListener {
            DatePickerDialog(
                this,
                edatepicker,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                show()
            }
        }
        view.setOnClickListener {
            if (start_date.text == "Start Date") {
                Toast.makeText(this, "Please Select Valid Start Date", Toast.LENGTH_SHORT).show()
            } else if (end_date.text == "End Date") {
                Toast.makeText(this, "Please Select Valid End Date", Toast.LENGTH_SHORT).show()
            }
            updateGraph()
        }
    }

        private fun updateGraph(){
            val db = database.collection("reportStatistics")
            val startDate = start_date.text.toString()
            val endDate = end_date.text.toString()

            db.whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    var L_P = 0
                    var S_P = 0
                    var I_P = 0
                    var PnO = 0

                    for (document in querySnapshot.toObjects(Report::class.java)) {
                        when (document.reportType){
                            "Land Problem" -> L_P += 1
                            "Sanitation Problem" -> S_P += 1
                            "Infrastructure Problem" -> I_P += 1
                            "Peace and Order" -> PnO += 1
                        }
                    }

                val list: ArrayList<BarEntry> = ArrayList()

                list.add(BarEntry(1f, L_P.toFloat()))
                list.add(BarEntry(2f, S_P.toFloat()))
                list.add(BarEntry(3f, I_P.toFloat()))
                list.add(BarEntry(4f, PnO.toFloat()))

                val barDataSet = BarDataSet(list, "List")

                barDataSet.setColors(ColorTemplate.MATERIAL_COLORS, 255)
                barDataSet.valueTextColor = Color.BLACK

                val legendLabels = arrayOf("Land Problem", "Sanitation Problem", "Infrastructure Problem", "Peace and Order")
                val legendEntries = ArrayList<LegendEntry>()
                for (i in legendLabels.indices) {
                    val legendEntry = LegendEntry(
                        legendLabels[i],
                        Legend.LegendForm.CIRCLE,
                        Float.NaN,
                        Float.NaN,
                        null,
                        ColorTemplate.MATERIAL_COLORS[i]
                    )
                    legendEntries.add(legendEntry)
                }
                val legend = barChart.legend
                legend.setCustom(legendEntries)
                legend.isEnabled = true
                legend.form = Legend.LegendForm.SQUARE
                legend.textSize = 12f
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend.maxSizePercent = 0.8f
                legend.xEntrySpace = 5f
                legend.yEntrySpace = 5f
                legend.isWordWrapEnabled = true


                val barData = BarData(barDataSet)

                barChart.setFitBars(false)

                barChart.data = barData

                barChart.description.text = "Reports Statistics"

                // Set x-axis labels
                val xAxis = barChart.xAxis
                xAxis.isEnabled = false

                barChart.animateY(2000)

            }
            .addOnFailureListener { exception ->
                Log.e("Report Statistics", "Error getting documents: $exception")
            }
    }
//    private fun updateGraph() {
//        val db = database.collection("reportStatistics")
//        val startDate = start_date.text.toString()
//        val endDate = end_date.text.toString()
//
//        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
//        val startDateObject = sdf.parse(startDate)
//        val endDateObject = sdf.parse(endDate)
//
//        val startTimestamp = Timestamp(startDateObject)
//        val endTimestamp = Timestamp(endDateObject)
//
//        val reportTypes = arrayOf(
//            "Land Problem",
//            "Sanitation Problem",
//            "Infrastructure Problem",
//            "Peace and Order"
//        )
//        val list: ArrayList<BarEntry> = ArrayList()
//
//        for (i in reportTypes.indices) {
//            val reportType = reportTypes[i]
//            db.whereEqualTo("reportType", reportType)
//                .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
//                .whereLessThanOrEqualTo("timestamp", endTimestamp)
//                .get()
//                .addOnSuccessListener { querySnapshot ->
//                    val count = querySnapshot.size()
//                    list.add(BarEntry((i + 1).toFloat(), count.toFloat()))
//
//                    if (i == reportTypes.size - 1) {
//                        // All queries have completed, update the graph
//                        val barDataSet = BarDataSet(list, "List")
//                        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS, 255)
//                        barDataSet.valueTextColor = Color.BLACK
//
//                        val barData = BarData(barDataSet)
//                        barChart.data = barData
//                        barChart.description.text = "Reports Statistics"
//
//                        // Set x-axis labels
//                        val xAxis = barChart.xAxis
//                        xAxis.isEnabled = false
//
//                        barChart.animateY(2000)
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("Report Statistics", "Error getting documents: $exception")
//                }
//        }
//    }


    data class Report(
        val uid: String?="",
        val title: String,
        val description: String,
        val mediaURL: String?,
        val timestamp: String,
        val status: String,
        val reportType: String,
        val userID: String
    ) {
        constructor() : this(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        )
    }
}