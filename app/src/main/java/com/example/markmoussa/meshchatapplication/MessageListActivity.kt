package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.hypelabs.hype.*
import java.io.UnsupportedEncodingException


class MessageListActivity : AppCompatActivity(), Store.Delegate {

    private var mMessageList: MutableList<Pair<String, Boolean>> = mutableListOf()
    private lateinit var mMessageAdapter: MessageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        val hypeFramework = applicationContext as HypeLifeCycle
        val userIdentifier = intent.getLongExtra("userIdentifier", 0)
        val contactsList = hypeFramework.getAllContacts()
        // Setting profileUri to default user image in case it doesn't exist
        // TODO: This way of setting the default profile pic might not be the best but it works; look into making more efficient later
        var profileUri: String? = "/Users/markmoussa/AndroidStudioProjects/MeshChatApplication/app/src/main/res/drawable/default_user_image.png"
        // Setting actionbar with name of user and getting profile pic path
        if(userIdentifier != 0.toLong()) {
            if(userIdentifier in contactsList) {
                val actionBar = supportActionBar
                actionBar!!.title = contactsList[userIdentifier]!!.nickname

                profileUri = contactsList[userIdentifier]?.profileUri
            }
        }
        mMessageAdapter = MessageListAdapter(this, mMessageList, profileUri)
        var mMessageRecycler: RecyclerView? = null
        mMessageRecycler = findViewById(R.id.recyclerview_message_list)
        mMessageRecycler.layoutManager = LinearLayoutManager(this)
        mMessageRecycler.adapter = mMessageAdapter
        // getting the messages
        notifyMessageListChanged()


        // hackish fix at the messages not appearing when notifyDataSetChanged() called. Need to fix later
        // also need to fix same thing for ConversationListActivity
        val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        mSwipeRefreshLayout.setOnRefreshListener {
            notifyMessageListChanged()
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        // checking to see which user chat they want, or if it's a new message

        val hypeFramework = applicationContext as HypeLifeCycle
        val userIdentifier = intent.getLongExtra("userIdentifier", 0)
        if(userIdentifier in hypeFramework.getAllMessages()) {
            val store = getStore()
            // debugging
            Log.d("MessageListActivity ", "Store is this: ")
            for(x in store.getMessages()) {
                Log.i("DEBUG", x.first)
            }
            store.delegate = this
            store.lastReadIndex = store.getMessages().size

            val chatBox = findViewById<EditText>(R.id.edittext_chatbox)
            val sendButton = findViewById<Button>(R.id.button_chatbox_send)

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
                    // Sending message via Hype
                    val onlinePeers = hypeFramework.getOnlinePeers()
                    if(onlinePeers.containsKey(userIdentifier)) {
                        // TODO: Add check right before here to see if user is still online
                        sendMessage(text, onlinePeers[userIdentifier]!!)
                        chatBox.setText("")
                        store.add(Pair(text, true), this)
                        // debugging
                        Log.d("MessageListActivity ", "Updated store is this: ")
                        for(x in store.getMessages()) {
                            Log.d("MessageListActivity", x.first)
                        }
                    } else {
                        // Alert the user that message cannot be sent (because onlinePeers does not have the Instance of the user)
                        val builder = AlertDialog.Builder(this@MessageListActivity)
                        builder.setMessage("Message cannot be sent to user at this time (they have likely just gone offline)")
                        builder.setPositiveButton("OK", {
                            _, _ ->  return@setPositiveButton
                        })
                        val dialog = builder.create()
                        dialog.show()
                    }
                } catch(e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun populateMessageList() {
        mMessageList.clear()
        mMessageList.addAll(getStore().getMessages().toMutableList())
        // Debugging
        Log.d("MessageListActivity: ", "populateMessageList() returned: ")
        if(!(mMessageList.isEmpty())) {
            for(x in mMessageList) {
                Log.d("MessageListActivity", "message: ${x.first}")
            }
        } else {
            Log.d("MessageListActivity ", "Message List is empty at the moment")
        }
    }

    private fun notifyMessageListChanged() {
        populateMessageList()
        updateInterface()
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
        return Hype.send(data, instance, true)
    }

    override fun onMessageAdded(store: Store, message: Pair<String, Boolean>) {
        Log.v("ONMESSAGEADDED: ", "onMessageAdded called in MessageListActivity")
        notifyMessageListChanged()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        if(!(getStore().getMessages().isEmpty())) {
            getStore().lastReadIndex = getStore().getMessages().size
        }
    }

    // getting Store of this chat (which is where messages are stored)
    private fun getStore(): Store {
        val hypeFramework = applicationContext as HypeLifeCycle
        val userIdentifier = intent.getLongExtra("userIdentifier", 0)
        for(x in hypeFramework.getAllMessages()[userIdentifier]!!.getMessages()) {
            Log.d("MessageListActivity", x.first)
        }
        return hypeFramework.getAllMessages()[userIdentifier]!!
    }

    private fun updateInterface() {
        this.runOnUiThread {
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_message_list) as RecyclerView

            (recyclerView.adapter as MessageListAdapter).notifyDataSetChanged()
        }
    }
}
