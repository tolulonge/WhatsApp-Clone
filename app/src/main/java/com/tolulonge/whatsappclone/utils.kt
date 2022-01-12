package com.tolulonge.whatsappclone

import android.content.Context
import android.widget.Toast

const val GALLERY_PICK = 1

fun showToast(message : String, context: Context){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}