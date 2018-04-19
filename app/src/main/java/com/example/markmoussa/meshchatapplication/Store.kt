package com.example.markmoussa.meshchatapplication

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

import android.content.Context
import android.util.Log
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.Vector

class Store(val userIdentifier: Long): Serializable {


    // The Boolean in the Pair represents whether that message was sent from host or user
    // true = sent from host (aka your message) false = sent from other user (the one you're chatting with)
    // The first value, String, used to be Hype Message object, but since it's not serializable for now
    // I have to replace it with simply the text, since that means Message cannot be saved to a file
    private var messages: Vector<Pair<String, Boolean>> = Vector()
    var lastReadIndex: Int = 0
    // Just a hotfix, and have no idea how this will affect the program
    // but going to make WeakReference transient
    @Transient private var delegateWeakReference: WeakReference<Delegate?>? = null

    var delegate: Delegate?
        get() = if (delegateWeakReference != null) delegateWeakReference!!.get() else null
        set(delegate) {

            this.delegateWeakReference = WeakReference(delegate)
        }

    interface Delegate {

        fun onMessageAdded(store: Store, message: Pair<String, Boolean>)
    }

    init {
        this.lastReadIndex = 0
    }

    // need the context in order to be able to access setMessageDatabase function which lives in HypeLifeCycle
    // because Stores is a singleton class
    fun add(message: Pair<String, Boolean>, context: Context) {
        getMessages().add(message)
        val hypeFramework = context.applicationContext as HypeLifeCycle
         hypeFramework.setMessageDatabase(userIdentifier, this)
        val delegate = delegate

        delegate?.onMessageAdded(this, message)

    }

    fun getMessages(): Vector<Pair<String, Boolean>> {
        return messages
    }

    fun hasNewMessages(): Boolean {

        return lastReadIndex < getMessages()!!.size
    }

    fun getMessageStringAtIndex(index: Int): String? {
        if(messages.isEmpty() || messages!!.size < index) {
            Log.d("Store", "this was activated")
            // this means there's no message at this index and therefore need to return null
            return null
        }
        return messages[index].first

    }

    fun getMessageAtIndex(index: Int): String {
        return messages[index].first
    }

    // TODO: write a function to update the stores value's last read index (here and in HypeLifeCycle)
}