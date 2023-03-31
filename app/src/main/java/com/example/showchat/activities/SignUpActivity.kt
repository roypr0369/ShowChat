package com.example.showchat.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.showchat.databinding.ActivitySignUpBinding
import com.example.showchat.utilities.Constants
import com.example.showchat.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private var encodedImage: String? = null
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var  database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        setListeners()
    }

    private fun setListeners(){
        binding.textSignIn.setOnClickListener { onBackPressed() }
        binding.buttonSignUp.setOnClickListener{
            if(isValidSignUpDetails()){
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String?){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun signUp(){
        loading(true)
        database = FirebaseFirestore.getInstance()
        val user: HashMap<String?, Any?>
                = HashMap()
        user[Constants.KEY_NAME] = binding.inputName.text!!.toString()
        user[Constants.KEY_EMAIL] = binding.inputEmail.text!!.toString()
        user[Constants.KEY_PASSWORD] = binding.inputPassword.text!!.toString()
        user[Constants.KEY_IMAGE] = encodedImage
        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener {
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                preferenceManager.putString(Constants.KEY_NAME, binding.inputName.text!!.toString())
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                loading(false)
                showToast(it.message)

            }
    }

    private  fun encodedImage(bitmap: Bitmap?): String{
        val previewWidth = 150
        val previewHeight = bitmap!!.height * previewWidth / bitmap.width
        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

   private var pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
       result : ActivityResult ->
       if(result.resultCode == Activity.RESULT_OK){
           if(result.data != null){
               val imageUri = result.data!!.data!!
               try{
                   val inputStream = contentResolver.openInputStream(imageUri)
                   val bitmap = BitmapFactory.decodeStream(inputStream!!)
                   binding.imageProfile.setImageBitmap(bitmap!!)
                   binding.textAddImage.visibility = View.GONE
                   encodedImage = encodedImage(bitmap)
               }catch (e: FileNotFoundException){
                   e.printStackTrace()
               }
           }
       }
   }

    private fun isValidSignUpDetails(): Boolean{
        //point of error
        if(encodedImage == null){
            showToast("Select profile image")
            return false
        }else if(binding.inputName.text!!.toString().trim().isEmpty()){
            showToast("Enter name")
            return false
        }else if(binding.inputEmail.text!!.toString().trim().isEmpty()){
            showToast("Enter email")
            return false
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text!!.toString()).matches()){
            showToast("Enter valid email")
            return false
        }else if(binding.inputPassword.text!!.toString().trim().isEmpty()){
            showToast("Enter password")
            return false
        }else if(binding.inputConfirmPassword.text!!.toString().trim().isEmpty()){
            showToast("Confirm your password")
            return false
        }else if(binding.inputPassword.text!!.toString() != binding.inputConfirmPassword.text!!.toString()){
            showToast("Password and confirm password must be same")
            return false
        }
        return true
    }
    private fun loading(isLoading: Boolean){
        if(isLoading){
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }
    }
}