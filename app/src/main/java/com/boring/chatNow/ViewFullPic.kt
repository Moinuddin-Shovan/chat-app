package com.boring.chatNow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.squareup.picasso.Picasso

class ViewFullPic : AppCompatActivity() {

    private var imageViewer: ImageView? = null
    private var imgUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_pic)

        imgUrl = intent.getStringExtra("Url")
        imageViewer = findViewById(R.id.image_viewer)

        Picasso.get().load(imgUrl).into(imageViewer)
    }
}