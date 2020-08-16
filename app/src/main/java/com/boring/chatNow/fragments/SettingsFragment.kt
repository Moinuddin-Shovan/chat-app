package com.boring.chatNow.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.marginLeft

import com.boring.chatNow.R
import com.boring.chatNow.pojoClasses.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import java.lang.StringBuilder

/*A simple [Fragment] subclass.*/
class SettingsFragment : Fragment() {

    private var userReference: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private val reqCode = 438
    private var imageUri: Uri? = null
    private var storeRef: StorageReference? = null
    private var coverCheck: String? = null
    private var socialCheck: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_settings, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storeRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user:Users? = snapshot.getValue(Users::class.java)

                    if (context!= null) {
                        view.username_settings.text = user!!.getUsername()
                        Picasso.get().load(user.getProfile()).into(view.profile_setting)
                        Picasso.get().load(user.getCover()).into(view.cover_setting)
                    }
                }
            }

        })
        view.profile_setting.setOnClickListener{
            pickImage()
        }
        view.cover_setting.setOnClickListener{
            coverCheck = "cover"
            pickImage()
        }
        view.facebook.setOnClickListener{
            socialCheck = "facebook"
            setSocialLink()
        }
        view.instagram.setOnClickListener{
            socialCheck = "instagram"
            setSocialLink()
        }
        view.google.setOnClickListener{
            socialCheck = "google"
            setSocialLink()
        }


        return view
    }

    private fun setSocialLink() {
        val builder : AlertDialog.Builder = AlertDialog.Builder(context,
            R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        if (socialCheck == "google"){
            builder.setTitle("Write Url: ")
        }
        else{
            builder.setTitle("Write Username: ")
        }
        val editText = EditText(context)
        editText.maxLines = 1
        if (socialCheck == "google"){
            editText.hint = " e.g. www.youtube.com "
        }
        else{
            builder.setTitle("Write Username: ")
        }
        builder.setView(editText)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener{ dialog, which ->
            val str = editText.text.toString()
            if (str == "")
            {
                Toast.makeText(context, "Please Input something...", Toast.LENGTH_SHORT).show()
            }
            else{
                saveSocialLinks(str)
            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()

    }

    private fun saveSocialLinks(str: String) {
        val mapSocial = HashMap<String, Any>()

        when(socialCheck)
        {
            "facebook" ->
            {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" ->
            {
                mapSocial["facebook"] = "https://m.instagram.com/$str"
            }
            "google" ->
            {
                mapSocial["google"] = "https://$str"
            }
        }
        userReference!!.updateChildren(mapSocial).addOnCompleteListener { task ->
            if (task.isSuccessful)
            {
                Toast.makeText(context, "Task Updated Successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, reqCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqCode && resultCode == Activity.RESULT_OK && data!!.data != null )
        {
            imageUri = data.data
            Toast.makeText(context, "Uploading to Fire store!", Toast.LENGTH_SHORT).show()
            uploadImage2fb()
        }
    }

    private fun uploadImage2fb() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading. Please wait!")
        progressBar.show()

        if (imageUri != null){
            val fileRef = storeRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri> >{ task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener{ task ->
                if (task.isSuccessful)
                {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverCheck == "cover"){
                        val mapCover = HashMap<String, Any>()
                        mapCover["cover"] = url
                        userReference!!.updateChildren(mapCover)
                        coverCheck = ""
                    }
                    else{
                        val mapProfile = HashMap<String, Any>()
                        mapProfile["profile"] = url
                        userReference!!.updateChildren(mapProfile)
                        coverCheck = ""
                    }
                    progressBar.dismiss()
                }
            }
        }
    }


}
