package com.boring.chatNow.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.boring.chatNow.R
import com.boring.chatNow.adapterClasses.UserAdapter
import com.boring.chatNow.pojoClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private var searchUserET: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View =  inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchUserET = view.findViewById(R.id.search_users_box)

        mUsers = ArrayList()
        retrieveAllUsers()


        searchUserET!!.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery(s.toString().toLowerCase(Locale.ROOT))
            }

        })

        return view
    }
    private fun retrieveAllUsers()
    {
        var firebaseUID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot)
            {
                (mUsers as ArrayList<Users>).clear()
                if ( searchUserET!!.text.toString() == "")
                {
                    for (item in snapshot.children)
                    {
                        val user: Users? = item.getValue(Users::class.java)
                        if (!(user!!.getUID()).equals(firebaseUID))
                        {
                            (mUsers as ArrayList<Users>).add(user)
                        }
                    }
                    userAdapter = UserAdapter(context!!, mUsers!!, false)
                    recyclerView!!.adapter = userAdapter
                }

            }
        })
    }
    private fun searchQuery(str: String){
        var firebaseUID = FirebaseAuth.getInstance().currentUser!!.uid
        val searchedUsers = FirebaseDatabase.getInstance().reference.child("Users")
            .orderByChild("search").startAt(str).endAt(str + "\uf8ff")

        searchedUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                for (item in snapshot.children){
                    val user: Users? = item.getValue(Users::class.java)
                    if (!(user!!.getUID()).equals(firebaseUID)){
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!!, false)
                recyclerView!!.adapter = userAdapter
            }
        })
    }

}
