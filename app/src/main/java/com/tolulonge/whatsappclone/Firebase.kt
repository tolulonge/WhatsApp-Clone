package com.tolulonge.whatsappclone

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object Firebase {
    val mAuth = FirebaseAuth.getInstance()
    val rootRef = FirebaseDatabase.getInstance().reference
    val currentUser = mAuth.currentUser

}