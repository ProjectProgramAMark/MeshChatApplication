package com.example.markmoussa.meshchatapplication

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

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

    fun add(message: Message) {

        getMessages()!!.add(message)

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
}