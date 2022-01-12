package com.tolulonge.whatsappclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.tolulonge.whatsappclone.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var currentUserID : String
    private lateinit var mAuth : FirebaseAuth
    private lateinit var rootRef : DatabaseReference
    private lateinit var loadingBar: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference
        currentUserID = mAuth.currentUser?.uid.toString()
        loadingBar = ProgressDialog(this)

        binding.updateSettingsButton.setOnClickListener {
            updateSettings()
        }
        retrieveUserInformation()

        binding.setProfileImage.setOnClickListener{
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, GALLERY_PICK)
        }
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
                        Picasso.get().load(retrievedImage).into(binding.setProfileImage)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data

            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image")
                loadingBar.setMessage("Please wait, your profile image is updating...")
                loadingBar.setCanceledOnTouchOutside(false)
                loadingBar.show()
                val resultUri = result.uri

                val filePath = Firebase.userProfileImagesRef.child("$currentUserID.jpg")
                filePath.putFile(resultUri).continueWith {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Profile Image Uploaded Successfully...",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val message = it.exception.toString()
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_SHORT).show()
                        loadingBar.dismiss()
                    }
                    filePath.downloadUrl
                }.addOnCompleteListener{
                    if (it.isSuccessful){
                        it.result!!.addOnSuccessListener {task->
                            val downloadedUrl = task.toString()
                            Log.d("PhotoUpload", "onActivityResult: $downloadedUrl")
                            Firebase.rootRef.child("Users").child(currentUserID).child("image")
                                .setValue(downloadedUrl).addOnCompleteListener { imageTask ->
                                    if (imageTask.isSuccessful) {
                                        showToast("Image saved in Database successfully...", this)
                                        loadingBar.dismiss()
                                    } else {
                                        showToast("Error : ${imageTask.exception}", this)
                                        loadingBar.dismiss()
                                    }
                                }

                        }
                    }
                }
            }
        }
    }
}