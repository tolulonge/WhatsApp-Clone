package com.tolulonge.whatsappclone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.tolulonge.whatsappclone.databinding.ActivityPhoneLoginBinding
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.PhoneAuthOptions




class PhoneLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneLoginBinding
    private lateinit var callbacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mVerificationId : String
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var loadingBar : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingBar = ProgressDialog(this)

        binding.sendVerCodeButton.setOnClickListener {
            binding.apply {
                sendVerCodeButton.visibility = View.INVISIBLE
                phoneNumberInput.visibility = View.INVISIBLE

                verifiyButton.visibility = View.VISIBLE
                verificationCodeInput.visibility = View.VISIBLE

                val phoneNumber = phoneNumberInput.text.toString()
                if (phoneNumber.isEmpty()){
                    Toast.makeText(
                        this@PhoneLoginActivity,
                        "Please enter your phone number first...",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    loadingBar.setTitle("Phone Verification")
                    loadingBar.setMessage("Please wait, while we are authenticating your phone...")
                    loadingBar.setCanceledOnTouchOutside(false)
                    loadingBar.show()

                    val options = PhoneAuthOptions.newBuilder(Firebase.mAuth)
                        .setPhoneNumber(phoneNumber) // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this@PhoneLoginActivity) // Activity (for callback binding)
                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                        .build()
                    PhoneAuthProvider.verifyPhoneNumber(options)
                }
            }
        }

        binding.verifiyButton.setOnClickListener {
            binding.apply {
                sendVerCodeButton.visibility = View.INVISIBLE
                phoneNumberInput.visibility = View.INVISIBLE

                val verificationCode = verificationCodeInput.text.toString()
                if (verificationCode.isEmpty()){
                    Toast.makeText(
                        this@PhoneLoginActivity,
                        "Please write verification code first...",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    loadingBar.setTitle("Verification Code")
                    loadingBar.setMessage("Please wait, while we are verifying your verification code...")
                    loadingBar.setCanceledOnTouchOutside(false)
                    loadingBar.show()
                    val credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode)
                    signInWithPhoneAuthCredential(credential)
                }
            }
        }


        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                loadingBar.dismiss()
                Toast.makeText(
                    this@PhoneLoginActivity,
                    "Invalid Phone Number, Please enter correct phone number with country code...",
                    Toast.LENGTH_SHORT
                ).show()

                binding.apply {
                    sendVerCodeButton.visibility = View.VISIBLE
                    phoneNumberInput.visibility = View.VISIBLE

                    verifiyButton.visibility = View.INVISIBLE
                    verificationCodeInput.visibility = View.INVISIBLE
                }
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                mVerificationId = p0
                mResendToken = p1

                loadingBar.dismiss()
                Toast.makeText(
                    this@PhoneLoginActivity,
                    "Code has been sent, please check and verify...",
                    Toast.LENGTH_SHORT
                ).show()

                binding.apply {
                    sendVerCodeButton.visibility = View.INVISIBLE
                    phoneNumberInput.visibility = View.INVISIBLE

                    verifiyButton.visibility = View.VISIBLE
                    verificationCodeInput.visibility = View.VISIBLE
                }
            }

        }
    }

    private fun signInWithPhoneAuthCredential(credential : PhoneAuthCredential){
        Firebase.mAuth.signInWithCredential(credential).addOnCompleteListener(this
        ) {
            if (it.isSuccessful){
                loadingBar.dismiss()
                Toast.makeText(
                    this,
                    "Congratulations, you're logged in successfully...",
                    Toast.LENGTH_SHORT
                ).show()
                sendUserToMainActivity()
            }else{
                loadingBar.dismiss()
                val message = it.exception.toString()
                Toast.makeText(this, "Error : $message", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }
}