package com.example.markmoussa.meshchatapplication

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

import android.content.Context
import com.hypelabs.hype.Instance
import com.hypelabs.hype.Message

import java.lang.ref.WeakReference
import java.util.Vector

class Store(val instance: Instance) {
    private var messages: Vector<Message>? = null
    var lastReadIndex: Int = 0
    private var delegateWeakReference: WeakReference<Delegate?>? = null

    var delegate: Delegate?
        get() = if (delegateWeakReference != null) delegateWeakReference!!.get() else null
        set(delegate) {

            this.delegateWeakReference = WeakReference(delegate)
        }

    interface Delegate {

        fun onMessageAdded(store: Store, message: Message)
    }

    init {
        this.lastReadIndex = 0
    }

    // need the context in order to be able to access setStores function which lives in HypeLifeCycle
    // because Stores is a singleton class
    fun add(message: Message, context: Context) {

        getMessages()!!.add(message)
        val hypeFramework = context.applicationContext as HypeLifeCycle
         hypeFramework.setStores(instance.stringIdentifier, this)
        val delegate = delegate

        delegate?.onMessageAdded(this, message)

    }

    fun getMessages(): Vector<Message>? {
        if (messages == null) {
            messages = Vector()
        }

        return messages
    }

    fun hasNewMessages(): Boolean {

        return lastReadIndex < getMessages()!!.size
    }

    // TODO: write a function to update the stores value's last read index (here and in HypeLifeCycle)
}