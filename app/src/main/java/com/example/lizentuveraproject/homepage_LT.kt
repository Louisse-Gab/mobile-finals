package com.example.lizentuveraproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class homepage_LT : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage_lt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ----------------------------------
        // DECLARE VIEWS (your style)
        // ----------------------------------
        val LThome_tvHello: TextView = findViewById(R.id.LThome_tvHello)

        val LTdiscover_navHome: ImageButton = findViewById(R.id.LTdiscover_navHome)
        val LTdiscover_navDiscover: ImageButton = findViewById(R.id.LTdiscover_navDiscover)
        val LTdiscover_navCommunity: ImageButton = findViewById(R.id.LTdiscover_navCommunity)
        val LTdiscover_navPofile: ImageButton = findViewById(R.id.LTdiscover_navProfile)

        // ----------------------------------
        // FIREBASE CONNECTION (your style)
        // ----------------------------------
        val auth = FirebaseAuth.getInstance()
        val con = FirebaseFirestore.getInstance()

        val user = auth.currentUser

        if (user != null) {
            val userid = user.uid

            con.collection("tbl_users").document(userid).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    LThome_tvHello.text = "Hello, ${name ?: "User"}"
                }
                .addOnFailureListener { e ->
                    LThome_tvHello.text = "Hello!"
                    Toast.makeText(this, "Failed to load name: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            LThome_tvHello.text = "Hello!"
            // Optional: send to login if no user
            // val intent = Intent(this, LoginLT::class.java)
            // startActivity(intent)
            // finish()
        }

        // ----------------------------------
        // NAVIGATION INTENTS
        // ----------------------------------
        LTdiscover_navHome.setOnClickListener {
            // already in homepage_LT, do nothing
        }

        LTdiscover_navDiscover.setOnClickListener {
            val intent = Intent(this, Discover_LT::class.java)
            startActivity(intent)
        }

        LTdiscover_navCommunity.setOnClickListener {
            val intent = Intent(this, Community_LT::class.java)
            startActivity(intent)
        }

        LTdiscover_navPofile.setOnClickListener {
            val intent = Intent(this, profileLT::class.java)
            startActivity(intent)
        }
    }
}
