package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.content.Context
import android.graphics.BitmapFactory
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.text.format.DateUtils.*
import android.util.Log
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.library.bubbleview.BubbleTextView
import com.hypelabs.hype.Message
import java.io.ByteArrayInputStream


// BaseMessage is specific to SendBird, fix later
class MessageListAdapter(private val mContext: Context, private val mMessageList: List<Pair<String, Boolean>>, private val profileUri: String?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

        return if (message.second) {
            // If the current user is the sender of the message
            VIEW_TYPE_MESSAGE_SENT
        } else {
            // If some other user sent the message
            VIEW_TYPE_MESSAGE_RECEIVED
        }

    }

    // Inflates the appropriate layout according to the ViewType.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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

        // Not supposed to be this but it's whatever I'll fix later
        return SentMessageHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false))
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        // TODO: UserMessage is specific to SendBird, fix later
        val message = mMessageList[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> (holder as SentMessageHolder).bind(message.first)
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageHolder).bind(message.first)
        }
    }

    private inner class SentMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var messageText: BubbleTextView = itemView.findViewById(R.id.bubbleTextView)
        internal var timeText: TextView = itemView.findViewById(R.id.text_message_time)

        // UserMessage is specific to SendBird, fix later
        internal fun bind(message: String) {
            messageText.text = message
            Log.i("DEBUG ", "Message text from adapter: ${messageText.text}")

            // Format the stored timestamp into a readable String using method.
            // Utils specific to SendBird, fix later
//            timeText.setText(Utils.formatDateTime(message.getCreatedAt()))
        }
    }

    private inner class ReceivedMessageHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var messageText: BubbleTextView = itemView.findViewById(R.id.bubbleTextView)
        internal var timeText: TextView = itemView.findViewById(R.id.text_message_time)
        internal var profileImage: ImageView = itemView.findViewById(R.id.image_message_profile) as ImageView

        
        // UserMessage is specific to SendBird, fix later

        internal fun bind(message: String) {
            messageText.text = message
            Log.i("DEBUG ", "Message text from adapter: ${messageText.text}")
            if(profileUri == null) {
                profileImage.setImageResource(R.drawable.default_user_image)
            } else {
                profileImage.setImageBitmap(BitmapFactory.decodeFile(profileUri))
            }

            // Format the stored timestamp into a readable String using method.
            // TODO: Replace HOUR_IN_MILLIS and FORMAT_SHOW_TIME to get metadata from Hype Messages
            // TODO: Hype Messages do not have a createdAt feature. Figure out what to do
            //timeText.text = DateUtils.formatDateTime(message.getCreatedAt(), HOUR_IN_MILLIS, FORMAT_SHOW_TIME)

            //nameText.setText(message.getSender().getNickname())

            // TODO: Figure out how to do this without using SendBird
            // Insert the profile image from the URL into the ImageView.
            // ImageUtils.displayRoundImageFromUrl(mContext, message.getSender().getProfileUri(), profileImage)
        }
    }

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }
}