package com.example.lizentuveraproject

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
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

    // Variable to store the current user's name to use when commenting
    private var currentUserName: String = "Community Member"

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

        // --- PRE-FETCH USER NAME ---
        val user = auth.currentUser
        if (user != null) {
            db.collection("tbl_users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        currentUserName = document.getString("name") ?: "Community Member"
                    }
                }
        }

        // --- 1. SETUP VIEWS ---
        val etPostContent: EditText = findViewById(R.id.LTcommunity_etPostContent)
        val btnPost: Button = findViewById(R.id.LTcommunity_btnPost)
        val feedContainer: LinearLayout = findViewById(R.id.LTcommunity_feedContainer)


        // --- 2. LOAD EXISTING POSTS ---
        loadCommunityPosts(feedContainer)

        // --- 3. CREATE POST LOGIC ---
        btnPost.setOnClickListener {
            val content = etPostContent.text.toString().trim()
            val currentUser = auth.currentUser

            if (TextUtils.isEmpty(content)) {
                etPostContent.error = "Please write something..."
                return@setOnClickListener
            }

            if (currentUser != null) {
                btnPost.isEnabled = false
                btnPost.text = "Posting..."

                val newPostRef = db.collection("tbl_posts").document()

                val postMap = hashMapOf(
                    "post_id" to newPostRef.id,
                    "user_id" to currentUser.uid,
                    "content" to content,
                    "image_url" to "",
                    "timestamp" to FieldValue.serverTimestamp(),
                    "likes_count" to 0,
                    "comments_count" to 0,
                    "likes_by" to ArrayList<String>()
                )

                newPostRef.set(postMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Posted successfully!", Toast.LENGTH_SHORT).show()
                        etPostContent.setText("")
                        btnPost.isEnabled = true
                        btnPost.text = "Post"
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

        setupNavigation()
    }

    // --- HELPER: Load Posts ---
    private fun loadCommunityPosts(container: LinearLayout) {
        val currentUserId = auth.currentUser?.uid

        db.collection("tbl_posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                container.removeAllViews()

                for (document in documents) {
                    val inflater = LayoutInflater.from(this)
                    val postView = inflater.inflate(R.layout.item_feed_post, container, false)

                    val tvUser: TextView = postView.findViewById(R.id.tvPostUser)
                    val tvContent: TextView = postView.findViewById(R.id.tvPostContent)
                    val tvLikesCount: TextView = postView.findViewById(R.id.tvLikesCount)
                    val imgHeart: ImageView = postView.findViewById(R.id.imgHeart)
                    val tvComments: TextView = postView.findViewById(R.id.tvCommentsCount)

                    val content = document.getString("content")
                    val comments = document.getLong("comments_count") ?: 0
                    val timestamp = document.getTimestamp("timestamp")
                    val postId = document.id
                    val likedBy = document.get("likes_by") as? ArrayList<String> ?: arrayListOf()

                    var dateString = "Just now"
                    if (timestamp != null) {
                        dateString = DateFormat.format("MMM dd, hh:mm a", timestamp.toDate()).toString()
                    }

                    tvContent.text = content
                    tvComments.text = "💬 $comments"
                    tvUser.text = "Community Member • $dateString"

                    // Like Logic
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

                    // Comment Logic
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

    // --- HELPER: Show Comments Dialog ---
    private fun showCommentDialog(postId: String, tvCommentCount: TextView) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Inflate Dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_lt, null)
        val containerComments = dialogView.findViewById<LinearLayout>(R.id.dialog_comments_container)
        val etComment = dialogView.findViewById<EditText>(R.id.dialog_etComment)
        val btnSend = dialogView.findViewById<ImageButton>(R.id.dialog_btnSend)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // 2. Load Comments Function (Updated for Look & Feel)
        fun loadComments() {
            db.collection("tbl_comments")
                .whereEqualTo("post_id", postId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    containerComments.removeAllViews()

                    if (documents.isEmpty) {
                        val emptyTv = TextView(this)
                        emptyTv.text = "No comments yet. Be the first!"
                        emptyTv.setPadding(10, 10, 10, 10)
                        containerComments.addView(emptyTv)
                    }

                    for (doc in documents) {
                        // --- INFLATE THE NEW PRETTY ITEM ---
                        val commentView = LayoutInflater.from(this).inflate(R.layout.item_comment, containerComments, false)

                        // Find Views
                        val tvName = commentView.findViewById<TextView>(R.id.comment_tvUser) // Note: ID depends on item_comment.xml
                        val tvContent = commentView.findViewById<TextView>(R.id.comment_tvContent)

                        // Get Data
                        val content = doc.getString("content")
                        // If user_name was saved, use it. If not (old comments), default to "Member"
                        val name = doc.getString("user_name") ?: "Community Member"

                        // Set Data
                        tvName.text = name
                        tvContent.text = content

                        // Add to list
                        containerComments.addView(commentView)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CommunityLT", "Error loading comments", e)
                    if (e.message!!.contains("index")) {
                        Toast.makeText(this, "Index required! Check Logcat.", Toast.LENGTH_LONG).show()
                    }
                }
        }

        loadComments()

        // 3. Send Button Logic (Now saves user_name too)
        btnSend.setOnClickListener {
            val commentText = etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val newCommentRef = db.collection("tbl_comments").document()

                val commentMap = hashMapOf(
                    "comment_id" to newCommentRef.id,
                    "post_id" to postId,
                    "user_id" to currentUser.uid,
                    "user_name" to currentUserName, // <--- SAVING THE NAME HERE
                    "content" to commentText,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                newCommentRef.set(commentMap).addOnSuccessListener {
                    etComment.setText("")
                    loadComments() // Reload to see your new comment immediately

                    db.collection("tbl_posts").document(postId).update("comments_count", FieldValue.increment(1))

                    // Update UI Counter
                    val currentText = tvCommentCount.text.toString()
                    try {
                        val parts = currentText.split(" ")
                        if (parts.size >= 2) {
                            val count = parts[1].toLong()
                            tvCommentCount.text = "💬 ${count + 1}"
                        }
                    } catch (e: Exception) {}
                }
            }
        }

        dialog.show()
    }

    private fun setupNavigation() {
        // Navigation Code (Same as before)
        findViewById<ImageButton>(R.id.LTcommunity_navHome).setOnClickListener {
            startActivity(Intent(this, homepage_LT::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.LTcommunity_navDiscover).setOnClickListener {
            startActivity(Intent(this, Discover_LT::class.java))
        }
        findViewById<ImageButton>(R.id.LTcommunity_navProfile).setOnClickListener {
            startActivity(Intent(this, profileLT::class.java))
            finish()
        }
    }
}
