package com.boring.chatNow

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.boring.chatNow.pojoClasses.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_profile.*

class VisitProfileActivity : AppCompatActivity() {
    private var userIdVisit: String? = ""
    var user:Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_profile)

        userIdVisit = intent.getStringExtra("visit_profile")

        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(userIdVisit!!)
        ref.addValueEventListener(object : ValueEventListener
        {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists())
                {
                    user = snapshot.getValue(Users::class.java)
                    username_display.text = user!!.getUsername()
                    Picasso.get().load(user!!.getCover()).into(cover_display)
                    Picasso.get().load(user!!.getProfile()).into(profile_display)
                }
            }

        })

        facebook_display.setOnClickListener {
            val uri = Uri.parse(user!!.getFacebook())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        instagram_display.setOnClickListener {
            val uri = Uri.parse(user!!.getInstagram())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        google_display.setOnClickListener {
            val uri = Uri.parse(user!!.getGoogle())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        send_msg_display.setOnClickListener{
            val intent = Intent(this@VisitProfileActivity, ChatActivity::class.java)
            intent.putExtra("start_chat", user!!.getUID())
            startActivity(intent)
        }

    }
}