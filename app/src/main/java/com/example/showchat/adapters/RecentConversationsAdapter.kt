package com.example.showchat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.showchat.databinding.ItemContainerRecentConversationBinding
import com.example.showchat.models.ChatMessage
import android.widget.ImageView
import com.example.showchat.listeners.ConversionListener
import com.example.showchat.models.User

class RecentConversationsAdapter(private val chatMessages: List<ChatMessage>,
                                 private val conversionListener: ConversionListener) : RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>(){


    inner class ConversionViewHolder(private val binding: ItemContainerRecentConversationBinding?) : RecyclerView.ViewHolder(binding!!.root){

        fun setData(chatMessage: ChatMessage){
            binding!!.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage))
            binding.textName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener {
                val user: User = User()
                user.id = chatMessage.conversionId
                user.name = chatMessage.conversionName
                user.image = chatMessage.conversionImage
                conversionListener.onConversionClicked(user)
            }
        }

        private fun getConversionImage(encodedImage: String?): Bitmap{
            val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        return ConversionViewHolder(
            ItemContainerRecentConversationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }
}