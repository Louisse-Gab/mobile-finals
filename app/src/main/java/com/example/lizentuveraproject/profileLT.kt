package com.example.lizentuveraproject // Check your package name!

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class profileLT : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_lt)

        // Handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- 1. SETUP VIEWS ---
        val btnLogout: Button = findViewById(R.id.btnLogout) // The new red button
        val tvName: TextView = findViewById(R.id.LTprofile_tvName)

        val navHome: ImageButton = findViewById(R.id.LTdiscover_navHome)
        val navDiscover: ImageButton = findViewById(R.id.LTdiscover_navDiscover)
        val navCommunity: ImageButton = findViewById(R.id.LTdiscover_navCommunity)
        val navProfile: ImageButton = findViewById(R.id.LTdiscover_navProfile) // The nav icon

        // --- 2. FIREBASE SETUP ---
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        if (user != null) {
            // Load user name
            db.collection("tbl_users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        tvName.text = name ?: "User"
                    }
                }
        }

        // --- 3. LOGOUT LOGIC ---
        // Only run this when the RED BUTTON is clicked
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginLT::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // --- 4. NAVIGATION BAR LOGIC ---

        // Go Home
        navHome.setOnClickListener {
            startActivity(Intent(this, homepage_LT::class.java))
            finish() // Optional
        }

        // Go Discover
        navDiscover.setOnClickListener {
            startActivity(Intent(this, Discover_LT::class.java))
        }

        // Go Community
        navCommunity.setOnClickListener {
            startActivity(Intent(this, Community_LT::class.java))
        }

        // Go Profile (WE ARE ALREADY HERE)
        navProfile.setOnClickListener {
            // Do nothing! Or just show a toast.
            // DO NOT PUT LOGOUT CODE HERE
            Toast.makeText(this, "You are on the profile page", Toast.LENGTH_SHORT).show()
        }
    }
}
