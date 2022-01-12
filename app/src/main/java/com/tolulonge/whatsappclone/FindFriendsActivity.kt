package com.tolulonge.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.squareup.picasso.Picasso
import com.tolulonge.whatsappclone.databinding.ActivityFindFriendsBinding
import com.tolulonge.whatsappclone.databinding.UsersDisplayLayoutBinding

class FindFriendsActivity() : AppCompatActivity() {
    private lateinit var binding: ActivityFindFriendsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.findFriendsToolbar)
        supportActionBar?.title = "Find Friends"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(Firebase.rootRef.child("Users"), Contacts::class.java).build()

        val adapter = object : FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): FindFriendViewHolder {
                val binding = UsersDisplayLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                return FindFriendViewHolder(binding)
            }

            override fun onBindViewHolder(
                holder: FindFriendViewHolder,
                position: Int,
                model: Contacts
            ) {
                with(holder){
                    with(model){
                        binding2.userProfileName.text = this.name
                        binding2.userStatus.text = this.status
                        Picasso.get().load(this.image)
                            .placeholder(R.drawable.profile_image)
                            .into(binding2.usersProfileImage)

                        itemView.setOnClickListener {
                            val visitUserId = getRef(position).key

                            val intent = Intent(this@FindFriendsActivity, ProfileActivity::class.java)
                            intent.putExtra("visit_user_id",visitUserId)
                            startActivity(intent)
                        }
                    }
                }
            }

        }

        binding.findFriendsRecyclerList.adapter = adapter
        adapter.startListening()
    }

   inner class FindFriendViewHolder( val binding2: UsersDisplayLayoutBinding) : RecyclerView.ViewHolder(binding2.root) {

    }
}