package com.boring.chatNow

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boring.chatNow.adapterClasses.ChatsAdapter
import com.boring.chatNow.fragments.API_Service
import com.boring.chatNow.notifications.Clients
import com.boring.chatNow.notifications.Response
import com.boring.chatNow.notifications.Sender
import com.boring.chatNow.pojoClasses.Chats
import com.boring.chatNow.pojoClasses.Data
import com.boring.chatNow.pojoClasses.Token
import com.boring.chatNow.pojoClasses.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import retrofit2.Call
import retrofit2.Callback
import java.util.ArrayList

class ChatActivity : AppCompatActivity() {

    var receiverVisitId: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chats>? = null
    private var seenListener: ValueEventListener? = null
    //private var userListRef: DatabaseReference? = null

    var notify = false
    var apiService: API_Service? = null


    lateinit var rvChats: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar: Toolbar = findViewById(R.id.toolbar_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
//            val intent = Intent(this@ChatActivity, WelcomeActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
            finish()
        }

        apiService = Clients.Client.getClient("https://fcm.googleapis.com/")!!.create(API_Service::class.java)


//        intent = intent
        receiverVisitId = intent.getStringExtra("start_chat")!!
        firebaseUser = FirebaseAuth.getInstance().currentUser

        rvChats = findViewById(R.id.rvChatList)
        rvChats.setHasFixedSize(true)

        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        rvChats.layoutManager = linearLayoutManager

        val userListRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(receiverVisitId)
        userListRef.addValueEventListener( object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user: Users? = snapshot.getValue(Users::class.java)
                username_chat.text = user!!.getUsername()

                Picasso.get().load(user.getProfile()).placeholder(R.drawable.user_male).into(profile_img_chat)
                retrieveMessage(firebaseUser!!.uid, receiverVisitId, user.getProfile())
            }
        })

        send_btn.setOnClickListener{
            notify = true
            val message = text_message.text.toString()

            if (message == "")
            {
                Toast.makeText(this@ChatActivity, "Please, Write Something",
                    Toast.LENGTH_LONG).show()
            }
            else
            {
                sendMessageToUser(firebaseUser!!.uid, receiverVisitId, message)
            }
            text_message.setText("")
        }

        attach_file.setOnClickListener {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }
        seenMessage(receiverVisitId)
    }

    private fun retrieveMessage(senderId: String, receiverId: String?, receiverImageUrl: String?)
    {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener( object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot)
            {
                (mChatList as ArrayList<Chats>).clear()

                for (item in snapshot.children)
                {
                    val chat = item.getValue(Chats::class.java)

                    if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                    {
                        (mChatList as ArrayList<Chats>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(this@ChatActivity, mChatList as ArrayList<Chats>, receiverImageUrl!!)
                    rvChats.adapter = chatsAdapter
                }
            }

        })

    }

    private fun sendMessageToUser(sender: String, receiver: String?, msg: String)
    {
        val reference = FirebaseDatabase.getInstance().reference
        val messageID = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = sender
        messageHashMap["message"] = msg
        messageHashMap["receiver"] = receiver
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageID"] = messageID
        reference.child("Chats").child(messageID!!).setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val chatListRef = FirebaseDatabase.getInstance().reference
                        .child("Chat List")
                        .child(firebaseUser!!.uid)
                        .child(receiverVisitId)

                    chatListRef.addListenerForSingleValueEvent( object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()){
                                chatListRef.child("id").setValue(receiverVisitId)
                            }
                            val chatListReceiverRef = FirebaseDatabase.getInstance().reference
                                .child("Chat List")
                                .child(receiverVisitId)
                                .child(firebaseUser!!.uid)

                            chatListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                    })
                }
            }

        // Implementing Push Notification by Firebase Cloud Messaging
        val userListRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(firebaseUser!!.uid)

        userListRef.addValueEventListener( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                val user = snapshot.getValue(Users::class.java)
                if (notify)
                {
                    sendNotification(receiverVisitId, user!!.getUsername(), msg)
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun sendNotification(receiverId: String, username: String?, message: String)
    {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(receiverId)

        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot)
            {
                for ( item in snapshot.children)
                {
                    val token: Token? = item.getValue(Token::class.java)
                    val data = Data(firebaseUser!!.uid, R.mipmap.ic_launcher,
                    "$username: $message", "New message", receiverVisitId)

                    val sender = Sender(data!!, token!!.getToken().toString())

                    apiService!!.sendNotification(sender).enqueue(object : Callback<Response> {
                        override fun onFailure(call: Call<Response>, t: Throwable) {

                        }

                        override fun onResponse(
                            call: Call<Response>,
                            response: retrofit2.Response<Response>
                        )
                        {
                            if (response.code() == 200)
                            {
                                if (response.body()!!.success !== 1)
                                {
                                    Toast.makeText(applicationContext, "Failed to respond", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null )
        {
            val loadBar = ProgressDialog(this)
            loadBar.setMessage("Wait, Image is sending...")
            loadBar.show()

            val fileUri = data.data
            val storageRef = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageRef.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image!"
                    messageHashMap["receiver"] = receiverVisitId
                    messageHashMap["isSeen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageID"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener{task ->
                            if (task.isSuccessful)
                            {
                                loadBar.dismiss()
                                val userListRef = FirebaseDatabase.getInstance().reference.child("Users")
                                    .child(firebaseUser!!.uid)
                                userListRef.addValueEventListener( object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot)
                                    {
                                        val user = snapshot.getValue(Users::class.java)
                                        if (notify)
                                        {
                                            sendNotification(receiverVisitId, user!!.getUsername(),"sent you an image!")
                                        }
                                        notify = false
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                            }
                        }
                }
            }
        }
    }

    private fun seenMessage(userId: String)
    {
        val refSeen = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = refSeen.addValueEventListener( object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                for (item in snapshot.children)
                {
                    val chat = item.getValue(Chats::class.java)

                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat.getSender().equals(userId))
                    {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isSeen"] = true
                        item.ref.updateChildren(hashMap)

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

//    override fun onPause()
//    {
//        super.onPause()
//        userListRef!!.removeEventListener(seenListener!!)
//    }
}
