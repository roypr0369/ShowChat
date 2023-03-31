package com.example.showchat.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.showchat.adapters.ChatAdapter
import com.example.showchat.databinding.ActivityChatBinding
import com.example.showchat.models.ChatMessage
import com.example.showchat.models.User
import com.example.showchat.network.ApiClient
import com.example.showchat.network.ApiService
import com.example.showchat.utilities.Constants
import com.example.showchat.utilities.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private var conversionId: String? = null
    private var isReceiverAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init(){
        preferenceManager = PreferenceManager(this)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            getBitmapFromEncodedString(receiverUser.image),
            preferenceManager.getString(Constants.KEY_USER_ID)!!
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage(){
        val message : HashMap<String?, Any?> = HashMap()
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)!!
        message[Constants.KEY_RECEIVER_ID] = receiverUser.id
        message[Constants.KEY_MESSAGE] = binding.inputMessage.text!!.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        if(conversionId != null){
            updateConversion(binding.inputMessage.text.toString())
        }else{
            val conversion: HashMap<String?, Any?> = HashMap()
            conversion[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID) ?: ""
            conversion[Constants.KEY_SENDER_NAME] = preferenceManager.getString(Constants.KEY_NAME) ?: ""
            conversion[Constants.KEY_SENDER_IMAGE] = preferenceManager.getString(Constants.KEY_IMAGE) ?: ""
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id) ?: ""
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name) ?: ""
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image) ?: ""
            conversion.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.text!!.toString()) ?: ""
            conversion.put(Constants.KEY_TIMESTAMP,Date()) ?: ""
            addConversion(conversion)
        }
        if(!isReceiverAvailable){
            try {
                val tokens = JSONArray()
                tokens.put(receiverUser.token)!!

                val data = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID)!!)
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME)!!)
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN)!!)
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.text.toString())

                val body = JSONObject()
                body.put(Constants.REMOTE_MSG_DATA, data)
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

                sendNotifications(body.toString())
            }catch (exception : Exception){
                showToast(exception.message!!)
            }
        }
        binding.inputMessage.text = null
    }

    private fun showToast(message: String?){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun sendNotifications(messageBody: String?){
        ApiClient.getClient()?.create(ApiService::class.java)?.sendMessage(
            Constants.getRemoteMsgHeadersBoo(),
            messageBody
        )?.enqueue(object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                if(response.isSuccessful){
                    try {
                        if(response.body() != null){
                            val responseJSON: JSONObject = JSONObject(response.body()!!)
                            val results : JSONArray = responseJSON.getJSONArray("results")
                            if(responseJSON.getInt("failure") == 1){
                                val error : JSONObject = results.get(0)!! as JSONObject
                                showToast(error.getString("error"))
                                return
                            }

                        }
                    }catch (e : JSONException){
                        e.printStackTrace()
                    }
                    showToast("Notification sent successfully")
                }else{
                    showToast("Error: " + response.code())
                }
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                showToast(t.message!!)
            }
        })
    }

    private fun listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
            receiverUser.id
        ).addSnapshotListener(this) { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    val availability = Objects.requireNonNull(
                        value.getLong(Constants.KEY_AVAILABILITY)!!
                            .toInt()
                    )
                    isReceiverAvailable = availability == 1
                }
                if(isReceiverAvailable){
                    receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN)!!
                }
                if(receiverUser.image == null){
                    receiverUser.image = value.getString(Constants.KEY_IMAGE)!!
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image))
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size)
                }
            }
            if(isReceiverAvailable){
                binding.textAvailability.visibility = View.VISIBLE
            }else{
                binding.textAvailability.visibility = View.GONE
            }
        }
    }

    private fun listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)!!)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID)!!)
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if(error != null){
            return@EventListener
        }
        if(value != null){
            val count = chatMessages.size
            for(documentChange in value.documentChanges){
                if(documentChange.type == DocumentChange.Type.ADDED){
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                    chatMessage.receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    chatMessage.message = documentChange.document.getString(Constants.KEY_MESSAGE)!!
                    chatMessage.dateTime = getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!)!!
                    chatMessage.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    chatMessages.add(chatMessage)
                }
            }
            chatMessages.sortWith(compareBy { it.dateObject })
            if(count == 0){
                chatAdapter.notifyDataSetChanged()
            }else{
                chatAdapter.notifyItemRangeChanged(chatMessages.size, chatMessages.size)
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
        if(conversionId == null){
            checkForConversion()
        }
    }

    private fun getBitmapFromEncodedString(encodedImage: String?) : Bitmap{
        return if(encodedImage != null){
            val bytes: ByteArray? = Base64.decode(encodedImage,Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes!!, 0, bytes.size)
        }else{
            null!!
        }
    }

    private fun loadReceiverDetails(){
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER)!! as User
        binding.textName.text = receiverUser.name
    }

    private fun setListeners(){
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.layoutSend.setOnClickListener{
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date?): String?{
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date!!)
    }

    private fun addConversion(conversion: HashMap<String?, Any?>){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .add(conversion)
            .addOnSuccessListener{
                conversionId = it.id
            }
    }

    private fun updateConversion(message: String?){
        val documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(conversionId!!)
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message!!,
            Constants.KEY_TIMESTAMP, Date()
        )
    }

    private fun checkForConversion(){
        if(chatMessages.size != 0){
            checkForConversionRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID)!!,
                receiverUser.id
            )
            checkForConversionRemotely(
                receiverUser.id,
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )
        }
    }

    private fun checkForConversionRemotely(senderId: String?, receiverId: String?){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId!!)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId!!)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

    private val conversionOnCompleteListener: OnCompleteListener<QuerySnapshot> =
        OnCompleteListener { task ->
            if(task.isSuccessful && task.result != null && task.result.documents.size > 0){
                val documentSnapshot: DocumentSnapshot = task.result.documents[0]
                conversionId = documentSnapshot.id
            }
        }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }
}