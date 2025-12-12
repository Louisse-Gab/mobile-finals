package com.example.lizentuveraproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
class registration : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // UI Components
        val edtName: EditText = findViewById(R.id.edtNameLL)
        val edtEmail: EditText = findViewById(R.id.edtEmailLL)
        val edtPassword: EditText = findViewById(R.id.psPassLL)
        val btnSU: Button = findViewById(R.id.btnSULL)


        // start connection for both firestore and auth
        val con = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        // button - editText values
        btnSU.setOnClickListener {
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            auth.createUserWithEmailAndPassword(email, password)


                val uid = auth.currentUser!!.uid

                val values = mapOf(
                    "name" to name,
                    "email" to email,
                    "role" to "",
                )

                    //save to tbl_users
                con.collection("tbl_users").document(uid).set(values).addOnSuccessListener {
                    Toast.makeText(this, "Account created", Toast.LENGTH_SHORT)

                    //transfer to login
                    val intent = Intent(this, login::class.java)
                    startActivity(intent)
                }


        }
    }
}