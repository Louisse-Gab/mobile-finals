package com.example.lizentuveraproject

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // UI elements from activity_dashboard
        val txtName: TextView = findViewById(R.id.txtName)
        val txtEmail: TextView = findViewById(R.id.txtEmail)
        val btnLogout: Button = findViewById(R.id.btnLogout)
        val vlayout: LinearLayout = findViewById(R.id.LinearLayoutLL)

        // Firebase auth and Firestore
        val auth = FirebaseAuth.getInstance()
        val conn = FirebaseFirestore.getInstance()

        // Get current user and show in header
        val currentUser = auth.currentUser
        val currentUserEmail = currentUser!!.email
        val currentUserName = currentUser!!.displayName
        val userid = auth.currentUser!!.uid

        conn.collection("tbl_users").document(userid).get()
            .addOnSuccessListener {

                record ->
                val fs_name = record.getString("name")

                //display
                //if google sign in var(name), if native login var (fs_name)
                txtName.text = currentUserName ?: fs_name
                txtEmail.text = currentUserEmail
        }


        // display
        txtName.text = currentUserName
        txtEmail.text = currentUserEmail

        // Load posts from Firestore
        conn.collection("tbl_posts").get().addOnSuccessListener { records ->
            for (record in records) {
                val inflater = LayoutInflater.from(this)
                val template = inflater.inflate(R.layout.activity_main3, vlayout, false)

                // TextViews from card layout (activity_main3)
                val txtContent: TextView = template.findViewById(R.id.contentLL)
                val txtPostName: TextView = template.findViewById(R.id.nameLL)
                val dateTime: TextView = template.findViewById(R.id.date2LL)
                val numLikes: TextView = template.findViewById(R.id.txtlikes)
                val numComments: TextView = template.findViewById(R.id.txtcomments)
                val imgLikes: ImageView = template.findViewById(R.id.heartLL)



                // Field values from Firestore record
                val content = record.getString("content")
                val postUserName = record.getString("user_id")   // or "name" depending on your field
                val date = record.getTimestamp("timestamp")
                val likes = record.getLong("likes_count") ?: 0
                val comments = record.getLong("comments_count") ?: 0
                val likedby = record.get("likedby") as? ArrayList<String> ?: arrayListOf()
                val id = record.id //primary key (document id)

                imgLikes.setOnClickListener {

                    if(likedby.contains(userid)) {
                        // if the user already liked the post

                        likedby.remove(userid)
                        conn.collection("tbl_posts").document(id)
                            .update(mapOf(
                                "likedby" to likedby,
                                "likes_count" to likedby.size //count the no. of element inside likeby
                            )
                            ).addOnSuccessListener {
                                numLikes.text = likedby.size.toString()
                                imgLikes.setImageResource(R.drawable.heart)
                            }
                    }else{
                        // if the user did not like the post yet
                        likedby.add(userid)
                        conn.collection("tbl_posts").document(id)
                            .update(mapOf(
                                "likedby" to likedby,
                                "likes_count" to likedby.size //count the no. of element inside likeby
                            )
                            ).addOnSuccessListener {
                                numLikes.text = likedby.size.toString()
                                imgLikes.setImageResource(R.drawable.heartfill)

                            }

                    }
                }
                //likedby nooflikes
                //likedby .size .contains .add .remove

                if (date != null) {
                    val dateObj = date.toDate()
                    val dateFormatted = DateFormat.format("MMM dd \n hh:mm", dateObj)
                    dateTime.text = dateFormatted
                }

                // Display field values on the card
                txtContent.text = content
                txtPostName.text = postUserName
                numLikes.text = likes.toString()
                numComments.text = comments.toString()

                // Add the card to the parent LinearLayout
                vlayout.addView(template)
            }
        }

        // Logout button
        btnLogout.setOnClickListener {
            // Firebase sign out
            auth.signOut()

            // Google sign out
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            GoogleSignIn.getClient(this, gso).signOut()

            // Go back to login activity
            val intent = Intent(this, login::class.java)
            startActivity(intent)

            // Finish dashboard activity
            finish()
        }
    }
}
