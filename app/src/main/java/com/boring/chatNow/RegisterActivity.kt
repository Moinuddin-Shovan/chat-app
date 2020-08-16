package com.boring.chatNow

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {

    private lateinit var mainAuth: FirebaseAuth
    private lateinit var refUser: DatabaseReference
    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mainAuth= FirebaseAuth.getInstance()
        register_btn.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username: String = username_register.text.toString()
        val password: String = password_register.text.toString()
        val email: String = email_register.text.toString()

        if( username == "" || password == "" || email == "" ){
            Toast.makeText(this@RegisterActivity,"Please fill all the values correctly", Toast.LENGTH_SHORT).show()
        }
        else{
            mainAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    firebaseUserID = mainAuth.currentUser!!.uid
                    refUser = FirebaseDatabase.getInstance().reference.child("Users")
                        .child(firebaseUserID)

                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = firebaseUserID
                    userHashMap["username"] = username
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chit-chat-60401.appspot.com/o/profile.jpg?alt=media&token=9c89d4a1-c32f-467a-8787-0ab68049b624"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chit-chat-60401.appspot.com/o/cover.jpg?alt=media&token=b53a0d67-c87a-4435-9bb7-b217b759e9ac"
                    userHashMap["status"] = "offline"
                    userHashMap["search"] = username.toLowerCase(Locale.ROOT)
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["instagram"] = "https://m.instagram.com"
                    userHashMap["google"] = "https://www.google.com"

                    refUser.updateChildren(userHashMap)
                        .addOnCompleteListener{ job ->
                            if (job.isSuccessful)
                            {
                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK. or (Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                    }

                }
                else{
                    Toast.makeText(this@RegisterActivity, "Error: " + task.exception!!.message.toString(),
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
