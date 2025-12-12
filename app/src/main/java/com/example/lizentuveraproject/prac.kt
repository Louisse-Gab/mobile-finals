package com.example.lizentuveraproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue


class prac : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prac)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val edtpost: EditText = findViewById(R.id.edtpost)
        val btnpost: Button = findViewById(R.id.btnpost)

        // start connection of firebase and app
        val con = FirebaseFirestore.getInstance()

        btnpost.setOnClickListener {
            val userpost = edtpost.text.toString()


            // "field" : value
            val values = mapOf(
                "user_idLL" to "001",
                "contentLL" to userpost,
                "noofLikesLL" to 0,
                "noofcommentsLL" to 0,
                "createdAtLL" to FieldValue.serverTimestamp()
            )

            con.collection("tbl_users").add(values).addOnSuccessListener {
                Toast.makeText(this,"New post created", Toast.LENGTH_SHORT).show()
            }

        }
    }

}