package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.hypelabs.hype.*
import java.io.UnsupportedEncodingException


class MessageListActivity : AppCompatActivity(), Store.Delegate {

    private val hypeFramework = application as HypeLifeCycle
    private val userIdentifier = intent.getLongExtra("userIdentifier", 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        // getting the messages
        val mMessageList: List<Message> = getStore().getMessages()!!.toList()
        var mMessageRecycler: RecyclerView? = null
        var mMessageAdapter: MessageListAdapter? = null
        mMessageRecycler = findViewById<RecyclerView>(R.id.recyclerview_message_list) as RecyclerView
        mMessageAdapter = MessageListAdapter(this, mMessageList)
        mMessageRecycler.layoutManager = LinearLayoutManager(this)
        mMessageRecycler.adapter = mMessageAdapter

        // Setting actionbar with name of user
        if(userIdentifier != 0.toLong()) {
            if(userIdentifier in hypeFramework.getAllContacts()) {
                val actionBar = actionBar
                actionBar.title = hypeFramework.getAllContacts()[userIdentifier]?.nickname
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // checking to see which user chat they want, or if it's a new message
        // TODO: made a new separate class for new messages for now, condense that into this class when I have everything set up
        if(userIdentifier in hypeFramework.getAllMessages()) {
            val store = getStore()
            store.delegate = this
            store.lastReadIndex = store.getMessages()!!.size

            val chatBox = findViewById<EditText>(R.id.edittext_chatbox) as EditText
            val sendButton = findViewById<Button>(R.id.button_chatbox_send) as Button

            if(!(intent.getBooleanExtra("online", true))) {
                chatBox.isEnabled = false
                sendButton.isEnabled = false
            }

            // Sending message
            sendButton.setOnClickListener {
                val text = chatBox.text.toString()
                if(text.isEmpty()) {
                    return@setOnClickListener
                }
                try {
                    Log.v(this@MessageListActivity::class.simpleName, "Send Message")
                    val message = sendMessage(text, store.instance)
                    chatBox.setText("")
                    store.add(message, this)
                } catch(e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        }

    }


    @Throws(UnsupportedEncodingException::class)
    private fun sendMessage(text: String, instance: Instance): Message {

        // When sending content there must be some sort of protocol that both parties
        // understand. In this case, we simply send the text encoded in UTF-8. The data
        // must be decoded when received, using the same encoding.
        val data = text.toByteArray(charset("UTF-8"))

        // Sends the data and returns the message that has been generated for it. Messages have
        // identifiers that are useful for keeping track of the message's deliverability state.
        // In order to track message delivery set the last parameter to true. Notice that this
        // is not recommend, as it incurs extra overhead on the network. Use this feature only
        // if progress tracking is really necessary.
        return Hype.send(data, instance, false)
    }

    override fun onMessageAdded(store: Store, message: Message) {
        this.runOnUiThread {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_message_list) as RecyclerView

            (recyclerView.adapter as MessageListAdapter).notifyDataSetChanged()
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if(getStore() != null) {
            getStore().lastReadIndex = getStore().getMessages()!!.size
        }
    }

    // getting Store of this chat (which is where messages are stored)
    private fun getStore(): Store {
        return hypeFramework.getAllMessages()[userIdentifier]!!
    }
}
