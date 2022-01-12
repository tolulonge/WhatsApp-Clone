package com.tolulonge.whatsappclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tolulonge.whatsappclone.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRegisterBinding
    private lateinit var loadingBar: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingBar = ProgressDialog(this)

        binding.alreadyHaveAccountLink.setOnClickListener {
            sendUserToLoginActivity()
        }

        binding.registerBtn.setOnClickListener{
            createNewAccount()
        }
    }

    private fun createNewAccount() {
        val email = binding.registerEmail.text.toString()
        val password = binding.registerPassword.text.toString()

        if (email.isEmpty()){
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()){
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show()
            return
        }
        loadingBar.setTitle("Creating New Account")
        loadingBar.setMessage("Please Wait, while we are creating new account for you...")
        loadingBar.setCanceledOnTouchOutside(true)
        loadingBar.show()
            Firebase.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    val currentUserId = Firebase.mAuth.currentUser?.uid
                    if (currentUserId != null) {
                        Firebase.rootRef.child("Users").child(currentUserId).setValue("")
                    }

                    Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                    sendUserToMainActivity()
                }else{
                    val message = it.exception.toString()
                    Toast.makeText(this, "Error : $message", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                }
            }
    }

    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
    }
    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
}