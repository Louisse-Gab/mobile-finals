package com.example.lizentuveraproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class landing : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing_lt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Buttons
        val btnLogin: Button = findViewById(R.id.btnLoginLT)
        val btnSignUp: Button = findViewById(R.id.btnSignupLT)

        // 👉 Login button intent
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginLT::class.java)
            startActivity(intent)
        }

        // 👉 Signup button intent
        btnSignUp.setOnClickListener {
            val intent = Intent(this, signupLT::class.java)
            startActivity(intent)
        }
    }
}


