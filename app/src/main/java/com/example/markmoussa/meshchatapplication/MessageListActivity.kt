package com.example.markmoussa.meshchatapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.hypelabs.hype.Message


class MessageListActivity : AppCompatActivity() {
    private var mMessageRecycler: RecyclerView? = null
    private var mMessageAdapter: MessageListAdapter? = null
    var mMessageList: List<Message> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)

        mMessageRecycler = findViewById<RecyclerView>(R.id.reyclerview_message_list) as RecyclerView
        mMessageAdapter = MessageListAdapter(this, mMessageList)
        mMessageRecycler!!.layoutManager = LinearLayoutManager(this)
    }
}
