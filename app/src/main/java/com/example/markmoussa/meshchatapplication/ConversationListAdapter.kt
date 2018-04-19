package com.example.markmoussa.meshchatapplication

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.util.*

/**
 * Created by markmoussa on 2/24/18.
 */

class ConversationListAdapter(private val mContext: Context, private val mConversationList: List<Conversation>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return mConversationList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val conversation = mConversationList[position]

        (holder as ConversationHolder).bind(conversation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_conversation, parent, false)
        return ConversationHolder(view)

    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    private inner class ConversationHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var usernameText: TextView
        internal var messagePreviewText: TextView
        internal var timestampText: TextView
        internal var profilePicImage: ImageView
        internal var onlineStatusIcon: ImageView

        init {

            usernameText = itemView.findViewById(R.id.userNameTextView)
            messagePreviewText = itemView.findViewById(R.id.messagePreviewTextView)
            timestampText = itemView.findViewById(R.id.timestampTextView)
            profilePicImage = itemView.findViewById<ImageView>(R.id.profilePicImageView) as ImageView
            onlineStatusIcon = itemView.findViewById<ImageView>(R.id.onlineStatusIcon) as ImageView
        }
        internal fun bind(conversation: Conversation) {
             if(conversation.user!!.nickname != null) {
                 usernameText.text = conversation.user.nickname
             } else {
                 usernameText.text = conversation.user.userIdentifier.toString()
             }
            if(!(conversation.messageList?.getMessages()!!.isEmpty())) {

                var messagePreview = conversation.messageList.getMessageStringAtIndex(conversation.messageList.getMessages().size - 1)
                if(!(messagePreview.isNullOrEmpty()) && messagePreview!!.length > 40) {
                    messagePreview = messagePreview.substring(0, 33) + "..."
                }
                messagePreviewText.text = messagePreview
            } else {
                messagePreviewText.text = ""
            }
            // TODO: Find out how to get timestamp of last message from HypeLabs Messages
            //timeText.text = DateUtils.formatDateTime(message.getCreatedAt(), HOUR_IN_MILLIS, FORMAT_SHOW_TIME)
            timestampText.text = Date().toString()
            // TODO: Figure out how to pull profile pic, or if not, then what profile pic should be (if there should be one at all)
            if(conversation.user.profileUri != null) {
                // do nothing
            }
            val hypeFramework = mContext.applicationContext as HypeLifeCycle
            if(conversation.user.userIdentifier in hypeFramework.getOnlinePeers()) {
                onlineStatusIcon.setImageResource(android.R.drawable.presence_online)
            } else {
                onlineStatusIcon.setImageResource(android.R.drawable.presence_offline)
            }
        }
    }

}