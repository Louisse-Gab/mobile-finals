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


        // connection with auth
        val auth = FirebaseAuth.getInstance()
        val con = FirebaseFirestore.getInstance()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Web client ID
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
                        val email = user.email
                        val name = user.displayName
                        val values = mapOf(
                            "name" to name,
                            "email" to email
                        )

                        con.collection("tbl_users").document(userid).set(values)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Logged In Successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, homepage_LT::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Google Login Failed", Toast.LENGTH_SHORT).show()
            }
        }


        btnGoogle.setOnClickListener {
            googleClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleClient.signInIntent)
            }
        }

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
