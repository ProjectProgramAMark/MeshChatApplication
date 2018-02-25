package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class ConversationListActivity : AppCompatActivity() {

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
    private var mConversationListList: List<Conversation> = listOf(convo1, convo2, convo3, convo4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)
        var mConversationListRecycler: RecyclerView? = null
        var mConversationListAdapter: ConversationListAdapter? = null
        mConversationListRecycler = findViewById<RecyclerView>(R.id.reyclerview_conversation_list)
        mConversationListRecycler!!.layoutManager = LinearLayoutManager(this)
        mConversationListAdapter = ConversationListAdapter(this, mConversationListList)
        mConversationListRecycler.adapter = mConversationListAdapter
    }
}
