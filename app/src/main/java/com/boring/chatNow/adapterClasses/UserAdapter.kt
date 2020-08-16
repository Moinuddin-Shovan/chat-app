package com.boring.chatNow.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boring.chatNow.ChatActivity
import com.boring.chatNow.R
import com.boring.chatNow.VisitProfileActivity
import com.boring.chatNow.pojoClasses.Chats
import com.boring.chatNow.pojoClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_users_layout.view.*

class UserAdapter( mContext: Context, mUsers: List<Users>, chatCheck: Boolean)
    : RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{
    private val mContext: Context
    private val mUsers: List<Users>
    private var chatCheck: Boolean
    private var lastMsg: String = ""

    init
    {
        this.mContext = mContext
        this.mUsers = mUsers
        this.chatCheck = chatCheck
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.search_users_layout, parent,false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: Users? = mUsers[position]
        holder.userName.text = user!!.getUsername()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.user_male).into(holder.profileImage)

        if(chatCheck)
        {
            retrieveLastMessage(user.getUID(), holder.latestMsg)
        }
        else
        {
            holder.latestMsg.visibility = View.GONE
        }
        if (chatCheck)
        {
            if (user.getStatus() == "online")
            {
                holder.onlineStatus.visibility = View.VISIBLE
                holder.offlineStatus.visibility = View.GONE
            }
            else
            {
                holder.onlineStatus.visibility = View.GONE
                holder.offlineStatus.visibility = View.VISIBLE
            }
        }

        else{
            holder.onlineStatus.visibility = View.GONE
            holder.offlineStatus.visibility = View.GONE
        }

        holder.itemView.setOnClickListener{
            val options = arrayOf<CharSequence>("Send Message", "Visit Profile")
            val builder = AlertDialog.Builder(mContext)

            builder.setTitle("What are you up to?")
            builder.setItems(options, DialogInterface.OnClickListener{ dialog, position ->
                if (position == 0){
                    val intent = Intent(mContext, ChatActivity::class.java)
                    intent.putExtra("start_chat", user.getUID())
                    mContext.startActivity(intent)
                }
                if (position == 1){
                    val intent = Intent(mContext, VisitProfileActivity::class.java)
                    intent.putExtra("visit_profile", user.getUID())
                    mContext.startActivity(intent)
                }
            })
            builder.show()
        }
    }

    class ViewHolder( itemView: View): RecyclerView.ViewHolder(itemView){
        var userName: TextView
        var profileImage: CircleImageView
        var onlineStatus: CircleImageView
        var offlineStatus: CircleImageView
        var latestMsg: TextView

        init {
            userName = itemView.findViewById(R.id.username)
            profileImage = itemView.findViewById(R.id.profile_img_search)
            onlineStatus = itemView.findViewById(R.id.profile_online)
            offlineStatus = itemView.findViewById(R.id.profile_offline)
            latestMsg = itemView.findViewById(R.id.latest_msg)
        }

    }

    private fun retrieveLastMessage(senderUid: String?, latestMsg: TextView)
    {
        lastMsg = "default Msg"

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Users")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (item in snapshot.children)
                {
                    val chat: Chats? = item.getValue(Chats::class.java)

                    if (firebaseUser != null &&  chat!= null)
                    {
                        if (chat.getReceiver() == firebaseUser.uid && chat.getSender() == senderUid
                            || chat.getSender() == firebaseUser.uid && chat.getReceiver() == senderUid)
                        {
                            lastMsg = chat.getMessage()!!
                        }
                    }
                }
                when(lastMsg)
                {
                    "defaultMsg" -> latestMsg.text = "No Message"
                    "sent you an image!" -> latestMsg.text = "Image sent"
                    else -> latestMsg.text = lastMsg
                }
                lastMsg = "defaultMsg"
            }

        })
    }

}