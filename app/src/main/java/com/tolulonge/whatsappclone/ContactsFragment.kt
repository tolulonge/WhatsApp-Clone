package com.tolulonge.whatsappclone

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.FragmentContactsBinding
import com.tolulonge.whatsappclone.databinding.FragmentGroupsBinding
import com.tolulonge.whatsappclone.databinding.UsersDisplayLayoutBinding

class ContactsFragment : Fragment() {



    private var _binding: FragmentContactsBinding? = null

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
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        currentUserID = Firebase.currentUser?.uid.toString()
        contactsRef = Firebase.rootRef.child("Contacts").child(currentUserID)
        usersRef = Firebase.rootRef.child("Users")

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(contactsRef, Contacts::class.java).build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ContactsViewHolder {
                val binding = UsersDisplayLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return ContactsViewHolder(binding)
            }

            override fun onBindViewHolder(
                holder: ContactsViewHolder,
                position: Int,
                model: Contacts
            ) {
                with(holder){
                        val userIDs = getRef(position).key.toString()
                    Log.d("CurrentContact", "onBindViewHolder: $userIDs")
                        usersRef.child(userIDs).addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.hasChild("image")){
                                    val profileImage = snapshot.child("image").value.toString()
                                    val userName = snapshot.child("name").value.toString()
                                    val userStatus = snapshot.child("status").value.toString()

                                    binding2.userProfileName.text = userName
                                    binding2.userStatus.text = userStatus
                                    Picasso.get().load(profileImage)
                                        .placeholder(com.tolulonge.whatsappclone.R.drawable.profile_image)
                                        .into(binding2.usersProfileImage)
                                }else{
                                    val userName = snapshot.child("name").value.toString()
                                    val userStatus = snapshot.child("status").value.toString()

                                    binding2.userProfileName.text = userName
                                    binding2.userStatus.text = userStatus
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })


                        itemView.setOnClickListener {
                            val visitUserId = getRef(position).key

                            val intent = Intent(requireContext(), ProfileActivity::class.java)
                            intent.putExtra("visit_user_id",visitUserId)
                            startActivity(intent)
                        }
                }
            }

        }

        binding.contactsList.adapter = adapter
        adapter.startListening()
    }

    inner class ContactsViewHolder( val binding2: UsersDisplayLayoutBinding) : RecyclerView.ViewHolder(binding2.root) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}