package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.content.Intent
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


class ConversationListActivity : AppCompatActivity(), Store.Delegate {

    lateinit var mConversationList: MutableList<Conversation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        mConversationList = populateConversationList()

        var mConversationListRecycler: RecyclerView? = null
        var mConversationListAdapter: ConversationListAdapter? = null
        mConversationListRecycler = findViewById<RecyclerView>(R.id.reyclerview_conversation_list)
        mConversationListRecycler!!.layoutManager = LinearLayoutManager(this)
        mConversationListAdapter = ConversationListAdapter(this, mConversationList)
        mConversationListRecycler.adapter = mConversationListAdapter

        // The onItemClick and onItemLongClick needs to be put here and not in the adapter since it's custom-defined and directly
        // relates to the RecyclerView itself
        ItemClickSupport.addTo(mConversationListRecycler).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
            override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                // when item (conversation) is clicked, go to that conversation's message list
                Log.i("TEST: ", "This is a test, item number " + position.toString() + " picked.")
                val userIdentifier = mConversationList[position].user?.userIdentifier
                val hypeFramework = applicationContext as HypeLifeCycle
                val contactStore = hypeFramework.getAllMessages()[userIdentifier]
                contactStore?.delegate = this@ConversationListActivity
                val intent = Intent(this@ConversationListActivity, MessageListActivity::class.java)
                intent.putExtra("userIdentifier", userIdentifier)
                if(userIdentifier in hypeFramework.getAllOnlinePeers()) {
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_conversation_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            R.id.newMessageMenuButton -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun populateConversationList(): MutableList<Conversation> {
        // creating conversations list
        val conversationList = mutableListOf<Conversation>()
        val hypeFramework = applicationContext as HypeLifeCycle
        var currentlyOnline: Boolean
        for(x in hypeFramework.getAllMessages()) {
            // TODO: Implement User nickname when I enable setUserIdentifier on Hype SDK
            currentlyOnline = x.key in hypeFramework.getAllOnlinePeers()
            val nickname: String?
            if(x.key in hypeFramework.getAllContacts()) {
                // I get the actual user here. Consider just passing that into the conversation instead of
                // creating a brand new one
                nickname = hypeFramework.getAllContacts()[x.key]!!.nickname
            } else {
                nickname = null
            }
            conversationList.add(Conversation(User(nickname, null, x.key), null, x.value, currentlyOnline))
        }
        return conversationList
    }

    override fun onMessageAdded(store: Store, message: Message) {
        updateInterface()

    }

    fun notifyOnlinePeersChanged() {
        mConversationList = populateConversationList()
        updateInterface()
    }

    private fun updateInterface() {
        this.runOnUiThread {
            val recyclerView = findViewById<RecyclerView>(R.id.reyclerview_conversation_list) as RecyclerView

            (recyclerView.adapter as ConversationListAdapter).notifyDataSetChanged()
        }
    }



}
