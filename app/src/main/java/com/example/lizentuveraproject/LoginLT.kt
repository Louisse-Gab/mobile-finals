package com.example.lizentuveraproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.result.contract.ActivityResultContracts

class LoginLT : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_lt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- Bind UI Elements using IDs from activity_login_lt.xml ---
        val edtEmail: EditText = findViewById(R.id.LTlogin_etEmail)
        val edtPassword: EditText = findViewById(R.id.LTlogin_etPassword)
        val btnLogin: Button = findViewById(R.id.LTlogin_btnLogin)
        val btnGoogle: Button = findViewById(R.id.LTlogin_btnGoogle)
        val tvRegister: TextView = findViewById(R.id.LTlogin_tvRegister)

        // Firebase Instances
        val auth = FirebaseAuth.getInstance()
        val con = FirebaseFirestore.getInstance()

        // --- CHECK IF USER ALREADY LOGGED IN ---
        if(auth.currentUser != null){
            startActivity(Intent(this, homepage_LT::class.java))
            finish()
        }

        // --- GOOGLE SIGN IN SETUP ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this, gso)

        val googleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                auth.signInWithCredential(credential).addOnSuccessListener {
                    val user = auth.currentUser
                    if (user != null) {
                        val userid = user.uid
                        val userMap = mapOf(
                            "name" to user.displayName,
                            "email" to user.email
                        )

                        con.collection("tbl_users").document(userid).set(userMap) // Overwrites or creates
                            .addOnSuccessListener {
                                Toast.makeText(this, "Logged In Successfully", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, homepage_LT::class.java))
                                finish()
                            }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Google Login Failed", Toast.LENGTH_SHORT).show()
            }
        }

        // --- BUTTON LISTENERS ---

        // 1. Google Button
        btnGoogle.setOnClickListener {
            googleClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleClient.signInIntent)
            }
        }

        // 2. Login Button
        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val pass = edtPassword.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener {
                    Toast.makeText(this, "Log In Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, homepage_LT::class.java))
                    finish()
                }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Log In Failed: " + e.message, Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Register Link (Navigate to Signup Page)
        tvRegister.setOnClickListener {
            startActivity(Intent(this, signupLT::class.java))
            finish()
        }
    }
}
