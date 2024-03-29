package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ProcessLifecycleOwner
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.hypelabs.hype.Message


class ConversationListActivity : AppCompatActivity(), Store.Delegate, LifecycleObserver {

    var mConversationList: MutableList<Conversation> = mutableListOf()
    private lateinit var lifeCycleObserver: LifecycleObserverActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        val hypeFramework = applicationContext as HypeLifeCycle

        // This tells us
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleObserverActivity(this).also { lifeCycleObserver = it})

        populateConversationList()

        var mConversationListRecycler: RecyclerView? = null
        var mConversationListAdapter: ConversationListAdapter? = null
        mConversationListRecycler = findViewById(R.id.reyclerview_conversation_list)
        mConversationListRecycler!!.layoutManager = LinearLayoutManager(this)
        mConversationListAdapter = ConversationListAdapter(this, mConversationList)
        mConversationListRecycler.adapter = mConversationListAdapter

        // The onItemClick and onItemLongClick needs to be put here and not in the adapter since it's custom-defined and directly
        // relates to the RecyclerView itself
        ItemClickSupport.addTo(mConversationListRecycler).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
            override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                // when item (conversation) is clicked, go to that conversation's message list
//                Log.i("TEST: ", "This is a test, item number " + position.toString() + " picked.")
                val userIdentifier = mConversationList[position].user?.userIdentifier
                val contactStore = hypeFramework.getAllMessages()[userIdentifier]
                contactStore?.delegate = this@ConversationListActivity
                val intent = Intent(this@ConversationListActivity, MessageListActivity::class.java)
                intent.putExtra("userIdentifier", userIdentifier)
                if(userIdentifier in hypeFramework.getOnlinePeers()) {
                    intent.putExtra("online", true)
                } else {
                    intent.putExtra("online", false)
                }
                startActivity(intent)
            }
        })

        ItemClickSupport.addTo(mConversationListRecycler).setOnItemLongClickListener(object : ItemClickSupport.OnItemLongClickListener {
            override fun onItemLongClicked(recyclerView: RecyclerView, position: Int, v: View): Boolean {
                // when item (conversation) is LONG clicked, give options to delete/archive conversations
                Log.i("TEST: ", "This is a test, item number " + position.toString() + " picked.")
                val options = arrayOf("Archive", "Delete")
                val messageOptionsDialog = AlertDialog.Builder(this@ConversationListActivity)
                messageOptionsDialog.setTitle("Options")
                messageOptionsDialog.setItems(options) { dialog, which ->
                    // do ya thang
                    when(which) {
                        0 -> Log.i("CONVERSATIONLONGCLICK: ", "ARCHIVE")
                        1 -> Log.i("CONVERSATIONLONGCLICK: ", "DELETE")
                    }
                }

                val alert = messageOptionsDialog.create()
                alert.show()

                return true
            }
        })

        // setting refresh listener in order to refresh list of online peers
        // TODO: Figure out how to have this list refresh immediately when a new user comes online.
        // TODO: for this, think the delegate method that Store implements
        val mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swiperefresh)
        mSwipeRefreshLayout.setOnRefreshListener {
            notifyOnlinePeersChanged()
            mSwipeRefreshLayout.isRefreshing = false
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_conversation_list, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when(item!!.itemId) {
//            R.id.newMessageMenuButton -> {
//                val intent = Intent(this, NewMessageActivity::class.java)
//                startActivity(intent)
//                return true
//            }
//            else -> return super.onOptionsItemSelected(item)
//        }
//    }

    // This function initializes conversation list for the first time.
    // Making separate function because this one returns the list, populateConversationList
    // modifies it (so that the changes can be detected by the adapter)
    private fun initializeConversationList(): MutableList<Conversation> {
        val hypeFramework = applicationContext as HypeLifeCycle
        val messageList = hypeFramework.getAllMessages()
        val contactsList = hypeFramework.getAllContacts()
        val onlinePeers = hypeFramework.getOnlinePeers()
        val conversationList = mutableListOf<Conversation>()
        for(x in messageList) {
            conversationList.add(Conversation(contactsList[x.key], null, x.value, if(contactsList.containsKey(x.key)) onlinePeers[x.key] else null, onlinePeers.containsKey(x.key)))
        }
        return conversationList
    }

    private fun populateConversationList() {
        // creating conversations list
        mConversationList.clear()
        val hypeFramework = applicationContext as HypeLifeCycle
        var currentlyOnline: Boolean
        val contactsList = hypeFramework.getAllContacts()
        val onlinePeers = hypeFramework.getOnlinePeers()
        Log.d("ConversationListActivit", "Length of hypeFramework.getAllMessages(): ${hypeFramework.getAllMessages().size}")
        for(x in hypeFramework.getAllMessages()) {
            currentlyOnline = onlinePeers.containsKey(x.key)
//            val nickname: String?
//            if(contactsList.containsKey(x.key)) {
//                // I get the actual user here. Consider just passing that into the conversation instead of
//                // creating a brand new one
//                nickname = contactsList[x.key]!!.nickname
////                Log.d("ConversationListActivit", "Nickname is: $nickname")
//            } else {
//                nickname = null
//            }
            mConversationList.add(Conversation(contactsList[x.key], null, x.value, if(currentlyOnline) onlinePeers[x.key] else null, currentlyOnline))
        }
        // Debugging
//        Log.d("ConversationListActivit", "populateConversationList() returned: ")
        for(x in mConversationList) {
            Log.d("ConversationListActivit", "User nickname: ${x.user?.nickname}, User Identifier: ${x.user?.userIdentifier}, user online: ${x.currentlyOnline}")
        }
    }


    // TODO: Add option to delete conversation


    override fun onMessageAdded(store: Store, message: Pair<String, Boolean>) {
        updateInterface()

    }

    fun notifyOnlinePeersChanged() {
        populateConversationList()
        updateInterface()
    }

    private fun updateInterface() {
        this.runOnUiThread {
            val recyclerView = findViewById<RecyclerView>(R.id.reyclerview_conversation_list) as RecyclerView

            (recyclerView.adapter as ConversationListAdapter).notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("sp", Context.MODE_PRIVATE)
        if(sharedPreferences.contains("USERNAME") && sharedPreferences.getString("USERNAME", null).isNotEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("Exit me", true)
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }



}
