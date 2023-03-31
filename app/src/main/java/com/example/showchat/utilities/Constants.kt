package com.example.showchat.utilities

class Constants {
    companion object{
        const val KEY_COLLECTION_USERS: String = "users"
        const val KEY_NAME: String = "name"
        const val KEY_EMAIL: String = "email"
        const val KEY_PASSWORD: String = "password"
        const val KEY_PREFERENCE_NAME: String = "chatAppPreference"
        const val KEY_IS_SIGNED_IN: String = "isSignedIn"
        const val KEY_USER_ID: String = "userId"
        const val KEY_IMAGE: String = "image"
        const val KEY_FCM_TOKEN: String = "fcmToken"
        const val KEY_USER: String = "user"
        const val KEY_COLLECTION_CHAT: String = "chat"
        const val KEY_SENDER_ID: String = "senderId"
        const val KEY_RECEIVER_ID:String = "receiverId"
        const val KEY_MESSAGE: String = "message"
        const val KEY_TIMESTAMP: String = "timestamp"
        const val KEY_COLLECTION_CONVERSATION: String = "conversations"
        const val KEY_SENDER_NAME: String = "senderName"
        const val KEY_RECEIVER_NAME: String = "receiverName"
        const val KEY_SENDER_IMAGE: String = "senderImage"
        const val KEY_RECEIVER_IMAGE : String = "receiverImage"
        const val KEY_LAST_MESSAGE : String = "lastMessage"
        const val KEY_AVAILABILITY : String = "availability"
        const val REMOTE_MSG_AUTHORIZATION: String = "Authorization"
        const val REMOTE_MSG_CONTENT_TYPE: String = "Content-Type"
        const val REMOTE_MSG_DATA: String = "data"
        const val REMOTE_MSG_REGISTRATION_IDS: String = "registration_ids"

        var remoteMsgHeaders: HashMap<String, String>? = null
        fun getRemoteMsgHeadersBoo(): HashMap<String, String>{
            if(remoteMsgHeaders == null){
                remoteMsgHeaders = HashMap()
                remoteMsgHeaders!!.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAgYflS1s:APA91bEGwk7YnRw6_QtcPM8rUOWB7-Mokq7zqCmMa2RNl5QSFO-JRJmWkH6RYfvBY8QBmwshgXQMcYdmgJn_8kjSjphoFz2AG_tYiuYELFWXCERxt6uzvsOkwJ-fYtfBCbg782HuhHpg"
                )
                remoteMsgHeaders!!.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
                )
            }
            return remoteMsgHeaders!!
        }
    }
}