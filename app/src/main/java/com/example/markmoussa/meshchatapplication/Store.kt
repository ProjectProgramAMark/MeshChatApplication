package com.example.markmoussa.meshchatapplication

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

import android.content.Context
import com.hypelabs.hype.Instance
import com.hypelabs.hype.Message
import java.io.Serializable

import java.lang.ref.WeakReference
import java.util.Vector

class Store(val instance: Instance): Serializable {
    private var messages: Vector<Pair<Message, Boolean>> = Vector()
    var lastReadIndex: Int = 0
    private var delegateWeakReference: WeakReference<Delegate?>? = null

    var delegate: Delegate?
        get() = if (delegateWeakReference != null) delegateWeakReference!!.get() else null
        set(delegate) {

            this.delegateWeakReference = WeakReference(delegate)
        }

    interface Delegate {

        fun onMessageAdded(store: Store, message: Pair<Message, Boolean>)
    }

    init {
        this.lastReadIndex = 0
    }

    // need the context in order to be able to access setAllOnlinePeers function which lives in HypeLifeCycle
    // because Stores is a singleton class
    fun add(message: Pair<Message, Boolean>, context: Context) {

        getMessages().add(message)
        val hypeFramework = context.applicationContext as HypeLifeCycle
         hypeFramework.setMessageDatabase(instance.userIdentifier, this)
        val delegate = delegate

        delegate?.onMessageAdded(this, message)

    }

    fun getMessages(): Vector<Pair<Message, Boolean>> {
        return messages
    }

    fun hasNewMessages(): Boolean {

        return lastReadIndex < getMessages()!!.size
    }

    fun getMessageStringAtIndex(index: Int): String? {
        if(messages.isEmpty() || messages!!.size >= index) {
            // this means there's no message at this index and therefore need to return null
            return null
        }
        return messages[index].toString()

    }

    fun getMessageAtIndex(index: Int): Message {
        return messages[index].first
    }

    // TODO: write a function to update the stores value's last read index (here and in HypeLifeCycle)
}