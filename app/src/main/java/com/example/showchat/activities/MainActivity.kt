package com.example.showchat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.showchat.adapters.RecentConversationsAdapter
import com.example.showchat.databinding.ActivityMainBinding
import com.example.showchat.listeners.ConversionListener
import com.example.showchat.models.ChatMessage
import com.example.showchat.models.User
import com.example.showchat.utilities.Constants
import com.example.showchat.utilities.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), ConversionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations: MutableList<ChatMessage>
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenConversions()
    }

    private fun init(){
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations, this)
        binding.conversationsRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListeners(){
        binding.imageSignOut.setOnClickListener{
            signOut()
        }
        binding.fabNewChat.setOnClickListener{
            val intent = Intent(this, UsersActivity::class.java)
            try {
                startActivity(intent)
            }catch(e : Exception){
                showToast("UserActivity")
            }
        }
    }
    private fun loadUserDetails(){
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)!!
        val bytes = Base64.decode(
            preferenceManager.getString(Constants.KEY_IMAGE)!!,
            Base64.DEFAULT
        )
        val bitmap = BitmapFactory.decodeByteArray(bytes!!,0, bytes.size)!!
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String?){
        Toast.makeText(this, message!!, Toast.LENGTH_SHORT).show()
    }

    private fun listenConversions(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if(error != null){
            return@EventListener
        }
        if(value != null){
            for(documentChange in value.documentChanges){
                if(documentChange.type == DocumentChange.Type.ADDED){
                    val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                    val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = senderId
                    chatMessage.receiverId = receiverId
                    if(preferenceManager.getString(Constants.KEY_USER_ID)!! == senderId){
                        chatMessage.conversionImage = documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)!!
                        chatMessage.conversionName = documentChange.document.getString(Constants.KEY_RECEIVER_NAME)!!
                        chatMessage.conversionId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    }else{
                        chatMessage.conversionImage = documentChange.document.getString(Constants.KEY_SENDER_IMAGE)!!
                        chatMessage.conversionName = documentChange.document.getString(Constants.KEY_SENDER_NAME)!!
                        chatMessage.conversionId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                    }
                    chatMessage.message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                    chatMessage.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    conversations.add(chatMessage)
                }else if(documentChange.type == DocumentChange.Type.MODIFIED){
                    for(i in conversations.indices){
                        val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                        val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                        if(conversations[i].senderId == senderId && conversations[i].receiverId == receiverId){
                            conversations[i].message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                            conversations[i].dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                            break
                        }
                    }
                }
            }
            conversations.sortWith(compareBy { -it.dateObject.time })
            conversationsAdapter.notifyDataSetChanged()
            binding.conversationsRecyclerView.smoothScrollToPosition(0)
            binding.conversationsRecyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun getToken(){
        FirebaseMessaging.getInstance().token.addOnSuccessListener{
            updateToken(it)
        }
    }

    private fun updateToken(token: String?){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token!!)
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)!!
        )

        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener {
                showToast("Unable to update token")
            }
    }

    private fun signOut(){
        showToast(("Signing out..."))
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)!!
        )
        val updates: HashMap<String?, Any?> = HashMap()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener{
                preferenceManager.clear()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                showToast("Unable to sign out")
            }
    }

    override fun onConversionClicked(user: User) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}