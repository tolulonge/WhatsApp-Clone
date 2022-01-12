package com.tolulonge.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var receiverUserID : String
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiverUserID = intent.extras?.get("visit_user_id").toString()
        Toast.makeText(this, receiverUserID, Toast.LENGTH_SHORT).show()

        retrieveUserInfo()
    }

    private fun retrieveUserInfo() {
        Firebase.rootRef.child("Users").child(receiverUserID).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("image")){
                    val userImage = snapshot.child("image").value.toString()
                    val userName = snapshot.child("name").value.toString()
                    val userStatus = snapshot.child("status").value.toString()

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(binding.visitProfileImage)
                    binding.visitUserName.text = userName
                    binding.visitProfileStatus.text = userStatus
                }else{
                    val userName = snapshot.child("name").value.toString()
                    val userStatus = snapshot.child("status").value.toString()

                    binding.visitUserName.text = userName
                    binding.visitProfileStatus.text = userStatus
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}