package com.tolulonge.whatsappclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.tolulonge.whatsappclone.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRegisterBinding
    private var mAuth : FirebaseAuth? = null
    private lateinit var loadingBar: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
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
            mAuth?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show()
                    loadingBar.dismiss()
                    sendUserToLoginActivity()
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
}