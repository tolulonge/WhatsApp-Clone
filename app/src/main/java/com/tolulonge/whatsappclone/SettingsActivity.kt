package com.tolulonge.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tolulonge.whatsappclone.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var currentUserID : String
    private lateinit var mAuth : FirebaseAuth
    private lateinit var rootRef : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference
        currentUserID = mAuth.currentUser?.uid.toString()

        binding.updateSettingsButton.setOnClickListener {
            updateSettings()
        }
        retrieveUserInformation()
    }

    private fun updateSettings() {
        val setUserName = binding.setUserName.text.toString()
        val setStatus = binding.setProfileStatus.text.toString()

        if (setUserName.isEmpty()){
            Toast.makeText(this, "Please write your user name...", Toast.LENGTH_SHORT).show()
            return
        }
        if (setStatus.isEmpty()){
            Toast.makeText(this, "Please write your user name...", Toast.LENGTH_SHORT).show()
            return
        }
        val profileMap = hashMapOf("uid" to currentUserID, "name" to setUserName, "status" to setStatus)
        rootRef.child("Users").child(currentUserID).setValue(profileMap)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    sendUserToMainActivity()
                }
                else{
                    val message = it.exception.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    private fun retrieveUserInformation() {
        rootRef.child("Users").child(currentUserID)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image")){
                        val retrievedUserName = snapshot.child("name").value.toString()
                        val retrievedStatus = snapshot.child("status").value.toString()
                        val retrievedImage = snapshot.child("image").value.toString()

                        binding.setUserName.setText(retrievedUserName)
                        binding.setProfileStatus.setText(retrievedStatus)
                     //   binding.setProfileImage.setImageResource(retrievedUserName.toInt())

                    }else if (snapshot.exists() && snapshot.hasChild("name")){
                        val retrievedUserName = snapshot.child("name").value.toString()
                        val retrievedStatus = snapshot.child("status").value.toString()

                        binding.setUserName.setText(retrievedUserName)
                        binding.setProfileStatus.setText(retrievedStatus)
                    }else
                    {
                        Toast.makeText(this@SettingsActivity, "Please set & update your profile information...", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}