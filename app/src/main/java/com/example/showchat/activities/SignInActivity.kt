package com.example.showchat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.showchat.databinding.ActivitySignInBinding
import com.example.showchat.utilities.Constants
import com.example.showchat.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var preferenceManager:PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            val intent = Intent(this, MainActivity::class.java)
            try {
                startActivity(intent)
            }catch (e: Exception){
                showToast("SignInActivity")
            }
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }
    private fun setListeners(){
        binding.textCreateNewAccount.setOnClickListener{
            val intent = Intent(this,SignUpActivity::class.java)
            try {
                startActivity(intent)
            }catch (e: Exception){
                showToast("SignInActivity")
            }
        }
        binding.buttonSignIn.setOnClickListener{
            if (isValidSignInDetails()){
                signIn()
            }
        }
    }

    private fun signIn(){
        loading(true)
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.text!!.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.text!!.toString())
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful && it.result != null && it.result!!.documents.size > 0){
                    val documentSnapshot = it.result.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot!!.id)
                    preferenceManager.putString(Constants.KEY_NAME,
                        documentSnapshot.getString(Constants.KEY_NAME)!!
                    )
                    preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE)!!)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    try {
                        startActivity(intent)
                    }catch (e: Exception){
                        showToast("SignInActivity")
                    }
                }else{
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
    }

    private fun loading(isLoading: Boolean){
        if(isLoading){
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String?){
        Toast.makeText(this, message!!, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignInDetails(): Boolean{
        return if(binding.inputEmail.text!!.toString().trim().isEmpty()){
            showToast("Enter email")
            false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text!!.toString()).matches()){
            showToast("Enter valid email")
            false
        }else if(binding.inputPassword.text!!.toString().trim().isEmpty()){
            showToast("Enter password")
            false
        }else{
            true
        }
    }
}