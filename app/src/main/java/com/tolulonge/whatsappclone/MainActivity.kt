package com.tolulonge.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.tolulonge.whatsappclone.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var myTabsAccessorAdapter: TabsAccessorAdapter
    private lateinit var currentUserId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("MainActivity", "onCreate: ")

         setSupportActionBar(binding.mainToolBar)
        supportActionBar?.title = "WhatsApp"
        myTabsAccessorAdapter = TabsAccessorAdapter(supportFragmentManager, 4)
        binding.mainTabsPager.apply {
            adapter = myTabsAccessorAdapter
        }.also { binding.mainTabs.setupWithViewPager(it) }
    }

    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null){
            sendUserToLoginActivity()
        }
        else{
            updateUserStatus("online")
            verifyUserExistence()
        }
    }

    override fun onStop() {
        super.onStop()
        if(FirebaseAuth.getInstance().currentUser != null){
            updateUserStatus("offline")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(FirebaseAuth.getInstance().currentUser != null){
            updateUserStatus("offline")
        }
    }

    private fun verifyUserExistence() {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserID != null) {
            Firebase.rootRef.child("Users").child(currentUserID).addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child("name").exists()){
                        Toast.makeText(this@MainActivity, "Welcome", Toast.LENGTH_SHORT).show()
                    }else{
                        sendUserToSettingsActivity()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
            R.id.main_logout_option -> {
                FirebaseAuth.getInstance().signOut()
                updateUserStatus("offline")
                Firebase.currentUser = null
                sendUserToLoginActivity()
            }
             R.id.main_create_group_option -> {
                 requestNewGroup()
             }
            R.id.main_settings_option -> {
                sendUserToSettingsActivity()
            }

            R.id.main_find_friends_option -> {
                sendUserToFindFriendsActivity()
            }
            else -> super.onOptionsItemSelected(item)
        }

        return true
    }

    private fun requestNewGroup() {
        val builder = AlertDialog.Builder(this,
        R.style.AlertDialog)

        builder.setTitle("Enter Group Name :")
        builder.apply {
            val groupNameField = EditText(this@MainActivity)
            groupNameField.hint = "e.g Droiders"
            setView(groupNameField)
            setPositiveButton("Create"
            ) { p0, p1 ->
                val groupName = groupNameField.text.toString()
                if (groupName.isEmpty()){
                    Toast.makeText(this@MainActivity, "Please write Group Name", Toast.LENGTH_SHORT).show()
                }
                else{
                    createNewGroup(groupName)
                }
            }
            setNegativeButton("Cancel"
            ) { p0, _ ->
                p0.cancel()
            }
            show()

        }
    }

    private fun createNewGroup(groupName: String) {
        Firebase.rootRef.child("Groups").child(groupName).setValue("")
            .addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(
                        this,
                        "$groupName group is created successfully...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    private fun sendUserToSettingsActivity() {
        val settingsIntent = Intent(this, SettingsActivity::class.java)
        startActivity(settingsIntent)
    }

    private fun sendUserToFindFriendsActivity(){
        val findFriendsIntent = Intent(this, FindFriendsActivity::class.java)
        startActivity(findFriendsIntent)
    }

    private fun updateUserStatus(state : String){

        val currentDate = SimpleDateFormat("MMM dd, yyyy")
        val calendar = Calendar.getInstance()
        val saveCurrentDate = currentDate.format(calendar.time)

        val currentTime = SimpleDateFormat("hh:mm a")
        val saveCurrentTime = currentTime.format(calendar.time)

        val onlineStateMap = hashMapOf("time" to saveCurrentTime, "date" to saveCurrentDate, "state" to state)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        Firebase.rootRef.child("Users").child(currentUserId).child("userState")
            .updateChildren(onlineStateMap as Map<String, Any>)


    }
}