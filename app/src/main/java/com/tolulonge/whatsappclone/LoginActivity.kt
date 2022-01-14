package com.tolulonge.whatsappclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.tolulonge.whatsappclone.databinding.ActivityLoginBinding
import com.tolulonge.whatsappclone.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loadingBar: ProgressDialog
    private lateinit var usersRef: DatabaseReference
    private lateinit var deviceToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingBar = ProgressDialog(this)
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")

        binding.needNewAccontLink.setOnClickListener {
            sendUserToRegisterActivity()
        }

        binding.loginBtn.setOnClickListener {
            allowUserToLogin()
        }

        binding.phoneLoginBtn.setOnClickListener {
            val phoneLoginIntent = Intent(this, PhoneLoginActivity::class.java)
            startActivity(phoneLoginIntent)
        }
    }

    private fun allowUserToLogin() {
        val email = binding.loginEmail.text.toString()
        val password = binding.loginPassword.text.toString()

        if (email.isEmpty()){
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()){
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show()
            return
        }
        loadingBar.setTitle("Sign In")
        loadingBar.setMessage("Please wait...")
        loadingBar.setCanceledOnTouchOutside(true)
        loadingBar.show()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
                FirebaseMessaging.getInstance().token.addOnCompleteListener {
                   deviceToken = it.result.toString()
                    if (currentUserID != null) {
                        usersRef.child(currentUserID).child("device_token")
                            .setValue(deviceToken)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    sendUserToMainActivity()
                                    Toast.makeText(this, "Logged in Successfully...", Toast.LENGTH_SHORT).show()
                                    loadingBar.dismiss()
                                }
                            }
                    }
                }



            }else{
                val message = task.exception.toString()
                Toast.makeText(this, "Error : $message", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }
        }
    }

    private fun sendUserToRegisterActivity() {
        val registerIntent = Intent(this, RegisterActivity::class.java)
        startActivity(registerIntent)
    }


    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        Firebase.currentUser = FirebaseAuth.getInstance().currentUser
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
}