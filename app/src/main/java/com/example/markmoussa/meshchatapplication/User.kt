package com.example.markmoussa.meshchatapplication

import android.graphics.Bitmap
import com.hypelabs.hype.Instance

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

// TODO: Use this version once figured out how to import conversations and remove dummy data
//class User {
//    var nickname: String? = null
//    var profileUrl: String? = null
//}

// This version with a constructor used simply so we can generate dummy data to populate conversations
data class User(val nickname: String?, val profileUrl: String?, val userIdentifier: Long?) {
    // This secondary constructor is used when we need to send the profilePic itself to another user to be able to store it
    constructor(nickname: String?, profileUrl: String?, userIdentifier: Long?, profilePicBitmap: Bitmap?) : this(nickname, profileUrl, userIdentifier)
}
