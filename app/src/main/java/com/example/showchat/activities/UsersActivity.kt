package com.example.showchat.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.showchat.adapters.UsersAdapter
import com.example.showchat.databinding.ActivityUsersBinding
import com.example.showchat.listeners.UserListener
import com.example.showchat.models.User
import com.example.showchat.utilities.Constants
import com.example.showchat.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UsersActivity : BaseActivity(), UserListener {

    private lateinit var binding: ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        setListeners()
        getUsers()
    }

    private fun setListeners(){
        binding.imageBack.setOnClickListener{
            onBackPressed()
        }
    }

    private fun getUsers(){
        loading(true)
        database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                loading(false)
                val currentUserId: String? = preferenceManager.getString(Constants.KEY_USER_ID)!!
                if(it.isSuccessful && it.result != null){
                    val users = emptyList<User>().toMutableList()
                    it.result.forEach{ queryDocumentSnapshot ->
                        Log.d("My_TAG", "fuck$currentUserId")
                        if(currentUserId!! == queryDocumentSnapshot!!.id){
                            return@forEach
                        }
                        val user = User()
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME)!!
                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL)!!
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)!!
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN) ?: ""
                        user.id = queryDocumentSnapshot.id
                        users.add(user)

                    }
                    if(users.size > 0){
                        val usersAdapter = UsersAdapter(users, this)
                        binding.usersRecyclerView.adapter = usersAdapter
                        binding.usersRecyclerView.visibility = View.VISIBLE
                    }else{
                        showErrorMessage()
                    }
                }else{
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage(){
        binding.textErrorMessage.text = String.format("%s", "No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean){
        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}
