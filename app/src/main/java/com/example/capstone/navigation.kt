package com.example.capstone

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.capstone.Adapters.UserAdapter
import com.example.capstone.Dashboard.BarangayOfficials
import com.example.capstone.Dashboard.EmergencyContacts
import com.example.capstone.List.Message
import com.example.capstone.List.Report
import com.example.capstone.List.User
import com.example.capstone.Message_.UserSendMessage
import com.example.capstone.Reservation.adminReservationView
import com.example.capstone.bottomMenu.ReservationList
import com.example.capstone.databinding.ActivityMainBinding
import com.example.capstone.message.Inbox
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast

class navigation : AppCompatActivity(), dashboard.DashboardInteractionListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationIcon: ImageView
    private lateinit var fragmentNameTextView: TextView
    private lateinit var preferences: SharedPreferences
    private lateinit var searchIcon: ImageView
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var userList: ArrayList<User>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)
        preferences = getPreferences(Context.MODE_PRIVATE)
        fragmentNameTextView = binding.root.findViewById(R.id.FragmentName)
        notificationIcon = binding.root.findViewById(R.id.notif)
        searchIcon = binding.root.findViewById(R.id.search)

        val user = FirebaseAuth.getInstance().currentUser
        val bottomNavigationView = binding.bottomNavigationView
        replace(dashboard())

        val navView: NavigationView = findViewById(R.id.nav_view)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchAndDisplayUserData(currentUser.uid)
        }

        // new added for the notif clicked redirection to inbox
        val intent = intent
        if (intent.hasExtra("fragment")) {
            val targetFragment = intent.getStringExtra("fragment")
            if (targetFragment == "inbox") {
                replace(Inbox())
                updateFragmentName("Inbox")
            }
        }
        //end
        // Set the initial fragment name
        updateFragmentName("Home")

        // Check the notification preference and set the icon accordingly
        val isNotificationEnabled = preferences.getBoolean("notification_enabled", true)
        updateNotificationIcon(isNotificationEnabled)

        // Set a click listener for the notification icon
        notificationIcon.setOnClickListener {
            showNotificationDialog()
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        // Inside onCreate method after initializing your views
        val topMenuButton: ImageButton = binding.root.findViewById(R.id.topmenu)

        topMenuButton.setOnClickListener {
            // Open the drawer when the top menu button is clicked
            drawerLayout.openDrawer(GravityCompat.START)
        }

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView.setNavigationItemSelectedListener {
            val currentUser = auth.currentUser
            when (it.itemId) {
                R.id.profile -> {
                    // Handle the profile item click
                    updateFragmentName("Profile")
                    if (currentUser != null && "barangayirisan@gmail.com" == currentUser.email) {
                        val profileFragment = AdminProfile()
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.container, profileFragment)
                        transaction.addToBackStack(null)  // Optional: Add to back stack if you want to navigate back
                        transaction.commit()
                    } else {
                        val profileFragment = Profile()
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.container, profileFragment)
                        transaction.addToBackStack(null)  // Optional: Add to back stack if you want to navigate back
                        transaction.commit()
                    }
                }

                R.id.officials -> {
                    val intent = Intent(this, BarangayOfficials::class.java)
                    startActivity(intent)
                }

                R.id.contacts -> {
                    val intent = Intent(this, EmergencyContacts::class.java)
                    startActivity(intent)
                }

                R.id.switchAccount -> {
                    showSignOutConfirmationDialog()

                }

                R.id.signout -> {
                    showSignOutConfirmationDialog()

                }

                else -> {

                }
            }
            // Close the drawer after handling the item click
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        if (user?.email == LoginPage.ADMIN_EMAIL) {
            // Admin user, load admin layout
            bottomNavigationView.inflateMenu(R.menu.adminmenu)
            makeNotif()
            reportsNotif()
        } else {
            // Regular user, load regular layout
            bottomNavigationView.inflateMenu(R.menu.usermenu)
            userNotif()
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            val currentUser = auth.currentUser
            when (it.itemId) {
                R.id.home -> {
                    replace(dashboard())
                    updateFragmentName("Home")
                }

                R.id.calendar -> {
                    replace(calendar())
                    updateFragmentName("Upcoming Events")
                }

                R.id.admincalendar -> {
                    replace(AdminCalendarView())
                    updateFragmentName("Events")
                }

                R.id.upload -> {
                    replace(Home())
                    updateFragmentName("Recent Reports")
                }

                R.id.navigation_read_report -> {
                    replace(AdminCheck())
                    updateFragmentName("New Reports")
                    badgeClear(R.id.navigation_read_report)
                }

                R.id.userappointments -> {
                    replace(ReservationList())
                    updateFragmentName("My Appointments")
                }

                R.id.appointments -> {
                    replace(adminReservationView())
                    updateFragmentName("Appointments")
                }

                R.id.inbox -> {
                    if (currentUser != null && "barangayirisan@gmail.com" == currentUser.email) {
                        // If admin, replace with AdminMessage fragment
                        replace(Inbox())
                        updateFragmentName("Inbox")
                        badgeClear(R.id.inbox)

                    } else {
                        // If not admin, start UserSendMessage activity
                        val intent = Intent(
                            this,
                            UserSendMessage::class.java
                        )// contextUnresolved reference: context
                        startActivity(intent)
                        badgeClear(R.id.usermessage)
                    }
                }

                R.id.usermessage -> {
                    if (currentUser != null && "barangayirisan@gmail.com" == currentUser.email) {
                        // If admin, replace with AdminMessage fragment
                        replace(Inbox())
                    } else {
                        // If not admin, start UserSendMessage activity
                        val intent = Intent(
                            this,
                            UserSendMessage::class.java
                        )// contextUnresolved reference: context
                        startActivity(intent)
                    }
                    badgeClear(R.id.usermessage)
                }

                else -> {

                }
            }
            true
        }

    }


    private fun showSignOutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sign Out")
        builder.setMessage("Are you sure you want to sign out?")

        builder.setPositiveButton("Yes") { _, _ ->
            // User confirmed sign out
            signOut()
        }

        builder.setNegativeButton("No") { _, _ ->
            // User canceled sign out
        }

        builder.show()
    }

    // Method to handle signout
    private fun signOut() {
        Firebase.auth.signOut()
        val intent = Intent(this, LoginPage::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun fetchAndDisplayUserData(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val userReference = database.reference.child("users").child(uid)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User data found in the database
                    val user = snapshot.getValue(User::class.java)

                    // Update the UI with the user data
                    if (user != null) {
                        user.fName?.let {
                            user.lName?.let { it1 ->
                                user.email?.let { it2 ->
                                    updateHeaderUI(
                                        it,
                                        it1, it2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }

    private fun updateHeaderUI(firstName: String, lastName: String, email: String) {
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navHeader = navView.getHeaderView(0)
        val navHeaderName = navHeader.findViewById<TextView>(R.id.nameDisplay)
        val navHeaderEmail = navHeader.findViewById<TextView>(R.id.gmailDisplay)

        val fullName = "$firstName $lastName"

        navHeaderName.text = fullName
        navHeaderEmail.text = email
    }

    private fun replace(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            R.id.topmenu -> {
                // Open the drawer when the top menu button is clicked
                val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showNotificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Notification Permission")
        builder.setMessage("Do you want to enable notifications?")

        builder.setPositiveButton("Accept") { _, _ ->
            // User accepted, enable notifications
            updateNotificationIcon(true)
            preferences.edit().putBoolean("notification_enabled", true).apply()
        }

        builder.setNegativeButton("Reject") { _, _ ->
            // User rejected, disable notifications
            updateNotificationIcon(false)
            preferences.edit().putBoolean("notification_enabled", false).apply()
        }

        builder.setOnCancelListener {
            // Handle cancel event if needed
        }

        builder.show()
    }

    private fun updateNotificationIcon(isEnabled: Boolean) {
        val iconResId = if (isEnabled) {
            R.drawable.notifications_on_icon
        } else {
            R.drawable.notifications_off_icon
        }

        notificationIcon.setImageResource(iconResId)
    }

    private fun updateFragmentName(name: String) {
        fragmentNameTextView.text = name

        // Check if the current fragment requires a search bar
        val requiresSearchBar = when (name) {
            "Upcoming Events", "Events", "Recent Reports", "New Reports", "Inbox", "My Appointments" -> true
            else -> false
        }

        // Change the visibility of the icons based on the conditions
        if (requiresSearchBar) {
            // Change the icon to a search bar icon
            searchIcon.visibility = View.VISIBLE
            notificationIcon.visibility = View.GONE
        } else {
            // Change the icon to notifications icon if needed
            notificationIcon.visibility = View.VISIBLE
            searchIcon.visibility = View.GONE
        }
    }

    override fun onViewAllClicked() {
        // Handle the "View All" click event here
        updateFragmentName("Recent Reports")
        // Update other UI components as needed
    }

    override fun onBackPressed() {
        // Check if the drawer is open, if so, close it
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Check if the current fragment is "Recent Reports"
            val currentFragmentName = fragmentNameTextView.text.toString()
            if (currentFragmentName == "Recent Reports") {
                // Revert the fragment name to the previous one
                updateFragmentName("Home")
                // Optionally, handle other UI components as needed

                // Call super to handle normal back button behavior for other fragments
                super.onBackPressed()
            } else {
                // If not on "Recent Reports" fragment, show sign-out confirmation dialog
                showSignOutConfirmationDialog()
            }
        }
    }

    private fun reportsNotif(){
        database = FirebaseDatabase.getInstance().reference
        val reports = database.child("reports")
        val reportCount =  arrayListOf<String>()


        reports.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                reportCount.clear()
                for (p0 in snapshot.children){
                    val reportList = p0.getValue(Report::class.java)
                    if (reportList?.status.equals("Pending")){
                        reportCount.add(reportList?.description.toString())
                    }
                    if (reportCount.isEmpty()) {
                        badgeClear(R.id.navigation_read_report)
                    }else {
                    badgeSetup(R.id.navigation_read_report, reportCount.count())
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG", "ValueEventListener cancelled: ${databaseError.message}")
            }

        })


    }

    private fun makeNotif() {
        userList = arrayListOf()
        database = FirebaseDatabase.getInstance().reference
        var chatListChildIds = 0
//        val db = database.child("users")
        val chatList = database.child("ChatList")

        chatList.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                for (p0 in snapshot.children){
                    val message = p0.getValue(Message::class.java)
                    chatListChildIds += 1
                }
                if (chatListChildIds == 0) {
                    badgeClear(R.id.inbox)
                }else {
                    badgeSetup(R.id.inbox, chatListChildIds)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("TAG", "ValueEventListener cancelled: ${databaseError.message}")
            }

        })

    }

    private fun userNotif(){
        val chats =  arrayListOf<String>()
        database = FirebaseDatabase.getInstance().reference
        database.child("chats")
            .child( auth.currentUser?.uid.toString() + "zJWE9QmxXZhMF2bNec5l9XOtNHl1")
            .child("messages")
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    chats.clear()
                    for (p0 in snapshot.children){
                        val message = p0.getValue(Message::class.java)
                        if (auth.currentUser?.uid.toString() == message?.receiverID.toString()) {
                            chats.add(p0.toString())

                        }
                        badgeSetup(R.id.usermessage, chats.size)

                    }

                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TAG", "ValueEventListener cancelled: ${databaseError.message}")
                }

            })
    }

//    private fun sendNotif() {
//        val channelId = "Channel Notif"
//        val builder: NotificationCompat.Builder =
//            NotificationCompat.Builder(applicationContext, channelId)
//        builder.setSmallIcon(R.drawable.logo)
//            .setContentTitle("New Message")
//            .setAutoCancel(true).priority =
//            NotificationCompat.PRIORITY_DEFAULT
//
//        // Updated intent to open Inbox fragment
//        val intent =
//            Intent(applicationContext, navigation::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        intent.putExtra("data", "test Value")
//        intent.putExtra("fragment", "inbox") // Add extra to specify the target fragment
//
//        val pending = PendingIntent.getActivity(
//            applicationContext, 0, intent,
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        builder.setContentIntent(pending)
//
//        val notificationManager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationChannel = NotificationChannel(
//                channelId,
//                "Notification",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            notificationChannel.enableVibration(true)
//            notificationManager.createNotificationChannel(
//                notificationChannel
//            )
//        }
//        notificationManager.notify(0, builder.build())
//    }

    private fun badgeSetup(id: Int, alerts: Int) {
        val badge: BadgeDrawable = binding.bottomNavigationView.getOrCreateBadge(id)
        badge.isVisible = true
        if (alerts != 0) {
            badge.number = alerts
        }
    }

    private fun badgeClear(id: Int) {
        val badgeDrawable: BadgeDrawable? = binding.bottomNavigationView.getBadge(id)
        if (badgeDrawable != null) {
            badgeDrawable.isVisible = false
            badgeDrawable.clearNumber()
        }
    }

}
