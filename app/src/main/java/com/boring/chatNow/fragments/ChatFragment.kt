package com.boring.chatNow.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boring.chatNow.ChatActivity

import com.boring.chatNow.R
import com.boring.chatNow.adapterClasses.UserAdapter
import com.boring.chatNow.notifications.MyFirebaseInstanceID
import com.boring.chatNow.pojoClasses.ChatList
import com.boring.chatNow.pojoClasses.Token
import com.boring.chatNow.pojoClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 */
class ChatFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var usersChatList: List<ChatList>? = null
    private var firebaseUser: FirebaseUser? = null

    lateinit var rvChatActivity: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_chat, container, false)
        rvChatActivity = view.findViewById(R.id.rvChatActivity)
        rvChatActivity.setHasFixedSize(true)
        rvChatActivity.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersChatList = ArrayList()
        val refCL = FirebaseDatabase.getInstance().reference.child("Chat List").child(firebaseUser!!.uid)
        refCL.addValueEventListener( object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatList as ArrayList).clear()

                for (item in snapshot.children)
                {
                    val chatList = item.getValue(ChatList::class.java)
                    (usersChatList as ArrayList).add(chatList!!)

                }
                retrieveChatList()
            }

        })

        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val getToken = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(getToken)
    }

    private fun retrieveChatList()
    {
        mUsers = ArrayList()

        val refU = FirebaseDatabase.getInstance().reference.child("Users")
        refU.addValueEventListener( object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for ( item in snapshot.children)
                {
                    val user = item.getValue(Users::class.java)
                    for (eachOne in usersChatList!!)
                    {
                        if (user!!.getUID().equals(eachOne.getId()))
                        {
                            (mUsers as ArrayList).add(user)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>),true)
                rvChatActivity.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}
