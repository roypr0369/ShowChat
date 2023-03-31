package com.example.showchat.listeners

import com.example.showchat.models.User

interface UserListener {
    fun onUserClicked(user: User)
}