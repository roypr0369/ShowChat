package com.example.showchat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.showchat.databinding.ItemContainerReceivedMessageBinding
import com.example.showchat.databinding.ItemContainerSentMessageBinding
import com.example.showchat.models.ChatMessage



class ChatAdapter( private var chatMessages: List<ChatMessage>,
                   private var receiverProfileImage: Bitmap,
                   private var senderId: String ) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    companion object{
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    fun setReceiverProfileImage(bitmap: Bitmap?){
        receiverProfileImage = bitmap!!
    }

    class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding?) : RecyclerView.ViewHolder(binding!!.root){

        fun setData(chatMessage: ChatMessage){
            binding!!.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime


        }

    }

    class ReceivedMessageViewHolder(private val binding: ItemContainerReceivedMessageBinding?) : RecyclerView.ViewHolder(binding!!.root){

        fun setData(chatMessage: ChatMessage?, receiverProfileImage: Bitmap?){
            binding!!.textMessage.text = chatMessage!!.message
            binding.textDateTime.text = chatMessage.dateTime
            if(receiverProfileImage != null){
                binding.imageProfile.setImageBitmap(receiverProfileImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_SENT){
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }else{
            return ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType((position)) == VIEW_TYPE_SENT){
            (holder as SentMessageViewHolder).setData(chatMessages[position])
        }else{
            (holder as ReceivedMessageViewHolder).setData(chatMessages[position], receiverProfileImage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(chatMessages[position].senderId == senderId){
            VIEW_TYPE_SENT
        }else{
            VIEW_TYPE_RECEIVED
        }
    }
}