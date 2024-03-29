package com.example.markmoussa.meshchatapplication

import com.hypelabs.hype.Instance
import java.util.*

/**
 * Created by markmoussa on 2/24/18.
 */


// This version with a constructor used simply so we can generate dummy data to populate conversations
class Conversation(val user: User?, val timeStamp: Date?, val messageList: Store?, val instance: Instance?, val currentlyOnline: Boolean)