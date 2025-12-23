package com.example.lizentuveraproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

class signupLT : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_lt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editName: EditText = findViewById(R.id.LTetNameSignup)
        val editEmail: EditText = findViewById(R.id.LTetEmailSignup)
        val editPassword: EditText = findViewById(R.id.LTetPasswordSignup)
        val btnRegister: Button = findViewById(R.id.LTbtnSignup)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.currentUser!!.uid

                    val userData = hashMapOf(
                        "user_id" to uid,
                        "name" to name,
                        "favorite_sports" to emptyList<String>(),
                        "favorite_teams" to emptyList<String>(),
                        "profile_picture" to "" // or a default image URL
                    )

                    db.collection("tbl_users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginLT::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }
}
