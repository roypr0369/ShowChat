package com.example.showchat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.showchat.databinding.ItemContainerUserBinding
import com.example.showchat.listeners.UserListener
import com.example.showchat.models.User

private lateinit var userListener: UserListener
class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder>{
    private lateinit var users: List<User>

    constructor()

    constructor(users: List<User>?, userListener1: UserListener?) {
        this.users = users!!
        userListener = userListener1!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemContainerUserBinding.inflate(
            LayoutInflater.from(parent.context!!),
            parent,
            false
        )
        return UserViewHolder(itemContainerUserBinding)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users[position])
    }

    class UserViewHolder(private val binding: ItemContainerUserBinding?): RecyclerView.ViewHolder(binding!!.root){
        fun setUserData(user: User?){
            binding!!.textName.text = user!!.name
            binding.textEmail.text = user.email
            binding.imageProfile.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener{
                userListener.onUserClicked(user)
            }
        }
        private fun getUserImage(encodedImage:String?):Bitmap?{
            val bytes = Base64.decode(encodedImage!!, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes!!,0,bytes.size)
        }
    }
    private fun getUserImage(encodedImage:String?):Bitmap?{
        val bytes = Base64.decode(encodedImage!!, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes!!,0,bytes.size)
    }
}