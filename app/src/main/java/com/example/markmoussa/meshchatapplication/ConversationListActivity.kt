package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.hypelabs.hype.Message
import kotlinx.android.synthetic.main.item_conversation.*


class ConversationListActivity : AppCompatActivity(), Store.Delegate {

    // writing dummy data here for conversations. remove when figured out how to import conversations
    val user1: User = User("Mark", null)
    val user2: User = User("Marilyn", null)
    val user3: User = User("Marvin", null)
    val user4: User = User("Maged", null)

    val convo1: Conversation = Conversation(user1, null)
    val convo2: Conversation = Conversation(user2, null)
    val convo3: Conversation = Conversation(user3, null)
    val convo4: Conversation = Conversation(user4, null)

    // TODO: Replace with imported conversations when figured out how to do such
//    private var mConversationListList: List<Conversation> = listOf(convo1, convo2, convo3, convo4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)
        var mConversationListRecycler: RecyclerView? = null
        var mConversationListAdapter: ConversationListAdapter? = null
        mConversationListRecycler = findViewById<RecyclerView>(R.id.reyclerview_conversation_list)
        mConversationListRecycler!!.layoutManager = LinearLayoutManager(this)
        val hypeFramework = applicationContext as HypeLifeCycle
        mConversationListAdapter = ConversationListAdapter(this, hypeFramework.getAllStores().keys.toList())
        mConversationListRecycler.adapter = mConversationListAdapter

        // The onItemClick and onItemLongClick needs to be put here and not in the adapter since it's custom-defined and directly
        // relates to the RecyclerView itself
        ItemClickSupport.addTo(mConversationListRecycler).setOnItemClickListener(object : ItemClickSupport.OnItemClickListener {
            override fun onItemClicked(recyclerView: RecyclerView, position: Int, v: View) {
                // when item (conversation) is clicked, go to that conversation's message list
                Log.i("TEST: ", "This is a test, item number " + position.toString() + " picked.")
                val contactName = userNameTextView.text
                val hypeFramework = applicationContext as HypeLifeCycle
                val contactStore = hypeFramework.getAllStores()[contactName]
                contactStore?.delegate = this@ConversationListActivity
                val intent = Intent(this@ConversationListActivity, MessageListActivity::class.java)
                intent.putExtra("StoreIdentifier", contactStore?.instance?.stringIdentifier)
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
                        0 -> Log.i("CONVERSATION LONG CLICK ITEM PICKED: ", "ARCHIVE")
                        1 -> Log.i("CONVERSATION LONG CLICK ITEM PICKED: ", "DELETE")
                    }
                }

                val alert = messageOptionsDialog.create()
                alert.show()

                return true
            }
        })
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

    override fun onMessageAdded(store: Store, message: Message) {
        updateInterface()

    }

    private fun updateInterface() {
        this.runOnUiThread {
            val recyclerView = findViewById<RecyclerView>(R.id.reyclerview_conversation_list) as RecyclerView

            (recyclerView.adapter as ConversationListAdapter).notifyDataSetChanged()
        }
    }



}
