package com.example.markmoussa.meshchatapplication

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.hypelabs.hype.Error
import com.hypelabs.hype.Hype
import com.hypelabs.hype.Instance
import com.hypelabs.hype.Message
import com.hypelabs.hype.MessageInfo
import com.hypelabs.hype.MessageObserver
import com.hypelabs.hype.NetworkObserver
import com.hypelabs.hype.StateObserver
import org.json.JSONObject
import java.io.*
import java.nio.charset.Charset
import kotlin.collections.HashMap
import kotlin.properties.Delegates


class HypeLifeCycle : StateObserver, NetworkObserver, MessageObserver, Application() {

    // The onlinePeers object keeps track of message storage associated with each instance (peer)
    var onlinePeers: MutableList<Long> by Delegates.observable(mutableListOf()) {
        _, _, _ -> updateOnlinePeersFile()
    }

    private var messageDatabase: HashMap<Long, Store> by Delegates.observable(hashMapOf()) {
        _, _, _ -> updateMessageDatabase()
    }

    private var contactsDatabase: HashMap<Long, User> by Delegates.observable(hashMapOf()) {
        _, _, _ -> updateContactsDatabase()
    }

    private lateinit var dirPath: File

    fun requestHypeToStart() {
        Hype.setContext(applicationContext)

        // Add this as an Hype observer
        Hype.addStateObserver(this)
        Hype.addNetworkObserver(this)
        Hype.addMessageObserver(this)

        // Generate an app identifier in the HypeLabs dashboard (https://hypelabs.io/apps/),
        // by creating a new app. Copy the given identifier here.
        Hype.setAppIdentifier("f370ac17")
        val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("sp", Context.MODE_PRIVATE)
        val userIdentifier = sharedPreferences.getInt("USER_IDENTIFIER", Hype.DefaultUserIdentifier)
        Hype.setUserIdentifier(userIdentifier)
        Hype.start()
    }

    fun requestHypeToStop() {
        // The current release has a known issue with Bluetooth Low Energy that causes all
        // connections to drop when the SDK is stopped. This is an Android issue.
        Hype.stop()
    }

    override fun onHypeStart() {
        Log.i(TAG, "Hype started!")
        Log.i(TAG, "Loading store from file")
        readOnlinePeers()
        readMessageDatabase()
    }

    override fun onHypeStop(error: Error?) {

        var description = ""

        if (error != null) {
            description = String.format("[%s]", error.description)
        }

        Log.i(TAG, String.format("Hype stopped [%s]", description))
        readOnlinePeers()
    }

    override fun onHypeFailedStarting(error: Error) {

        Log.i(TAG, String.format("Hype failed starting [%s]", error.description))
    }

    override fun onHypeReady() {

        Log.i(TAG, String.format("Hype is ready"))

        requestHypeToStart()
    }

    override fun onHypeStateChange() {

        Log.i(TAG, String.format("Hype changed state to [%d] (Idle=0, Starting=1, Running=2, Stopping=3)", Hype.getState().value))
    }

    internal fun shouldResolveInstance(instance: Instance): Boolean {
        // This method can be used to decide whether an instance is interesting
        return true
    }

    override fun onHypeInstanceFound(instance: Instance) {

        Log.i(TAG, String.format("Hype found instance: %s", instance.stringIdentifier))

        if (shouldResolveInstance(instance)) {
            Hype.resolve(instance)
        }
    }

    override fun onHypeInstanceLost(instance: Instance, error: Error) {

        Log.i(TAG, String.format("Hype lost instance: %s [%s]", instance.stringIdentifier, error.description))
        removeFromResolvedInstancesMap(instance)
    }

    override fun onHypeInstanceResolved(instance: Instance) {

        Log.i(TAG, String.format("Hype resolved instance: %s", instance.stringIdentifier))

        // This device is now capable of communicating
        addToResolvedInstancesMap(instance)
    }

    override fun onHypeInstanceFailResolving(instance: Instance, error: Error) {

        Log.i(TAG, String.format("Hype failed resolving instance: %s [%s]", instance.stringIdentifier, error.description))
    }

    override fun onHypeMessageReceived(message: Message, instance: Instance) {

        Log.i(TAG, String.format("Hype got a message from: %s", instance.stringIdentifier))

        val store = getAllMessages()[instance.userIdentifier]
        if(store != null) {
            setMessageDatabase(instance.userIdentifier, store)
        } else {
            setMessageDatabase(instance.userIdentifier, Store(instance))
        }
        // Storing the message triggers a reload update in the MessageList activity
        store!!.add(message, this)

        // Notify the conversationList activity to refresh the UI
//        val conversationListActivity = ConversationListActivity()
//        conversationListActivity.notifyOnlinePeersChanged()
    }

    override fun onHypeMessageFailedSending(messageInfo: MessageInfo, instance: Instance, error: Error) {

        Log.i(TAG, String.format("Hype failed to send message: %d [%s]", messageInfo.identifier, error.description))
    }

    override fun onHypeMessageSent(messageInfo: MessageInfo, instance: Instance, progress: Float, done: Boolean) {

        Log.i(TAG, String.format("Hype is sending a message: %f", progress))
        while(!done) {
            Log.i(TAG, "Testing. If infinite loop, then Hype message never sent.")
        }
        Log.i(TAG, "Hype message successfully sent!")
    }

    override fun onHypeMessageDelivered(messageInfo: MessageInfo, instance: Instance, progress: Float, done: Boolean) {

        Log.i(TAG, String.format("Hype delivered a message: %f", progress))
        while(!done) {}
        Log.i(TAG, "Hype message successfully delivered!")
    }

    override fun onHypeRequestAccessToken(i: Int): String {
        // Access the app settings (https://hypelabs.io/apps/) to find an access token to use here.
        return "903cbdd53f59e2f771cbf2a9429c91"
    }

    override fun onCreate() {

        super.onCreate()

        // have to assign file directory here because context of app is needed for it and that is
        // not available until onCreate() is called
        dirPath = this.filesDir
        Log.v("DIRECTORY: ", dirPath.absolutePath)

        // setting onlinePeers to read file here instead of at the top because readOnlinePeers() needs
        // dirPath in order to proceed, and dirPath is still null when onlinePeers is instantiated
        onlinePeers = readOnlinePeers()
        messageDatabase = readMessageDatabase()
    }

    fun getAllOnlinePeers(): MutableList<Long> {
        return onlinePeers
    }

    fun getAllMessages(): HashMap<Long, Store> {
        return messageDatabase
    }

    fun getAllContacts(): HashMap<Long, User> {
        return contactsDatabase
    }

    // adds online peer to onlinePeers and triggers updating online peers file in memory
    private fun setAllOnlinePeers(userIdentifier: Long, addOrRemove: Boolean) {
        // true = add, false = remove
        if(addOrRemove) {
            onlinePeers.add(userIdentifier)
        } else {
            onlinePeers.remove(userIdentifier)
        }
    }

    // adds message to store and triggers updating store file in memory
    fun setMessageDatabase(userIdentifier: Long, store: Store) {
        messageDatabase[userIdentifier] = store
    }

    // adds contact to contactsDatabase and triggers updating the contacts file in memory
    fun setContactsDatabase(userIdentifier: Long, user: User) {
        contactsDatabase[userIdentifier] = user
    }

    private fun readOnlinePeers(): MutableList<Long> {
        val storeFile = File(dirPath, "storeFile")
        if(!(storeFile.exists()) || storeFile.length() == 0.toLong()) {
            storeFile.createNewFile()
            return mutableListOf()
        } else {
            try {
                val fis = FileInputStream(storeFile)
                val ois = ObjectInputStream(fis)
                val result = ois.readObject() as MutableList<Long>
                ois.close()
                Log.i("DEBUG: ", "readOnlinePeers() returned: " + result.toString())
                return result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mutableListOf()
    }

    fun updateOnlinePeersFile() {
        try {
            val storeFile = File(dirPath, "storeFile")
            if(!(storeFile.exists())) {
                storeFile.createNewFile()
            }
            val fos = FileOutputStream(storeFile)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(onlinePeers)
            oos.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readMessageDatabase(): HashMap<Long, Store> {
        val messageDatabaseFile = File(dirPath, "messageDatabase")
        if(!(messageDatabaseFile.exists()) || messageDatabaseFile.length() == 0.toLong()) {
            messageDatabaseFile.createNewFile()
            return hashMapOf()
        } else {
            try {
                val fis = FileInputStream(messageDatabaseFile)
                val ois = ObjectInputStream(fis)
                val result = ois.readObject() as HashMap<Long, Store>
                ois.close()
                Log.i("DEBUG: ", "readMessageDatabase() returned: " + result.toString())
                return result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return HashMap()
    }

    // updates the message database file whenever a new message is sent
    private fun updateMessageDatabase() {
        try {
            val messageDatabaseFile = File(dirPath, "messageDatabase")
            if(!(messageDatabaseFile.exists())) {
                messageDatabaseFile.createNewFile()
            }
            val fos = FileOutputStream(messageDatabaseFile)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(messageDatabase)
            oos.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readContactsDatabase(): HashMap<Long, User> {
        val contactsFile = File(dirPath, "contactsFile")
        if(!(contactsFile.exists()) || contactsFile.length() == 0.toLong()) {
            contactsFile.createNewFile()
            return hashMapOf()
        } else {
            try {
                val fis = FileInputStream(contactsFile)
                val ois = ObjectInputStream(fis)
                val result = ois.readObject() as HashMap<Long, User>
                ois.close()
                Log.i("DEBUG: ", "readContactsDatabase() returned: " + result.toString())
                return result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return hashMapOf()
    }

    fun updateContactsDatabase() {
        try {
            val contactsFile = File(dirPath, "contactsFile")
            if(!(contactsFile.exists())) {
                contactsFile.createNewFile()
            }
            val fos = FileOutputStream(contactsFile)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(contactsDatabase)
            oos.close()

        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addToResolvedInstancesMap(instance: Instance) {
        // Instances should be strongly kept by some data structure. Their identifiers
        // are useful for keeping track of which instances are ready to communicate.
        getAllOnlinePeers().add(instance.userIdentifier)
        setAllOnlinePeers(instance.userIdentifier, true)
        Log.i("DEBUG ", "New onlinePeers: " + getAllOnlinePeers().toString())
        if(getAllMessages()[instance.userIdentifier] == null) {
            setMessageDatabase(instance.userIdentifier, Store(instance))
            Log.i("DEBUG ", "New messageDatabase: " + getAllMessages().toString())
        }
        if(!(readContactsDatabase().containsKey(instance.userIdentifier))) {
            // restoring User object from serialized byteArray
            val bis = ByteArrayInputStream(instance.announcement)
            val ois = ObjectInputStream(bis)
            val newUser: User = ois.readObject() as User
            // for now, userIdentifier in User object is null (because Hype SDK only allows for 255 bytes
            // and adding the userIdentifier exceeds the limit)
            Log.i("DEBUG ", "NEWUSER OBJECT: ${newUser.toString()}")
            setContactsDatabase(instance.userIdentifier, newUser)
        }

//        // Notify the conversationList activity to refresh the UI
//        val conversationListActivity = ConversationListActivity()
//        conversationListActivity.notifyOnlinePeersChanged()
    }

    fun removeFromResolvedInstancesMap(instance: Instance) {
        // Cleaning up is always a good idea. It's not possible to communicate with instances
        // that were previously lost.
//        getAllOnlinePeers().remove(instance.userIdentifier)
        setAllOnlinePeers(instance.userIdentifier, false)

        // Notify the conversationList activity to refresh the UI
//        val conversationListActivity = ConversationListActivity()
//        conversationListActivity.notifyOnlinePeersChanged()
    }

    companion object {

        private const val TAG = "HypeServices"
    }
}