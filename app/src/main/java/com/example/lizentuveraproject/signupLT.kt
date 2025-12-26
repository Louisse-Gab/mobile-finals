package com.example.lizentuveraproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class signupLT : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_lt)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind Views
        val etName = findViewById<TextInputEditText>(R.id.LTetNameSignup)
        val etEmail = findViewById<TextInputEditText>(R.id.LTetEmailSignup)
        val etPassword = findViewById<TextInputEditText>(R.id.LTetPasswordSignup)
        val btnSignup = findViewById<Button>(R.id.LTbtnSignup)
        val tvLogin = findViewById<TextView>(R.id.LTsignup_tvLogin)

        // --- SIGNUP BUTTON LOGIC ---
        btnSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create User in Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid
                    val userMap = hashMapOf(
                        "uid" to userId,
                        "name" to name,
                        "email" to email
                    )

                    // Save extra details in Firestore
                    if (userId != null) {
                        db.collection("tbl_users").document(userId).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account Created! Please Log In.", Toast.LENGTH_SHORT).show()

                                // --- CRITICAL FIX ---
                                // Firebase auto-logins on signup. We must Sign Out explicitly
                                // so the user is forced to enter their credentials on the Login page.
                                auth.signOut()

                                // Redirect to LoginLT
                                val intent = Intent(this, LoginLT::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Signup Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // --- NAVIGATION TO LOGIN ---
        tvLogin.setOnClickListener {
            val intent = Intent(this, LoginLT::class.java)
            startActivity(intent)
            finish()
        }
    }
}
