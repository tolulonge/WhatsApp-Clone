package com.tolulonge.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.ActivityImageViewerBinding

class ImageViewer : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewerBinding
    private lateinit var imageUrl : String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUrl = intent.getStringExtra("url").toString()
        Picasso.get().load(imageUrl).into(binding.imageViewer)
    }
}