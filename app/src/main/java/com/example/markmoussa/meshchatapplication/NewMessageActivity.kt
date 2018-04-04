package com.example.markmoussa.meshchatapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.hypelabs.hype.Hype
import com.hypelabs.hype.Instance
import com.hypelabs.hype.Message
import kotlinx.android.synthetic.main.activity_new_message.*
import java.io.UnsupportedEncodingException

class NewMessageActivity : AppCompatActivity(), Store.Delegate {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        val contactEditText = findViewById<EditText>(R.id.contactEditText) as EditText
        val chatBox = findViewById<EditText>(R.id.messageInputEditText) as EditText
        val sendButton = findViewById<Button>(R.id.sendNewMessageButton) as Button

        // Sending message
        sendButton.setOnClickListener {
            val text = chatBox.text.toString()
            if(text.isEmpty()) {
                return@setOnClickListener
            }
            try {
                val store = getStore()
                store.delegate = this
                store.lastReadIndex = store.getMessages()!!.size
                Log.v(this@NewMessageActivity::class.simpleName, "Send Message")
                val message = sendMessage(text, store.instance)
                chatBox.setText("")
                store.add(message, this)
            } catch(e: UnsupportedEncodingException) {
                e.printStackTrace()
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
            getStore()!!.lastReadIndex = getStore()!!.getMessages()!!.size
        }
    }

    // getting Store of this chat (which is where messages are stored)
    private fun getStore(): Store {

        val hypeFramework = applicationContext as HypeLifeCycle
        // have to manually search for the store based on the number user put in
        // this is hacky and probably won't work so if getting an error, this is where it is most likely
        // val storeIdentifier = intent.getStringExtra("StoreIdentifier")
        val storeIdentifier = contactEditText.text.toString()
        if(hypeFramework.getAllStores()[storeIdentifier] == null) {
            Log.e("No user found, NewMessageActivity", "Couldn't find user $storeIdentifier")

            /* TODO: CRITICAL - It looks like I can't import contacts since I have to find the people on the network first
             * so I should make a contacts list, and consult this link: https://hypelabs.io/community/topic/43/
             * to figure out how to make the identifiers persist
             */


        }
        return hypeFramework.getAllStores()[storeIdentifier]!!
    }
}
