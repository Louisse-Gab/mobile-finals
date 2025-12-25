package com.example.lizentuveraproject

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.ArrayList

class homepage_LT : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_homepage_lt)

        // Handle Status Bar Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- 1. DYNAMIC GREETING LOGIC ---
        val tvHello: TextView = findViewById(R.id.LThome_tvHello)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val user = auth.currentUser

        if (user != null) {
            db.collection("tbl_users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        tvHello.text = "Hello, ${name ?: "User"}"
                    }
                }
                .addOnFailureListener {
                    tvHello.text = "Hello, User"
                }
        } else {
            tvHello.text = "Hello, Guest"
        }

        // --- 2. LOAD FEED ---
        val feedContainer: LinearLayout? = findViewById(R.id.LThome_feedContainer)
        if (feedContainer != null) {
            loadHomePosts(feedContainer)
        } else {
            Toast.makeText(this, "Error: Feed container not found in XML", Toast.LENGTH_LONG).show()
        }

        // --- 3. NAVIGATION BUTTONS ---
        val navHome: ImageButton = findViewById(R.id.LTdiscover_navHome)
        val navDiscover: ImageButton = findViewById(R.id.LTdiscover_navDiscover)
        val navCommunity: ImageButton = findViewById(R.id.LTdiscover_navCommunity)
        val navProfile: ImageButton = findViewById(R.id.LTdiscover_navProfile)

        navHome.setOnClickListener {
            // Reload feed if clicked while on home
            if (feedContainer != null) {
                feedContainer.removeAllViews()
                loadHomePosts(feedContainer)
            }
        }

        navDiscover.setOnClickListener {
            startActivity(Intent(this, Discover_LT::class.java))
        }

        navCommunity.setOnClickListener {
            startActivity(Intent(this, Community_LT::class.java))
        }

        navProfile.setOnClickListener {
            startActivity(Intent(this, profileLT::class.java))
        }
    }

    private fun loadHomePosts(container: LinearLayout) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        db.collection("tbl_posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                container.removeAllViews() // Clear to prevent duplicates

                for (document in documents) {
                    val inflater = LayoutInflater.from(this)
                    // Ensure item_feed_post.xml exists in layout folder
                    val postView = inflater.inflate(R.layout.item_feed_post, container, false)

                    // 1. Find views inside the card template
                    val tvUser: TextView = postView.findViewById(R.id.tvPostUser)
                    val tvContent: TextView = postView.findViewById(R.id.tvPostContent)
                    val tvLikesCount: TextView = postView.findViewById(R.id.tvLikesCount)
                    val imgHeart: ImageView = postView.findViewById(R.id.imgHeart)
                    val tvComments: TextView = postView.findViewById(R.id.tvCommentsCount)

                    // 2. Get data from Firestore document
                    val content = document.getString("content")
                    val comments = document.getLong("comments_count") ?: 0
                    val timestamp = document.getTimestamp("timestamp")
                    val postId = document.id

                    // Get the list of people who liked the post
                    val likedBy = document.get("likes_by") as? ArrayList<String> ?: arrayListOf()

                    // 3. Format Date
                    var dateString = "Just now"
                    if (timestamp != null) {
                        dateString = DateFormat.format("MMM dd, hh:mm a", timestamp.toDate()).toString()
                    }

                    // 4. Set Content
                    tvContent.text = content
                    tvComments.text = "💬 $comments"
                    tvUser.text = "Shared Post • $dateString"

                    // --- 5. LIKE BUTTON LOGIC (ICON SWAPPING) ---

                    fun updateLikeStatus() {
                        tvLikesCount.text = likedBy.size.toString()

                        if (currentUserId != null && likedBy.contains(currentUserId)) {
                            // User HAS liked it -> Use Filled Heart
                            imgHeart.setImageResource(R.drawable.heartfill)
                        } else {
                            // User has NOT liked it -> Use Outline Heart
                            imgHeart.setImageResource(R.drawable.heart)
                        }
                    }

                    // Initial update
                    updateLikeStatus()

                    imgHeart.setOnClickListener {
                        if (currentUserId == null) {
                            Toast.makeText(this, "Please login to like posts", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (likedBy.contains(currentUserId)) {
                            // UNLIKE
                            likedBy.remove(currentUserId)
                        } else {
                            // LIKE
                            likedBy.add(currentUserId)
                        }

                        updateLikeStatus() // Update UI immediately

                        // Save to Firebase
                        db.collection("tbl_posts").document(postId).update(
                            mapOf(
                                "likes_by" to likedBy,
                                "likes_count" to likedBy.size
                            )
                        ).addOnFailureListener {
                            // Revert on failure
                            if (likedBy.contains(currentUserId)) likedBy.remove(currentUserId)
                            else likedBy.add(currentUserId)
                            updateLikeStatus()
                            Toast.makeText(this, "Failed to update like", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // --- 6. COMMENT LOGIC (Click Listener) ---
                    tvComments.setOnClickListener {
                        showCommentDialog(postId, tvComments)
                    }

                    container.addView(postView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load feed", Toast.LENGTH_SHORT).show()
            }
    }

    // --- HELPER: SHOW COMMENT DIALOG ---
    private fun showCommentDialog(postId: String, tvCommentCount: TextView) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show()
            return
        }

        // Create input field for dialog
        val input = EditText(this)
        input.hint = "Write a comment..."
        // Add padding: left, top, right, bottom
        input.setPadding(50, 40, 50, 40)

        AlertDialog.Builder(this)
            .setTitle("Add Comment")
            .setView(input)
            .setPositiveButton("Post") { _, _ ->
                val commentText = input.text.toString().trim()

                if (commentText.isNotEmpty()) {
                    // Create new comment reference
                    val newCommentRef = db.collection("tbl_comments").document()

                    val commentMap = hashMapOf(
                        "comment_id" to newCommentRef.id,
                        "post_id" to postId,
                        "user_id" to currentUser.uid,
                        "content" to commentText,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    // Save comment
                    newCommentRef.set(commentMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show()

                            // Update count in tbl_posts
                            val postRef = db.collection("tbl_posts").document(postId)
                            postRef.update("comments_count", FieldValue.increment(1))
                                .addOnSuccessListener {
                                    // Update UI text immediately
                                    val currentText = tvCommentCount.text.toString() // e.g. "💬 5"
                                    try {
                                        val parts = currentText.split(" ")
                                        if (parts.size >= 2) {
                                            val currentCount = parts[1].toLong()
                                            tvCommentCount.text = "💬 ${currentCount + 1}"
                                        }
                                    } catch (e: Exception) {
                                        // Ignore parsing errors
                                    }
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
