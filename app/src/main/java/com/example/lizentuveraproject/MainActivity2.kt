
package com.example.lizentuveraproject

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import android.text.format.DateFormat
import org.w3c.dom.Text

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val vlayout : LinearLayout = findViewById(R.id.layoutLL)

        //start firebase connection
        val conn = FirebaseFirestore.getInstance()

        conn.collection("tbl_posts").get().addOnSuccessListener {
            records ->

            for (record in records) {
                val inflater = LayoutInflater.from(this)
                val template = inflater.inflate(R.layout.activity_main3, vlayout, false)

                // textview variables coming from cardview
                val txtContent : TextView = template.findViewById(R.id.contentLL)
                val txtName : TextView = template.findViewById(R.id.nameLL)
                val dateTime : TextView = template.findViewById(R.id.date2LL)
                val numLikes : TextView = template.findViewById (R.id.txtlikes)
                val numComments : TextView = template.findViewById(R.id.txtcomments)




                // field values
                val content = record.getString("content")
                val name = record.getString("user_id")
                val date = record.getTimestamp("timestamp")
                val likes = record.getLong("likes_count")
                val comments = record.getLong("comments_count")

                if (date != null) {
                    val date = date.toDate()
                    val dateFormatted = DateFormat.format("MMM dd \n hh:mm", date)
                    dateTime.text = dateFormatted
                }

                //display field values
                txtContent.text = content
                txtName.text = name
                // dateTime.text = getDate.toString()
                numLikes.text = likes.toString()
                numComments.text = comments.toString()

                vlayout.addView(template)

            }
        }
    }
}