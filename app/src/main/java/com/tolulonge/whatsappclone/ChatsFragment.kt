package com.tolulonge.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.FragmentChatsBinding
import com.tolulonge.whatsappclone.databinding.FragmentContactsBinding
import com.tolulonge.whatsappclone.databinding.UsersDisplayLayoutBinding


class ChatsFragment : Fragment() {
    private var _binding: FragmentChatsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var contactsRef : DatabaseReference
    private lateinit var usersRef : DatabaseReference
    private lateinit var currentUserID : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        currentUserID = Firebase.currentUser?.uid.toString()
        contactsRef = Firebase.rootRef.child("Contacts").child(currentUserID)
        usersRef = Firebase.rootRef.child("Users")

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(contactsRef, Contacts::class.java).build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ChatViewHolder {
                val binding = UsersDisplayLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return ChatViewHolder(binding)
            }

            override fun onBindViewHolder(
                holder: ChatViewHolder,
                position: Int,
                model: Contacts
            ) {
                with(holder){
                    val userIDs = getRef(position).key.toString()
                    val retImage = arrayOf("default_image")
                    Log.d("CurrentContact", "onBindViewHolder: $userIDs")
                    usersRef.child(userIDs).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                val userName = snapshot.child("name").value.toString()
                                val userStatus = snapshot.child("status").value.toString()


                                if (snapshot.hasChild("image")){
                                    retImage[0] = snapshot.child("image").value.toString()
                                    Picasso.get().load(retImage[0])
                                        .placeholder(R.drawable.profile_image)
                                        .into(binding2.usersProfileImage)
                                }
                                binding2.userProfileName.text = userName
                                binding2.userStatus.text = "Last Seen: \nDate  Time "

                                itemView.setOnClickListener {
                                    val chatIntent = Intent(requireContext(), ChatActivity::class.java)
                                    chatIntent.putExtra("visit_user_id",userIDs)
                                    chatIntent.putExtra("visit_user_name", userName)
                                    chatIntent.putExtra("visit_image",retImage[0])
                                    startActivity(chatIntent)
                                }

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
                }
            }

        }

        binding.chatsList.adapter = adapter
        adapter.startListening()
    }

    inner class ChatViewHolder( val binding2: UsersDisplayLayoutBinding) : RecyclerView.ViewHolder(binding2.root) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}