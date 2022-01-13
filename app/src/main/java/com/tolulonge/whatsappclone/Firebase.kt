package com.tolulonge.whatsappclone

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object Firebase {
    val rootRef = FirebaseDatabase.getInstance().reference
    var currentUser = FirebaseAuth.getInstance().currentUser
    val userProfileImagesRef = FirebaseStorage.getInstance().reference.child("Profile Images")



}