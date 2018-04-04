package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.text.format.DateUtils.*
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.hypelabs.hype.Message


// BaseMessage is specific to SendBird, fix later
class MessageListAdapter(private val mContext: Context, private val mMessageList: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return mMessageList.size
    }

    // Determines the appropriate ViewType according to the sender of the message.
    override fun getItemViewType(position: Int): Int {
        val message = mMessageList[position]

        // Doesn't look like the feature for getting who sent the message is there yet
        /* TODO: Fill this space with the correct logic (it must choose the view type based on
         * if sending or receiving message. But Hype Messages doesn't look like it has that feature
         * yet
         * For now its just 0 and 1 to default to message received
         */
        // SendBird function
        // message.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())
        return if (true) {
            // If the current user is the sender of the message
            VIEW_TYPE_MESSAGE_SENT
        } else {
            // If some other user sent the message
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view: View

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
            return SentMessageHolder(view)
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
            return ReceivedMessageHolder(view)
        }

        return null
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        // TODO: UserMessage is specific to SendBird, fix later
        val message = mMessageList[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message)
        }
    }

    private inner class SentMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView
        internal var timeText: TextView

        init {

            messageText = itemView.findViewById(R.id.text_message_body)
            timeText = itemView.findViewById(R.id.text_message_time)
        }

        // UserMessage is specific to SendBird, fix later
        internal fun bind(message: Message) {
            messageText.text = message.data.toString()

            // Format the stored timestamp into a readable String using method.
            // Utils specific to SendBird, fix later
//            timeText.setText(Utils.formatDateTime(message.getCreatedAt()))
        }
    }

    private inner class ReceivedMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: TextView
        internal var timeText: TextView
        internal var nameText: TextView
        internal var profileImage: ImageView

        init {

            messageText = itemView.findViewById(R.id.text_message_body)
            timeText = itemView.findViewById(R.id.text_message_time)
            nameText = itemView.findViewById(R.id.text_message_name)
            profileImage = itemView.findViewById<ImageView>(R.id.image_message_profile) as ImageView
        }
        // UserMessage is specific to SendBird, fix later

        internal fun bind(message: Message) {
            messageText.text = message.data.toString()

            // Format the stored timestamp into a readable String using method.
            // TODO: Replace HOUR_IN_MILLIS and FORMAT_SHOW_TIME to get metadata from Hype Messages
            // TODO: Hype Messages do not have a createdAt feature. Figure out what to do
            //timeText.text = DateUtils.formatDateTime(message.getCreatedAt(), HOUR_IN_MILLIS, FORMAT_SHOW_TIME)

            //nameText.setText(message.getSender().getNickname())

            // TODO: Figure out how to do this without using SendBird
            // Insert the profile image from the URL into the ImageView.
            // ImageUtils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUrl(), profileImage)
        }
    }

    companion object {
        private val VIEW_TYPE_MESSAGE_SENT = 1
        private val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }
}