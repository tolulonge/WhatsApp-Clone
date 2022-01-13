package com.tolulonge.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var receiverUserID : String
    private lateinit var currentState : String
    private lateinit var sendUserID : String
    private lateinit var binding: ActivityProfileBinding
    private lateinit var chatRequestRef: DatabaseReference
    private lateinit var contactRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentState = "new"
        sendUserID = FirebaseAuth.getInstance().currentUser?.uid.toString()
        chatRequestRef = Firebase.rootRef.child("Chat Requests")
        contactRef = Firebase.rootRef.child("Contacts")

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

                    manageChatRequests()
                }else{
                    val userName = snapshot.child("name").value.toString()
                    val userStatus = snapshot.child("status").value.toString()

                    binding.visitUserName.text = userName
                    binding.visitProfileStatus.text = userStatus

                    manageChatRequests()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun manageChatRequests() {

        chatRequestRef.child(sendUserID).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild(receiverUserID)){
                    val requestType = snapshot.child(receiverUserID).child("request_type").value.toString()
                    if (requestType == "sent"){
                        currentState = "request_sent"
                        binding.sendMessageRequestButton.text = "Cancel Chat Request"
                    }else if (requestType == "received"){
                        currentState = "request_received"
                        binding.sendMessageRequestButton.text = "Accept Chat Request"

                        binding.declineMessageRequestButton.visibility = View.VISIBLE
                        binding.declineMessageRequestButton.isEnabled = true

                        binding.declineMessageRequestButton.setOnClickListener {
                            cancelChatRequest()
                        }
                    }
                }else{
                    contactRef.child(sendUserID).addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.hasChild(receiverUserID)){
                                currentState = "friends"
                                binding.sendMessageRequestButton.text = "Remove this Contact"
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        if(sendUserID != receiverUserID){
           binding.sendMessageRequestButton.setOnClickListener {
               binding.sendMessageRequestButton.isEnabled = false
               if (currentState == "new"){
                   sendChatRequest()
               }
               if (currentState == "request_sent"){
                   cancelChatRequest()
               }
               if (currentState == "request_received"){
                   acceptChatRequest()
               }
               if (currentState == "friends"){
                   removeSpecificContact()
               }
           }
        }else{
            binding.sendMessageRequestButton.visibility = View.INVISIBLE
        }
    }

    private fun removeSpecificContact() {
        contactRef.child(sendUserID).child(receiverUserID).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful){
                contactRef.child(receiverUserID).child(sendUserID).removeValue().addOnCompleteListener {
                    if (it.isSuccessful){
                        binding.sendMessageRequestButton.isEnabled = true
                        currentState = "new"
                        binding.sendMessageRequestButton.text = "Send Message"

                        binding.declineMessageRequestButton.visibility = View.INVISIBLE
                        binding.declineMessageRequestButton.isEnabled = false
                    }
                }
            }
        }
    }

    private fun acceptChatRequest() {
        contactRef.child(sendUserID).child(receiverUserID)
            .child("Contacts").setValue("Saved")
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    contactRef.child(receiverUserID).child(sendUserID)
                        .child("Contacts").setValue("Saved")
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful){
                                chatRequestRef.child(sendUserID).child(receiverUserID)
                                    .removeValue()
                                    .addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful){
                                            chatRequestRef.child(receiverUserID).child(sendUserID)
                                                .removeValue()
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful){
                                                        binding.sendMessageRequestButton.isEnabled = true
                                                        currentState = "friends"
                                                        binding.sendMessageRequestButton.text = "Remove this Contact"

                                                        binding.declineMessageRequestButton.visibility = View.INVISIBLE
                                                        binding.declineMessageRequestButton.isEnabled = false
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                }
            }
    }

    private fun cancelChatRequest() {
        chatRequestRef.child(sendUserID).child(receiverUserID).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful){
                chatRequestRef.child(receiverUserID).child(sendUserID).removeValue().addOnCompleteListener {
                    if (it.isSuccessful){
                        binding.sendMessageRequestButton.isEnabled = true
                        currentState = "new"
                        binding.sendMessageRequestButton.text = "Send Message"

                        binding.declineMessageRequestButton.visibility = View.INVISIBLE
                        binding.declineMessageRequestButton.isEnabled = false
                    }
                }
            }
        }
    }

    private fun sendChatRequest() {
        chatRequestRef.child(sendUserID).child(receiverUserID)
            .child("request_type").setValue("sent")
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    chatRequestRef.child(receiverUserID).child(sendUserID)
                        .child("request_type").setValue("received")
                        .addOnCompleteListener {
                            if (it.isSuccessful){
                                binding.sendMessageRequestButton.isEnabled = true
                                currentState = "request_sent"
                                binding.sendMessageRequestButton.text = "Cancel Chat Request"
                            }
                        }
                }
            }
    }
}