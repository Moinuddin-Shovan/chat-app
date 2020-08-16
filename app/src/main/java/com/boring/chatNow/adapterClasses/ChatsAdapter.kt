package com.boring.chatNow.adapterClasses

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.boring.chatNow.R
import com.boring.chatNow.ViewFullPic
import com.boring.chatNow.pojoClasses.Chats
import com.boring.chatNow.pojoClasses.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(
    mContext: Context,
    mChatList: List<Chats>,
    imageUrl: String): RecyclerView.Adapter<ChatsAdapter.ViewHolder?>()
{
    private val mContext: Context
    private val mChatList: List<Chats>
    private val imageUrl: String

    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mContext = mContext
        this.mChatList = mChatList
        this.imageUrl = imageUrl
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        return if ( viewType == 1)
        {
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.messages_left, parent,false)
            ViewHolder(view)
        }
        else{
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.messages_right, parent,false)
            ViewHolder(view)
        }

    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val chat: Chats = mChatList[position]
        Picasso.get().load(imageUrl).into(holder.profileImg)

        // For Image Portion
        if (chat.getMessage().equals("sent you an image!") && !chat.getUrl().equals(""))
        {
            if (chat.getSender().equals(firebaseUser.uid))
            {
                holder.showMessage!!.visibility = View.GONE
                holder.rightImage!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.rightImage)

                holder.rightImage!!.setOnClickListener{
                    val options = arrayOf<CharSequence>(
                        "View full Image",
                        "Delete Image",
                        "Cancel"
                    )

                    var builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(holder.itemView.context)

                    builder.setTitle("What to do?")

                    builder.setItems( options, DialogInterface.OnClickListener{
                        dialog, which ->
                        if (which == 0)
                        {
                            val intent = Intent(mContext, ViewFullPic::class.java)
                            intent.putExtra("Url", chat.getUrl())
                            mContext.startActivity(intent)

                        }
                        else if (which == 1)
                        {
                            deleteMessage(position, holder)
                        }
                    })
                    builder.show()
                }
            }
            else if (!chat.getSender().equals(firebaseUser.uid))
            {
                holder.showMessage!!.visibility = View.GONE
                holder.leftImage!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.leftImage)

                holder.leftImage!!.setOnClickListener{
                    val options = arrayOf<CharSequence>(
                        "View full Image",
                        "Cancel"
                    )

                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)

                    builder.setTitle("What to do?")

                    builder.setItems( options, DialogInterface.OnClickListener{
                            dialog, which ->
                        if (which == 0)
                        {
                            val intent = Intent(mContext, ViewFullPic::class.java)
                            intent.putExtra("Url", chat.getUrl())
                            mContext.startActivity(intent)

                        }
                    })
                    builder.show()
                }
            }
        }
        // For Text Portion
        else
        {
            holder.showMessage!!.text = chat.getMessage()

            if(firebaseUser.uid == chat.getSender())
            {
                holder.showMessage!!.setOnClickListener{
                    val options = arrayOf<CharSequence>(
                        "Delete Message",
                        "Cancel"
                    )

                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)

                    builder.setTitle("What to do?")

                    builder.setItems( options, DialogInterface.OnClickListener{
                            dialog, which ->
                        if (which == 0)
                        {
                            deleteMessage(position, holder)
                        }
                    })
                    builder.show()
                }
            }

        }
        // For Sent and Seen msgs
        if (position == mChatList.size -1)
        {
            if (chat.getIsSeen())
            {
                holder.seenMessage!!.text = "Seen"
                if (chat.getMessage().equals("sent you an image!") && !chat.getUrl().equals(""))
                {
                    val layoutParameter: RelativeLayout.LayoutParams? = holder.seenMessage!!
                        .layoutParams as RelativeLayout.LayoutParams
                    layoutParameter!!.setMargins(0, 220, 20, 0)
                    holder.seenMessage!!.layoutParams = layoutParameter
                }
            }
            else
            {
                holder.seenMessage!!.text = "Sent"
                if (chat.getMessage().equals("sent you an image!") && !chat.getUrl().equals(""))
                {
                    val layoutParameter: RelativeLayout.LayoutParams? = holder.seenMessage!!
                        .layoutParams as RelativeLayout.LayoutParams
                    layoutParameter!!.setMargins(0, 220, 20, 0)
                    holder.seenMessage!!.layoutParams = layoutParameter
                }
            }
        }
        else{
            holder.seenMessage!!.visibility = View.GONE
        }
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var profileImg: CircleImageView? = null
        var showMessage : TextView? = null
        var leftImage : ImageView? = null
        var seenMessage: TextView? = null
        var rightImage : ImageView? = null

        init {
            profileImg = itemView.findViewById(R.id.profile_img_message)
            showMessage = itemView.findViewById(R.id.show_message)
            leftImage = itemView.findViewById(R.id.left_image)
            seenMessage = itemView.findViewById(R.id.text_seen)
            rightImage = itemView.findViewById(R.id.right_image)
        }
    }

    override fun getItemViewType(position: Int): Int
    {
        return if (mChatList[position].getSender().equals(firebaseUser!!.uid))
        {
            1
        }
        else
        {
            0
        }
    }

    private fun deleteMessage(position: Int, holder: ChatsAdapter.ViewHolder)
    {
        val delRef = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList.get(position).getMessageID()!!).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    Toast.makeText(holder.itemView.context, "Deleted!", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    Toast.makeText(holder.itemView.context, "Failed to Delete!", Toast.LENGTH_SHORT).show()
                }
            }
    }

}