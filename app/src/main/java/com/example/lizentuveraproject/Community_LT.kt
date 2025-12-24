package com.example.lizentuveraproject

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Community_LT : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_lt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ----------------------------------
        // NAVIGATION BUTTONS (same style)
        // ----------------------------------
        val LTdiscover_navHome: ImageButton = findViewById(R.id.LTcommunity_navHome)
        val LTdiscover_navDiscover: ImageButton = findViewById(R.id.LTcommunity_navDiscover)
        val LTdiscover_navCommunity: ImageButton = findViewById(R.id.LTcommunity_navCommunity)
        val LTdiscover_navPofile: ImageButton = findViewById(R.id.LTcommunity_navProfile)

        LTdiscover_navHome.setOnClickListener {
            val intent = Intent(this, homepage_LT::class.java)
            startActivity(intent)
        }

        LTdiscover_navDiscover.setOnClickListener {
            val intent = Intent(this, Discover_LT::class.java)
            startActivity(intent)
        }

        LTdiscover_navCommunity.setOnClickListener {
            // Already on Community page
        }

        LTdiscover_navPofile.setOnClickListener {
            val intent = Intent(this, profileLT::class.java)
            startActivity(intent)
        }
    }
}
