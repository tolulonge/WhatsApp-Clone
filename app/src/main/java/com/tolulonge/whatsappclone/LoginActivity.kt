package com.tolulonge.whatsappclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tolulonge.whatsappclone.databinding.ActivityLoginBinding
import com.tolulonge.whatsappclone.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var mAuth : FirebaseAuth? = null
    private lateinit var loadingBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        loadingBar = ProgressDialog(this)

        binding.needNewAccontLink.setOnClickListener {
            sendUserToRegisterActivity()
        }

        binding.loginBtn.setOnClickListener {
            allowUserToLogin()
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
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener {
            if(it.isSuccessful){
                sendUserToMainActivity()
                Toast.makeText(this, "Logged in Successfully...", Toast.LENGTH_SHORT).show()
                loadingBar.dismiss()
            }else{
                val message = it.exception.toString()
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
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }
}