package com.example.lizentuveraproject

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnLogout: Button = findViewById(R.id.btnLogout)
        val tvName: TextView = findViewById(R.id.LTprofile_tvName)

        val navHome: ImageButton = findViewById(R.id.LTdiscover_navHome)
        val navDiscover: ImageButton = findViewById(R.id.LTdiscover_navDiscover)
        val navCommunity: ImageButton = findViewById(R.id.LTdiscover_navCommunity)
        val navProfile: ImageButton = findViewById(R.id.LTdiscover_navProfile)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        if (user != null) {
            db.collection("tbl_users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        tvName.text = name ?: "User"
                    }
                }
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginLT::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        navHome.setOnClickListener {
            startActivity(Intent(this, homepage_LT::class.java))
            finish()
        }

        navDiscover.setOnClickListener {
            startActivity(Intent(this, Discover_LT::class.java))
        }

        navCommunity.setOnClickListener {
            startActivity(Intent(this, Community_LT::class.java))
        }

        navProfile.setOnClickListener {
            // Do nothing, already here
            Toast.makeText(this, "You are on Profile", Toast.LENGTH_SHORT).show()
        }
    }
}
