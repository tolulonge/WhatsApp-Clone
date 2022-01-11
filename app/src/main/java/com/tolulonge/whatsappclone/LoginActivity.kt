package com.tolulonge.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseUser
import com.tolulonge.whatsappclone.databinding.ActivityLoginBinding
import com.tolulonge.whatsappclone.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {
    private var currentUser : FirebaseUser? = null
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.needNewAccontLink.setOnClickListener {
            sendUserToRegisterActivity()
        }
    }

    private fun sendUserToRegisterActivity() {
        val registerIntent = Intent(this, RegisterActivity::class.java)
        startActivity(registerIntent)
    }

    override fun onStart() {
        super.onStart()
        if (currentUser != null){
            sendUserToMainActivity()
        }
    }

    private fun sendUserToMainActivity() {
        val loginIntent = Intent(this, MainActivity::class.java)
        startActivity(loginIntent)
    }
}