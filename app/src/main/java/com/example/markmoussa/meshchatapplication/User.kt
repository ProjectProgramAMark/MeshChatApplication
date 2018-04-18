package com.example.markmoussa.meshchatapplication

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

// This version with a constructor used simply so we can generate dummy data to populate conversations
data class User(val nickname: String?, val profileUri: String?, val userIdentifier: Long?): Serializable {
    private var profilePicBitmap: Bitmap? = null
    // This secondary constructor is used when we need to send the profilePic itself to another user to be able to store it
    constructor(nickname: String?, profileUri: String?, userIdentifier: Long?, profilePicBitmap: Bitmap?) : this(nickname, profileUri, userIdentifier) {
        this.profilePicBitmap = profilePicBitmap
    }

    override fun toString(): String {
        return "{ nickname: $nickname; profileUri: $profileUri; userIdentifier: $userIdentifier; profilePicBitmap: $profilePicBitmap }"
    }

    fun serializeUser(): ByteArray {
        val byteOut = ByteArrayOutputStream()
        val oos = ObjectOutputStream(byteOut)
        oos.writeObject(this)
        return byteOut.toByteArray()
    }
}
