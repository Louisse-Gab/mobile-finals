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

        val edtEmail: EditText = findViewById(R.id.LTetEmailLogin)
        val edtPassword: EditText = findViewById(R.id.LTetPasswordLogin)
        val btnLogin: Button = findViewById(R.id.LTbtnLogin)
        val btnGoogle: Button = findViewById(R.id.btnGoogleLT)


        // Start connection with auth
        val auth = FirebaseAuth.getInstance()
        val con = FirebaseFirestore.getInstance()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Web client ID
            .requestEmail()
            .build()

        // Creates Google Sign-In client using the above settings
        val googleClient = GoogleSignIn.getClient(this, gso)

        // --------------------------------------------------------------
        // STEP 2 — Activity Result Launcher (handles result of Google Sign In)
        // --------------------------------------------------------------
        val googleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            // Google returns an intent → convert to task
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                // STEP 3 — Get the Google Account (may throw ApiException)
                val account = task.getResult(ApiException::class.java)

                // STEP 4 — Convert Google account to Firebase credential
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                auth.signInWithCredential(credential).addOnSuccessListener {
                    val user = auth.currentUser
                    if (user != null) {
                        val userid = user.uid
                        val email = user.email
                        val name = user.displayName
                        val values = mapOf(
                            "name" to name,
                            "email" to email
                        )

                        // Using set() to save or update user data
                        con.collection("tbl_users").document(userid).set(values)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Logged In Successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, homepage_LT::class.java)
                                startActivity(intent)
                                finish() // Close login activity so back button doesn't return here
                            }
                    }
                }

            } catch (e: Exception) {
                // If Google sign-in fails
                Toast.makeText(this, "Google Login Failed", Toast.LENGTH_SHORT).show()
            }
        }


        // --------------------------------------------------------------
        // STEP 8 — MODIFIED: Force Sign-out first so account picker appears
        // --------------------------------------------------------------
        btnGoogle.setOnClickListener {
            // This clears the cache of the previous login
            googleClient.signOut().addOnCompleteListener {
                // Once signed out locally, launch the sign-in intent again
                googleLauncher.launch(googleClient.signInIntent)
            }
        }

        // Email/Password Login Logic
        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val pass = edtPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener {
                    Toast.makeText(this, "Log In Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, homepage_LT::class.java)
                    startActivity(intent)
                    finish()
                }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Log In Failed: " + e.message, Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
