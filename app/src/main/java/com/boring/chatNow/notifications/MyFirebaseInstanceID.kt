package com.boring.chatNow.notifications

import com.boring.chatNow.pojoClasses.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceID: FirebaseMessagingService()
{
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseInstanceId.getInstance().token

        if (firebaseUser!= null)
        {
            updateToken(refreshToken)
        }
    }

    private fun updateToken(refresh: String?)
    {
        val firebaseUser= FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference().child("Tokens")
        val token = Token(refresh!!)
        ref.child(firebaseUser!!.uid).setValue(token)
    }
}