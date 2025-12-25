package com.example.lizentuveraproject

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.*
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

class Community_LT : AppCompatActivity() {

    // Firebase instances
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community_lt)

        // Handle Status Bar/Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- 1. SETUP VIEWS ---
        val etPostContent: EditText = findViewById(R.id.LTcommunity_etPostContent)
        val btnPost: Button = findViewById(R.id.LTcommunity_btnPost)
        // This is the container where the list of posts will appear
        val feedContainer: LinearLayout = findViewById(R.id.LTcommunity_feedContainer)

        // --- 2. LOAD EXISTING POSTS (The missing part) ---
        loadCommunityPosts(feedContainer)

        // --- 3. CREATE POST LOGIC ---
        btnPost.setOnClickListener {
            val content = etPostContent.text.toString().trim()
            val currentUser = auth.currentUser

            // Check if text is empty
            if (TextUtils.isEmpty(content)) {
                etPostContent.error = "Please write something..."
                return@setOnClickListener
            }

            if (currentUser != null) {
                // Disable button to prevent double-clicks
                btnPost.isEnabled = false
                btnPost.text = "Posting..."

                // Create a new unique ID for the post
                val newPostRef = db.collection("tbl_posts").document()

                // Prepare data object
                val postMap = hashMapOf(
                    "post_id" to newPostRef.id,
                    "user_id" to currentUser.uid,
                    "content" to content,
                    "image_url" to "", // Empty since we aren't using images
                    "timestamp" to FieldValue.serverTimestamp(),
                    "likes_count" to 0,
                    "comments_count" to 0,
                    "likes_by" to ArrayList<String>() // Initialize empty list for likes logic
                )

                // Save to Firestore
                newPostRef.set(postMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Posted successfully!", Toast.LENGTH_SHORT).show()
                        etPostContent.setText("") // Clear input

                        // Reset button state
                        btnPost.isEnabled = true
                        btnPost.text = "Post"

                        // Reload the feed so the new post appears immediately
                        loadCommunityPosts(feedContainer)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error posting. Try again.", Toast.LENGTH_SHORT).show()
                        btnPost.isEnabled = true
                        btnPost.text = "Post"
                    }
            } else {
                Toast.makeText(this, "You must be logged in to post.", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 4. NAVIGATION LOGIC ---
        setupNavigation()
    }

    // --- HELPER: Load Posts (This logic fetches posts and adds them to the list) ---
    private fun loadCommunityPosts(container: LinearLayout) {
        val currentUserId = auth.currentUser?.uid

        db.collection("tbl_posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                container.removeAllViews() // Clear old posts so we don't duplicate them

                for (document in documents) {
                    val inflater = LayoutInflater.from(this)
                    // We reuse the same card layout 'item_feed_post'
                    val postView = inflater.inflate(R.layout.item_feed_post, container, false)

                    // Find views inside the card template
                    val tvUser: TextView = postView.findViewById(R.id.tvPostUser)
                    val tvContent: TextView = postView.findViewById(R.id.tvPostContent)
                    val tvLikesCount: TextView = postView.findViewById(R.id.tvLikesCount)
                    val imgHeart: ImageView = postView.findViewById(R.id.imgHeart)
                    val tvComments: TextView = postView.findViewById(R.id.tvCommentsCount)

                    // Get data
                    val content = document.getString("content")
                    val comments = document.getLong("comments_count") ?: 0
                    val timestamp = document.getTimestamp("timestamp")
                    val postId = document.id
                    val likedBy = document.get("likes_by") as? ArrayList<String> ?: arrayListOf()

                    // Format Date
                    var dateString = "Just now"
                    if (timestamp != null) {
                        dateString = DateFormat.format("MMM dd, hh:mm a", timestamp.toDate()).toString()
                    }

                    // Set Content
                    tvContent.text = content
                    tvComments.text = "💬 $comments"
                    tvUser.text = "Community Member • $dateString"

                    // --- LIKE LOGIC ---
                    fun updateLikeStatus() {
                        tvLikesCount.text = likedBy.size.toString()
                        if (currentUserId != null && likedBy.contains(currentUserId)) {
                            imgHeart.setImageResource(R.drawable.heartfill)
                        } else {
                            imgHeart.setImageResource(R.drawable.heart)
                        }
                    }
                    updateLikeStatus()

                    imgHeart.setOnClickListener {
                        if (currentUserId == null) {
                            Toast.makeText(this, "Login to like", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (likedBy.contains(currentUserId)) {
                            likedBy.remove(currentUserId)
                        } else {
                            likedBy.add(currentUserId)
                        }
                        updateLikeStatus()

                        db.collection("tbl_posts").document(postId).update(
                            mapOf("likes_by" to likedBy, "likes_count" to likedBy.size)
                        )
                    }

                    // --- COMMENT LOGIC ---
                    tvComments.setOnClickListener {
                        showCommentDialog(postId, tvComments)
                    }

                    container.addView(postView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
    }

    // --- HELPER: Comment Dialog logic ---
    private fun showCommentDialog(postId: String, tvCommentCount: TextView) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(this)
        input.hint = "Write a comment..."
        input.setPadding(50, 40, 50, 40)

        AlertDialog.Builder(this)
            .setTitle("Add Comment")
            .setView(input)
            .setPositiveButton("Post") { _, _ ->
                val commentText = input.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    val newCommentRef = db.collection("tbl_comments").document()
                    val commentMap = hashMapOf(
                        "comment_id" to newCommentRef.id,
                        "post_id" to postId,
                        "user_id" to currentUser.uid,
                        "content" to commentText,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    newCommentRef.set(commentMap).addOnSuccessListener {
                        Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show()

                        // Increment comment count in database
                        db.collection("tbl_posts").document(postId).update("comments_count", FieldValue.increment(1))

                        // Update UI simply
                        val currentText = tvCommentCount.text.toString()
                        try {
                            val parts = currentText.split(" ")
                            if(parts.size >= 2) {
                                val count = parts[1].toLong()
                                tvCommentCount.text = "💬 ${count + 1}"
                            }
                        } catch (e: Exception) {}
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.LTcommunity_navHome).setOnClickListener {
            startActivity(Intent(this, homepage_LT::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.LTcommunity_navDiscover).setOnClickListener {
            startActivity(Intent(this, Discover_LT::class.java))
        }

        findViewById<ImageButton>(R.id.LTcommunity_navCommunity).setOnClickListener {
            // Already here
        }

        findViewById<ImageButton>(R.id.LTcommunity_navProfile).setOnClickListener {
            startActivity(Intent(this, profileLT::class.java))
            finish()
        }
    }
}
