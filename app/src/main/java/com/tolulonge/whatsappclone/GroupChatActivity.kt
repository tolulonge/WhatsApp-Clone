package com.tolulonge.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.tolulonge.whatsappclone.databinding.ActivityGroupChatBinding
import java.text.SimpleDateFormat
import java.util.*

class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var currentGroupName : String
    private lateinit var currentUserId : String
    private lateinit var currentUserName : String
    private lateinit var currentDate : String
    private lateinit var currentTime : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentGroupName = intent.extras?.get("groupName").toString()
        currentUserId = Firebase.mAuth.uid.toString()

        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show()

        setSupportActionBar(binding.groupChatBarLayout)
        supportActionBar?.title = currentGroupName

        getUserInfo()
        binding.sendMessageBtn.setOnClickListener {
            saveMessageInfoToDatabase()
            binding.inputGroupMessage.setText("")
            binding.myScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }


    }

    override fun onStart() {
        super.onStart()
        Firebase.rootRef.child("Groups").child(currentGroupName).addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    displayMessages(snapshot)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.exists()){
                    displayMessages(snapshot)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }




    private fun getUserInfo() {
        Firebase.rootRef.child("Users").child(currentUserId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    currentUserName = snapshot.child("name").value.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun saveMessageInfoToDatabase() {
        val message = binding.inputGroupMessage.text.toString()
        val messageKey = Firebase.rootRef.child("Groups").push().key
        if (message.isEmpty()){
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show()
        }
        else{
            val calForDate = Calendar.getInstance()
            val currentDateFormat = SimpleDateFormat("MMM dd, yyyy")
            currentDate = currentDateFormat.format(calForDate.time)

            val calForTime = Calendar.getInstance()
            val currentTimeFormat = SimpleDateFormat("hh:mm a")
            currentTime = currentTimeFormat.format(calForTime.time)

            val groupMessageKey = hashMapOf<String, Any>()
            Firebase.rootRef.child("Groups").child(currentGroupName).updateChildren(groupMessageKey)

            val messageInfoMap = hashMapOf("name" to currentUserName, "message" to message, "date" to currentDate, "time" to currentTime)
            if (messageKey != null) {
                Firebase.rootRef.child("Groups").child(currentGroupName).child(messageKey).updateChildren(messageInfoMap as Map<String, Any>)
            }



        }
    }

    private fun displayMessages(snapshot: DataSnapshot) {
        val iterator = snapshot.children.iterator()
        while (iterator.hasNext()){
            val chatDate = iterator.next().value
            val chatMessage = iterator.next().value
            val chatName = iterator.next().value
            val chatTime = iterator.next().value

            binding.groupChatTextDisplay.append("$chatName \n $chatMessage \n $chatTime       $chatDate \n\n\n")
            binding.myScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}