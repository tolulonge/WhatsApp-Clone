package com.tolulonge.whatsappclone

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.FragmentContactsBinding
import com.tolulonge.whatsappclone.databinding.FragmentRequestsBinding
import com.tolulonge.whatsappclone.databinding.UsersDisplayLayoutBinding

class RequestsFragment : Fragment() {



    private var _binding: FragmentRequestsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var chatRequestRef : DatabaseReference
    private lateinit var usersRef : DatabaseReference
    private lateinit var currentUserID : String
    private lateinit var contactRef: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestsBinding.inflate(inflater, container, false)
        currentUserID = Firebase.currentUser?.uid.toString()
        chatRequestRef = Firebase.rootRef.child("Chat Requests")
        usersRef = Firebase.rootRef.child("Users")
        contactRef = Firebase.rootRef.child("Contacts")


        return binding.root
    }


    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(chatRequestRef.child(currentUserID), Contacts::class.java).build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options){

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RequestsViewHolder {
                val binding = UsersDisplayLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return RequestsViewHolder(binding)
            }

            override fun onBindViewHolder(
                holder: RequestsViewHolder,
                position: Int,
                model: Contacts
            ) {

                with(holder){


                    val listUserID = getRef(position).key.toString()

                    val getTypeRef = getRef(position).child("request_type").ref

                    getTypeRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                            if (snapshot.exists()) {
                                val type = snapshot.value.toString()
                                Log.d("Request", "onDataChange: $type")



                                    usersRef.child(listUserID).addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val userName = snapshot.child("name").value.toString()
                                            val userStatus = snapshot.child("status").value.toString()
                                            if (snapshot.hasChild("image")) {
                                                val profileImage = snapshot.child("image").value.toString()
                                                binding2.userProfileName.text = userName
                                                Picasso.get().load(profileImage)
                                                    .placeholder(R.drawable.profile_image)
                                                    .into(binding2.usersProfileImage)

                                                if (type == "received"){
                                                    binding2.apply {
                                                        requestAcceptBtn.apply {
                                                            visibility = View.VISIBLE
                                                            setOnClickListener {
                                                                alertDialogAccept(userName,listUserID)
                                                            }
                                                        }
                                                        requestCancelBtn.apply {
                                                            visibility = View.VISIBLE
                                                           setOnClickListener {
                                                               alertDialogAccept(userName,listUserID)
                                                           }
                                                        }

                                                    }
                                                    binding2.userStatus.text = "wants to connect with you"
                                                    itemView.setOnClickListener {
                                                        alertDialogAccept(userName,listUserID)
                                                    }
                                                }else{
                                                    binding2.userStatus.text = "request sent, awaiting confirmation"
                                                }


                                            } else {
                                                binding2.userProfileName.text = userName


                                                if (type == "received"){
                                                    binding2.apply {
                                                        requestAcceptBtn.apply {
                                                            visibility = View.VISIBLE
                                                           setOnClickListener {alertDialogAccept(userName,listUserID)}
                                                        }
                                                        requestCancelBtn.apply {
                                                            visibility = View.VISIBLE
                                                           setOnClickListener { alertDialogAccept(userName,listUserID)}
                                                        }

                                                    }
                                                    binding2.userStatus.text = "wants to connect with you"
                                                    holder.itemView.setOnClickListener {
                                                        alertDialogAccept(userName,listUserID)
                                                    }
                                                }else{
                                                    binding2.userStatus.text = "Request sent, awaiting confirmation"
                                                }

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

                }
            }

        }

        binding.chatsRequestList.adapter = adapter
        adapter.startListening()
    }

    inner class RequestsViewHolder( val binding2: UsersDisplayLayoutBinding) : RecyclerView.ViewHolder(binding2.root) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun acceptChatRequest(sendUserID: String, receiverUserID: String) {
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
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "New Contact Saved",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                }
            }
    }

    private fun cancelChatRequest(sendUserID: String, receiverUserID: String) {
        chatRequestRef.child(sendUserID).child(receiverUserID).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful){
                chatRequestRef.child(receiverUserID).child(sendUserID).removeValue().addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(requireContext(), "Contact Deleted", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun alertDialogAccept(userName: String, listUserID: String){
        val optionsArray = arrayOf("Accept","Cancel")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("$userName Chat Request")
        builder.setItems(optionsArray) { _, p1 ->
            when (p1) {
                0 -> {
                    acceptChatRequest(currentUserID, listUserID)
                }
                1 -> {
                    cancelChatRequest(currentUserID, listUserID)
                }
            }
        }
        builder.show()
    }

}