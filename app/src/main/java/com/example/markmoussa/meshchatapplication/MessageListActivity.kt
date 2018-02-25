package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView


class MessageListActivity : AppCompatActivity() {

    // TODO: Replace mMessageList with this declaration (change so that it actually imports Hype messages) when figured out how to use Hype
    // var mMessageList: List<Message> = emptyList()

    // writing dummy data here for messages. remove when figured out how to import messages
    private val dummyMessage1: DummyMessage = DummyMessage("Hello")
    private val dummyMessage2: DummyMessage = DummyMessage("World")
    private val dummyMessage3: DummyMessage = DummyMessage("How")
    private val dummyMessage4: DummyMessage = DummyMessage("Sweet")
    private val dummyMessage5: DummyMessage = DummyMessage("The")
    private val dummyMessage6: DummyMessage = DummyMessage("Sound")

    var mMessageList: List<DummyMessage> = listOf(dummyMessage1, dummyMessage2, dummyMessage3, dummyMessage4, dummyMessage5, dummyMessage6)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        var mMessageRecycler: RecyclerView? = null
        var mMessageAdapter: MessageListAdapter? = null
        mMessageRecycler = findViewById<RecyclerView>(R.id.reyclerview_message_list) as RecyclerView
        mMessageAdapter = MessageListAdapter(this, mMessageList)
        mMessageRecycler!!.layoutManager = LinearLayoutManager(this)
        mMessageRecycler.adapter = mMessageAdapter
    }
}
