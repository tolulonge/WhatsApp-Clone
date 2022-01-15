package com.tolulonge.whatsappclone

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.ActivityChatBinding
import de.hdodenhof.circleimageview.CircleImageView
import java.net.URI
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
    private var checker = ""
    private var myUrl = ""
    private lateinit var fileUri : Uri
    private lateinit var uploadTask : UploadTask
    private lateinit var loadingBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingBar = ProgressDialog(this)

        mAuth = FirebaseAuth.getInstance()
        rootRef = FirebaseDatabase.getInstance().reference
        messageSenderID = mAuth.currentUser?.uid.toString()

        messageReceiverID = intent.extras?.get("visit_user_id").toString()
        messageReceiverName = intent.extras?.get("visit_user_name").toString()
        messageReceiverImage = intent.extras?.get("visit_image").toString()

        initializeControllers()
        displayLastSeen()
        readDataFromFirebase()


        userName.text = messageReceiverName
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage)

        binding.sendMessageBtn.setOnClickListener {
            sendMessage()
        }

        binding.sendFilesBtn.setOnClickListener {
            val options = arrayOf("Images","PDF Files","Ms Word Files")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select the File")
            builder.setItems(options) { p0, p1 ->
                when (p1) {
                    0 -> {
                        checker = "image"

                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "image/*"
                        startActivityForResult(Intent.createChooser(intent, "Select Image"), 438)

                    }
                    1 -> {
                        checker = "pdf"

                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "application/pdf"
                        startActivityForResult(Intent.createChooser(intent, "Select PDF File"), 438)


                    }
                    2 -> {
                        checker = "docx"

                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "application/msword"
                        startActivityForResult(Intent.createChooser(intent, "Select Ms Word File"), 438)
                    }
                }
            }
            builder.show()
        }


    }


    private fun readDataFromFirebase(){

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val messages = snapshot.getValue(Messages::class.java)

                    if (messages != null) {
                        messagesList.add(messages)
                        adapter.notifyItemInserted(messagesList.size)
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
                    val messagesRemoved = snapshot.getValue(Messages::class.java)

                    for (i in messagesList){
                        var isRemoved = false
                        messagesRemoved?.let {
                            if (i.messageID == it.messageID){
                                messagesList.removeAt(messagesList.indexOf(i))
                                adapter.notifyItemRemoved(messagesList.indexOf(i))
                                adapter.notifyDataSetChanged()
                                isRemoved = true
                            }
                        }
                      if (isRemoved) break
                    }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data.data != null){
            loadingBar.setTitle("Sending File")
            loadingBar.setMessage("Please wait, while we are sending the file...")
            loadingBar.setCanceledOnTouchOutside(false)
            loadingBar.show()

           fileUri = data.data!!

            if (checker != "image"){
                val storageReference = FirebaseStorage.getInstance().reference.child("Document Files")

                val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
                val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"

                val userMessageKeyRef = rootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push()

                val messagePushID = userMessageKeyRef.key

                val filePath = storageReference.child("$messagePushID.$checker")

                val uploadTask = filePath.putFile(fileUri)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation filePath.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val messageDocumentBody = hashMapOf("message" to myUrl,"name" to fileUri.lastPathSegment, "type" to checker, "from" to messageSenderID,"to" to messageReceiverID
                            ,"messageID" to messagePushID, "time" to saveCurrentTime, "date" to saveCurrentDate)

                        val messageBodyDetails = hashMapOf("$messageSenderRef/$messagePushID" to messageDocumentBody, "$messageReceiverRef/$messagePushID" to messageDocumentBody)
                        rootRef.updateChildren(messageBodyDetails as Map<String, Any>).addOnCompleteListener {
                            if (it.isComplete){
                                if(it.isSuccessful){
                                    loadingBar.dismiss()
                                    Toast.makeText(this, "Message Sent Successfully", Toast.LENGTH_SHORT).show()
                                }else{
                                    loadingBar.dismiss()
                                    showToast("Error",this)
                                }
                                binding.inputMessage.setText("")
                            }
                        }
                    }
                }.addOnFailureListener {
                    loadingBar.dismiss()
                    showToast(it.message.toString(), this)
                }

                uploadTask.addOnProgressListener {
                    val p = (100.0 * it.bytesTransferred)/it.totalByteCount
                    loadingBar.setMessage("${p.toInt()}% Uploading...")
                }


            }else if (checker == "image"){
                val storageReference = FirebaseStorage.getInstance().reference.child("Image Files")

                val messageSenderRef = "Messages/$messageSenderID/$messageReceiverID"
                val messageReceiverRef = "Messages/$messageReceiverID/$messageSenderID"

                val userMessageKeyRef = rootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push()

                val messagePushID = userMessageKeyRef.key

                val filePath = storageReference.child("$messagePushID.jpg")

                uploadTask = filePath.putFile(fileUri)


                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation filePath.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val messageImageBody = hashMapOf("message" to myUrl,"name" to fileUri.lastPathSegment, "type" to checker, "from" to messageSenderID,"to" to messageReceiverID
                            ,"messageID" to messagePushID, "time" to saveCurrentTime, "date" to saveCurrentDate)

                        val messageBodyDetails = hashMapOf("$messageSenderRef/$messagePushID" to messageImageBody, "$messageReceiverRef/$messagePushID" to messageImageBody)
                        rootRef.updateChildren(messageBodyDetails as Map<String, Any>).addOnCompleteListener {
                            if (it.isComplete){
                                if(it.isSuccessful){
                                    loadingBar.dismiss()
                                    Toast.makeText(this, "Message Sent Successfully", Toast.LENGTH_SHORT).show()
                                }else{
                                    loadingBar.dismiss()
                                    showToast("Error",this)
                                }
                                binding.inputMessage.setText("")
                            }
                        }


                    }
                }


            }else{
                loadingBar.dismiss()
                Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show()
            }
        }
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