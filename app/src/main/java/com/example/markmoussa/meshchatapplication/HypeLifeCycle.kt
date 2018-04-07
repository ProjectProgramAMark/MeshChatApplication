package com.example.markmoussa.meshchatapplication

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

import android.app.Application
import android.util.Log
import com.hypelabs.hype.Error
import com.hypelabs.hype.Hype
import com.hypelabs.hype.Instance
import com.hypelabs.hype.Message
import com.hypelabs.hype.MessageInfo
import com.hypelabs.hype.MessageObserver
import com.hypelabs.hype.NetworkObserver
import com.hypelabs.hype.StateObserver
import java.io.*
import kotlin.collections.HashMap
import kotlin.properties.Delegates


class HypeLifeCycle : HypeKeepForeground(), StateObserver, NetworkObserver, MessageObserver, HypeKeepForeground.LifecycleDelegate {

    // TODO: Consider moving onlinePeers to a different class (must be a class within the same lifetime of the app, a.k.a. an activity
    // The onlinePeers object keeps track of message storage associated with each instance (peer)
    private var onlinePeers: MutableList<String> by Delegates.observable(mutableListOf()) {
        _, _, _ -> updateOnlinePeersFile()
    }

    private var messageDatabase: HashMap<String, Store> by Delegates.observable(HashMap()) {
        _, _, _ -> updateMessageDatabase()
    }

    private lateinit var dirPath: File

    override fun onApplicationStart(app: Application) {
        requestHypeToStart()
    }

    override fun onApplicationStop(app: Application) {

        requestHypeToStop()
    }

    private fun requestHypeToStart() {
        Hype.setContext(applicationContext)

        // Add this as an Hype observer
        Hype.addStateObserver(this)
        Hype.addNetworkObserver(this)
        Hype.addMessageObserver(this)

        // Generate an app identifier in the HypeLabs dashboard (https://hypelabs.io/apps/),
        // by creating a new app. Copy the given identifier here.
        Hype.setAppIdentifier("f370ac17")

        Hype.start()
    }

    protected fun requestHypeToStop() {
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

        val store = getAllMessages()[instance.stringIdentifier]

        // Storing the message triggers a reload update in the ChatActivity
        store!!.add(message, this)

        // TODO: Add a contact activity to update contacts
        /*
        // Update the UI for the ContactActivity as well
        val contactActivity = ContactActivity.getDefaultInstance()

        if (contactActivity != null) {
            contactActivity!!.notifyAddedMessage()
        }*/
    }

    override fun onHypeMessageFailedSending(messageInfo: MessageInfo, instance: Instance, error: Error) {

        Log.i(TAG, String.format("Hype failed to send message: %d [%s]", messageInfo.identifier, error.description))
    }

    override fun onHypeMessageSent(messageInfo: MessageInfo, instance: Instance, progress: Float, done: Boolean) {

        Log.i(TAG, String.format("Hype is sending a message: %f", progress))
    }

    override fun onHypeMessageDelivered(messageInfo: MessageInfo, instance: Instance, progress: Float, done: Boolean) {

        Log.i(TAG, String.format("Hype delivered a message: %f", progress))
    }

    override fun onHypeRequestAccessToken(i: Int): String {
        // Access the app settings (https://hypelabs.io/apps/) to find an access token to use here.
        return "903cbdd53f59e2f771cbf2a9429c91"
    }

    override fun onCreate() {

        super.onCreate()
        setLifecyleDelegate(this)

        // have to assign file directory here because context of app is needed for it and that is
        // not available until onCreate() is called
        dirPath = this.filesDir
        // setting onlinePeers to read file here instead of at the top because readOnlinePeers() needs
        // dirPath in order to proceed, and dirPath is still null when onlinePeers is instantiated
        onlinePeers = readOnlinePeers()
        messageDatabase = readMessageDatabase()
    }

    fun getAllOnlinePeers(): MutableList<String> {
        return onlinePeers
    }

    fun getAllMessages(): HashMap<String, Store> {
        return messageDatabase
    }

    // adds message to store and triggers updating store file in memory
    private fun setAllOnlinePeers(storeIdentifier: String) {
        onlinePeers.add(storeIdentifier)
    }

    // adds message to store and triggers updating store file in memory
    fun setMessageDatabase(storeIdentifier: String, store: Store) {
        messageDatabase[storeIdentifier] = store
    }

    private fun readOnlinePeers(): MutableList<String> {
        val storeFile = File(dirPath, "storeFile")
        if(!(storeFile.exists())) {
            storeFile.createNewFile()
        }
        try {
            val fis = FileInputStream(storeFile)
            val ois = ObjectInputStream(fis)
            val result = ois.readObject() as MutableList<String>
            ois.close()
            return result
        } catch (e: Exception) {
            e.printStackTrace()
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
            oos.writeObject(getAllOnlinePeers())
            oos.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readMessageDatabase(): HashMap<String, Store> {
        val messageDatabaseFile = File(dirPath, "messageDatabase")
        if(!(messageDatabaseFile.exists())) {
            messageDatabaseFile.createNewFile()
        }
        try {
            val fis = FileInputStream(messageDatabaseFile)
            val ois = ObjectInputStream(fis)
            val result = ois.readObject() as HashMap<String, Store>
            ois.close()
            return result
        } catch (e: Exception) {
            e.printStackTrace()
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
            oos.writeObject(getAllMessages())
            oos.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addToResolvedInstancesMap(instance: Instance) {
        // Instances should be strongly kept by some data structure. Their identifiers
        // are useful for keeping track of which instances are ready to communicate.
        getAllOnlinePeers().add(instance.stringIdentifier)
        setAllOnlinePeers(instance.stringIdentifier)

        /* TODO: Make another class to store conversations. Store just onlinePeers instances online right now
         * OR consider just showing both everyone who's online at the moment and everyone you've had
         * a conversation with (since they'll both be saved to onlinePeers) and have a green dot next to
         * anyone you can actually communicate with at the moment
         */

        // TODO: Add a contact activity to update contacts
        /*
        // Notify the contact activity to refresh the UI
        val contactActivity = ContactActivity.getDefaultInstance()

        if (contactActivity != null) {
            contactActivity!!.notifyContactsChanged()
        }*/
    }

    fun removeFromResolvedInstancesMap(instance: Instance) {
        // Cleaning up is always a good idea. It's not possible to communicate with instances
        // that were previously lost.
        getAllOnlinePeers().remove(instance.stringIdentifier)


        // TODO: Add a contact activity to update contacts
        /*
        // Notify the contact activity to refresh the UI
        val contactActivity = ContactActivity.getDefaultInstance()

        if (contactActivity != null) {
            contactActivity!!.notifyContactsChanged()
        } */
    }

    companion object {

        private val TAG = HypeKeepForeground::class.java.name
    }
}