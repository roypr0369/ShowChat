package com.example.showchat.listeners

import com.example.showchat.models.User

interface ConversionListener {
    fun onConversionClicked(user: User)
}