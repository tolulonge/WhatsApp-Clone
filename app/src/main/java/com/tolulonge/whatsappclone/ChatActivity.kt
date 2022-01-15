package com.tolulonge.whatsappclone

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.ActivityChatBinding
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageReceiverID: String
    private lateinit var messageSenderID: String
    private lateinit var messageReceiverName: String
    private lateinit var messageReceiverImage: String
    private lateinit var saveCurrentTime: String
    private lateinit var saveCurrentDate: String
    private lateinit var userName: TextView
    private lateinit var userLastSeen: TextView
    private lateinit var userImage: CircleImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var rootRef: DatabaseReference
    private  var messagesList = arrayListOf<Messages>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference
        messageSenderID = mAuth.currentUser?.uid.toString()

        messageReceiverID = intent.extras?.get("visit_user_id").toString()
        messageReceiverName = intent.extras?.get("visit_user_name").toString()
        messageReceiverImage = intent.extras?.get("visit_image").toString()

        initializeControllers()
        displayLastSeen()

        userName.text = messageReceiverName
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage)

        binding.sendMessageBtn.setOnClickListener {
            sendMessage()
        }

    }

    override fun onStart() {
        super.onStart()

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val messages = snapshot.getValue(Messages::class.java)

                    if (messages != null) {
                        messagesList.add(messages)
                        adapter.notifyDataSetChanged()
                        binding.privateMessagesListOfUsers.adapter?.itemCount?.let {
                            binding.privateMessagesListOfUsers.smoothScrollToPosition(
                                it
                            )
                        }
                    }

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun initializeControllers() {
        setSupportActionBar(binding.chatToolBar)

        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        adapter = MessageAdapter(messagesList)
        linearLayoutManager = LinearLayoutManager(this)
        binding.privateMessagesListOfUsers.layoutManager = linearLayoutManager
        binding.privateMessagesListOfUsers.adapter = adapter


        val layoutInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val actionBarView : View = layoutInflater.inflate(R.layout.custom_chat_bar,null)
        supportActionBar?.customView = actionBarView

        userName = findViewById(R.id.custom_profile_name)
        userImage = findViewById(R.id.custom_profile_image)
        userLastSeen = findViewById(R.id.custom_user_last_seen)

        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        val calendar = Calendar.getInstance()
        saveCurrentDate = currentDate.format(calendar.time)

        val currentTime = SimpleDateFormat("hh:mm a")
        saveCurrentTime = currentTime.format(calendar.time)
    }

    private fun displayLastSeen(){
        rootRef.child("Users").child(messageReceiverID).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("userState").hasChild("state")){
                    val state = snapshot.child("userState").child("state").value.toString()
                    val date = snapshot.child("userState").child("date").value.toString()
                    val time = snapshot.child("userState").child("time").value.toString()

                    if (state == "online"){
                        userLastSeen.text = "Online"
                    }else if (state == "offline"){
                        userLastSeen.text = "Last Seen: $date $time "
                    }

                }else{
                    userLastSeen.text = "Offline"

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    private fun sendMessage(){
        val messageText = binding.inputMessage.text.toString()

        if (messageText.isEmpty()){
            Toast.makeText(this, "Please write your message...", Toast.LENGTH_SHORT).show()
        }else{
            val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
            val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"

            val userMessageKeyRef = rootRef.child("Messages")
                .child(messageSenderID).child(messageReceiverID).push()

            val messagePushID = userMessageKeyRef.key
            val messageTextBody = hashMapOf("message" to messageText, "type" to "text", "from" to messageSenderID,"to" to messageReceiverID
            ,"messageID" to messagePushID, "time" to saveCurrentTime, "date" to saveCurrentDate)

            val messageBodyDetails = hashMapOf("$messageSenderRef/$messagePushID" to messageTextBody, "$messageReceiverRef/$messagePushID" to messageTextBody)
            rootRef.updateChildren(messageBodyDetails as Map<String, Any>).addOnCompleteListener {
                if (it.isComplete){
                    if(it.isSuccessful){
                        Toast.makeText(this, "Message Sent Successfully", Toast.LENGTH_SHORT).show()
                    }else{
                        showToast("Error",this)
                    }
                    binding.inputMessage.setText("")
                }
            }
        }
    }
}