package com.tolulonge.whatsappclone

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.CustomMessagesLayoutBinding

class MessageAdapter(private var userMessagesList: List<Messages>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>(){
    private lateinit var mAuth: FirebaseAuth
    private lateinit var usersRef: DatabaseReference

    // create an inner class with name ViewHolder
    // It takes a view argument, in which pass the generated class of single_item.xml
    // ie SingleItemBinding and in the RecyclerView.ViewHolder(binding.root) pass it like this
    inner class ViewHolder(val binding: CustomMessagesLayoutBinding) : RecyclerView.ViewHolder(binding.root)


    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val binding = CustomMessagesLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        mAuth = FirebaseAuth.getInstance()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val messageSenderID = mAuth.currentUser?.uid
        val messages = userMessagesList[position]

        val fromUserID = messages.from
        val fromMessageType = messages.type

        usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(fromUserID)
        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.hasChild("image")){
                    val receiverImage = snapshot.child("image").value.toString()
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.binding.messageProfileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        holder.binding.receiverMessageText.visibility = View.GONE
        holder.binding.messageProfileImage.visibility = View.GONE
        holder.binding.senderMesssageText.visibility = View.GONE

        holder.binding.messageSenderImageView.visibility = View.GONE
        holder.binding.messageReceiverImageView.visibility = View.GONE
        if (fromMessageType == "text"){


            if (fromUserID == messageSenderID){
                holder.binding.senderMesssageText.visibility = View.VISIBLE

                holder.binding.senderMesssageText.setBackgroundResource(R.drawable.sender_messages_layout)
                holder.binding.senderMesssageText.text = "${messages.message} \n \n${messages.time} - ${messages.date}"

            }else{
                holder.binding.receiverMessageText.visibility = View.VISIBLE
                holder.binding.messageProfileImage.visibility = View.VISIBLE

                    holder.binding.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout)
                    holder.binding.receiverMessageText.text = "${messages.message} \n \n${messages.time} - ${messages.date}"
            }
        }else if (fromMessageType == "image"){
            if (fromUserID == messageSenderID){
                holder.binding.messageSenderImageView.visibility = View.VISIBLE
                Picasso.get().load(messages.message).into(holder.binding.messageSenderImageView)
            }else{
                holder.binding.messageReceiverImageView.visibility = View.VISIBLE
                holder.binding.messageProfileImage.visibility = View.VISIBLE
                Picasso.get().load(messages.message).into(holder.binding.messageReceiverImageView)
            }
        }else if (fromMessageType == "pdf" || fromMessageType == "docx"){
            if (fromUserID == messageSenderID){
                holder.binding.messageSenderImageView.visibility = View.VISIBLE
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsappclone-b4403.appspot.com/o/Image%20Files%2Ffiles.png?alt=media&token=9c70a318-dd2a-44c5-bca9-a4aee05a46a9")
                    .into(holder.binding.messageSenderImageView)
            }else{
                holder.binding.messageReceiverImageView.visibility = View.VISIBLE
                holder.binding.messageProfileImage.visibility = View.VISIBLE
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/whatsappclone-b4403.appspot.com/o/Image%20Files%2Ffiles.png?alt=media&token=9c70a318-dd2a-44c5-bca9-a4aee05a46a9")
                    .into(holder.binding.messageReceiverImageView)
            }
        }
        if (fromUserID == messageSenderID){
            holder.itemView.setOnClickListener {
                if (userMessagesList[position].type == "pdf" || userMessagesList[position].type == "docx"){
                    val options = arrayOf("Delete for me", "Download and view this document", "Cancel", "Delete for everyone")
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.apply {
                        setTitle("Delete Message?")
                        setItems(options) { _, p1 ->
                            when (p1) {
                                0 -> {
                                    deleteSentMessage(position, holder)
                                }
                                1 -> {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList[position].message))
                                    holder.itemView.context.startActivity(intent)
                                }
                                3 -> {
                                    deleteMessageForEveryOne(position, holder)
                                }
                            }
                        }
                        show()
                    }


                }else if (userMessagesList[position].type == "text"){
                    val options = arrayOf("Delete for me", "Cancel", "Delete for everyone")
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.apply {
                        setTitle("Delete Message?")
                        setItems(options) { _, p1 ->
                            when (p1) {
                                0 -> {
                                    deleteSentMessage(position, holder)
                                }
                                2 -> {
                                    deleteMessageForEveryOne(position, holder)
                                }
                            }
                        }
                        show()
                    }
                }else if (userMessagesList[position].type == "image"){
                    val options = arrayOf("Delete for me", "View this Image","Cancel" ,"Delete for everyone")
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.apply {
                        setTitle("Delete Message?")
                        setItems(options) { _, p1 ->
                            when (p1) {
                                0 -> {
                                    deleteSentMessage(position, holder)
                                }
                                1 -> {
                                    viewImage(holder,userMessagesList[position].message)
                                }
                                3 -> {
                                    deleteMessageForEveryOne(position, holder)

                                }
                            }
                        }
                        show()
                    }
                }
            }
        }else{

            holder.itemView.setOnClickListener {
                if (userMessagesList[position].type == "pdf" || userMessagesList[position].type == "docx"){
                    val options = arrayOf("Delete for me", "Download and view this document", "Cancel")
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.apply {
                        setTitle("Delete Message?")
                        setItems(options) { _, p1 ->
                            when (p1) {
                                0 -> {
                                    deleteReceiveMessage(position, holder)
                                }
                                1 -> {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList[position].message))
                                    holder.itemView.context.startActivity(intent)
                                }

                            }
                        }
                        show()
                    }


                }else if (userMessagesList[position].type == "text"){
                    val options = arrayOf("Delete for me", "Cancel")
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.apply {
                        setTitle("Delete Message?")
                        setItems(options) { _, p1 ->
                            when (p1) {
                                0 -> {
                                    deleteReceiveMessage(position, holder)
                                }

                            }
                        }
                        show()
                    }
                }else if (userMessagesList[position].type == "image"){
                    val options = arrayOf("Delete for me", "View this Image","Cancel")
                    val builder = AlertDialog.Builder(holder.itemView.context)
                    builder.apply {
                        setTitle("Delete Message?")
                        setItems(options) { _, p1 ->
                            when (p1) {
                                0 -> {
                                    deleteReceiveMessage(position,holder)
                                }
                                1 -> {
                                    viewImage(holder,userMessagesList[position].message)
                                }
                            }
                        }
                        show()
                    }
                }
            }

        }
    }

    private fun viewImage(context : MessageAdapter.ViewHolder, imageUrl : String){
        val intent = Intent(context.itemView.context, ImageViewer::class.java)
        intent.putExtra("url", imageUrl)
        context.itemView.context.startActivity(intent)
    }

    private fun deleteSentMessage(position: Int, holder: MessageAdapter.ViewHolder){
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages").child(userMessagesList[position].from)
            .child(userMessagesList[position].to)
            .child(userMessagesList[position].messageID)
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful){
                    showToast("Deleted Successfully", holder.itemView.context)
                }else{
                    showToast("Error Occurred", holder.itemView.context)
                }
            }
    }

    private fun deleteReceiveMessage(position: Int, holder: MessageAdapter.ViewHolder){
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages").child(userMessagesList[position].to)
            .child(userMessagesList[position].from)
            .child(userMessagesList[position].messageID)
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful){
                    showToast("Deleted Successfully", holder.itemView.context)
                }else{
                    showToast("Error Occurred", holder.itemView.context)
                }
            }
    }

    private fun deleteMessageForEveryOne(position: Int, holder: MessageAdapter.ViewHolder){
        val rootRef = FirebaseDatabase.getInstance().reference
        rootRef.child("Messages").child(userMessagesList[position].to)
            .child(userMessagesList[position].from)
            .child(userMessagesList[position].messageID)
            .removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    rootRef.child("Messages").child(userMessagesList[position].from)
                        .child(userMessagesList[position].to)
                        .child(userMessagesList[position].messageID)
                        .removeValue().addOnCompleteListener {
                            if (it.isSuccessful){
                                showToast("Deleted Successfully", holder.itemView.context)
                            }
                        }
                }else{
                    showToast("Error Occurred", holder.itemView.context)
                }
            }
    }



    override fun getItemCount(): Int {
      return userMessagesList.size
    }
}

