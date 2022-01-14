package com.tolulonge.whatsappclone

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
        if (fromMessageType == "text"){
            holder.binding.receiverMessageText.visibility = View.INVISIBLE
            holder.binding.messageProfileImage.visibility = View.INVISIBLE
            holder.binding.senderMessageText.visibility = View.INVISIBLE


            if (fromUserID == messageSenderID){
                holder.binding.senderMessageText.visibility = View.VISIBLE

                holder.binding.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout)
                holder.binding.senderMessageText.text = messages.message
            }else{
                holder.binding.receiverMessageText.visibility = View.VISIBLE
                holder.binding.messageProfileImage.visibility = View.VISIBLE

                    holder.binding.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout)
                    holder.binding.receiverMessageText.text = messages.message
            }
        }

    }

    override fun getItemCount(): Int {
      return userMessagesList.size
    }
}

