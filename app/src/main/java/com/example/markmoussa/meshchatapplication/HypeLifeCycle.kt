package com.example.markmoussa.meshchatapplication

// THIS FILE WAS TAKEN DIRECTLY FROM THE HYPELABS ANDROID DEMO.
// ALL CREDIT FOR THIS FILE GOES DIRECTLY TO HYPELABS
// link: https://github.com/Hype-Labs/HypeChatDemo.android

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
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
import com.google.gson.reflect.TypeToken


class HypeLifeCycle : StateObserver, NetworkObserver, MessageObserver, Application() {


    /* TODO: Try doing this:
     * Make Instance transient in Store, since we don't really need to serialize and save it anyways
     * since it changes every time
     * Then make onlinePeers have a hashMap with key: userIdentifier and value: Instance
     * Then pass the Instance of the user (most likely as an intent extra) from ConversationListActivity
     * to MessageListActivity, and have Store use that instead
     * May find at the end that you don't even need to have Instance in the Store constructor
     * TODO: I don't even need onlinePeers file since the onlinePeers is only for the duration of the app runtime.
     * Delete and replace with standard getters and setters
     *
     * If that doesn't solve the problem, it might be that Message is causing serialization issues
     * in which case, as a hotfix until I figure out why Hype hasn't allowed Serialization for these
     * Make the Store messages variable carry the Messages data (aka the text itself) instead of the
     * Hype Message class, since we don't really need the Message object when saving things anyways
     *
     * If that still doesn't work, might have to write custom serializer/deserializer for Store instead
     */


    // TODO: DO THIS SECOND - Figure out why these observables aren't working sometimes (such as messageDatabase in setMessageDatabase()
    // TODO: Consider merging messageDatabase and contactsDatabase (can do HashMap<Long, Pair<Store, User>>)
    // The onlinePeers object keeps track of message storage associated with each instance (peer) via their userIdentifiers
    private var onlinePeers: HashMap<Long, Instance> = hashMapOf()

    // The messageDatabase keeps track of the users and their previous messages via saving their userIdentifiers and a Store
    private var messageDatabase: HashMap<Long, Store> by Delegates.observable(hashMapOf()) {
        _, _, _ -> updateMessageDatabase()
    }

    // contactsDatabase saves previously before seen users to save sending data back and forth via their userIdentifiers and User objects
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

        // ChatApplication
        Hype.setAppIdentifier("f370ac17")

        // MeshNetworkApplication
        // Hype.setAppIdentifier("9a96baaa")

        // MeshNetworkApp2
        Hype.setAppIdentifier("b056a7af")

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
        messageDatabase = readMessageDatabase()
        contactsDatabase = readContactsDatabase()
    }

    override fun onHypeStop(error: Error?) {

        var description = ""

        if (error != null) {
            description = String.format("[%s]", error.description)
        }

        Log.i(TAG, String.format("Hype stopped [%s]", description))
        onlinePeers.clear()
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

        var store = getAllMessages()[instance.userIdentifier]
        if(store == null) {
            store = Store(instance, instance.userIdentifier)
        }
        // Storing the message triggers a reload update in the MessageList activity
        store.add(Pair(message.data.toString(charset("UTF-8")), false), this)
        setMessageDatabase(instance.userIdentifier, store)

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
        // This one is for chatApplication
//        return "903cbdd53f59e2f771cbf2a9429c91"

        // MeshNetworkApplication
//        return "4e5936b294a88cf2"

        // MeshNetworkApp2
        return "4e5936b294a88cf2"
    }

    override fun onCreate() {

        super.onCreate()

        // have to assign file directory here because context of app is needed for it and that is
        // not available until onCreate() is called
        dirPath = this.filesDir
        Log.v("DIRECTORY: ", dirPath.absolutePath)

        // setting onlinePeers to read file here instead of at the top because readOnlinePeers() needs
        // dirPath in order to proceed, and dirPath is still null when onlinePeers is instantiated
//        onlinePeers = readOnlinePeers()
//        messageDatabase = readMessageDatabase()
//        contactsDatabase = readContactsDatabase()
    }

    fun getOnlinePeers(): HashMap<Long, Instance> {
        return onlinePeers
    }

    // adds online peer to onlinePeers and triggers updating online peers file in memory
    private fun setOnlinePeers(userIdentifier: Long, instance: Instance, addOrRemove: Boolean) {
        // true = add, false = remove
        if(addOrRemove) {
            if(!(onlinePeers.contains(userIdentifier))) {
                onlinePeers[userIdentifier] = instance
            }
        } else {
            onlinePeers.remove(userIdentifier)
        }
    }

    fun getAllMessages(): HashMap<Long, Store> {
        return messageDatabase
    }

    fun getAllContacts(): HashMap<Long, User> {
        return contactsDatabase
    }

    fun setAllMessages(messageDatabase: HashMap<Long, Store>) {
        this.messageDatabase = messageDatabase
    }



    // adds message to store and triggers updating store file in memory
    fun setMessageDatabase(userIdentifier: Long, store: Store) {
        messageDatabase[userIdentifier] = store
        // TODO: For some reason, observable delegate not calling updateMessageDatabase() when message database is changed
        // TODO: Figure out why and delete manual call once fixed
        updateMessageDatabase()
        // debugging
        Log.d("HypeLifeCycle ", "new messageDatabase (from variable) is: ${messageDatabase.entries.toString()}")
        for(x in messageDatabase.values) {
            for(y in x.getMessages()) {
                Log.d("HypeLifeCycle", "Store contents (from new messageDatabase (from variable)): ${y.first}")
            }
        }
    }

    // adds contact to contactsDatabase and triggers updating the contacts file in memory
    fun setContactsDatabase(userIdentifier: Long, user: User) {
        // TODO: Hackish fix since I can't send over userIdentifier without the bytes going over its limit
        // Fix once Hype's SDK allows for greater than 255 bytes sent on announcement
        val newUser = User(user.nickname, user.profileUri, userIdentifier)
        contactsDatabase[userIdentifier] = newUser

        // TODO: For some reason, observable delegate not calling updateContactsDatabase() when message database is changed
        // TODO: Figure out why and delete manual call once fixed
        updateContactsDatabase()
    }

    inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object: TypeToken<T>() {}.type)
    private fun readMessageDatabase(): HashMap<Long, Store> {
        val messageDatabaseFile = File(dirPath, "messageDatabase")
        if(!(messageDatabaseFile.exists()) || messageDatabaseFile.length() == 0.toLong()) {
            messageDatabaseFile.createNewFile()
            if(!(messageDatabaseFile.exists())) {
                Log.d("HypeLifeCycle", "File for message database does not exist; creating new one (from read)")
            } else {
                Log.d("HypeLifeCycle", "messageDatabaseFile.length() == 0")
            }
            return hashMapOf()
        } else {
            try {
                val fis = FileInputStream(messageDatabaseFile)
                val ois = ObjectInputStream(fis)
                val result = ois.readObject() as HashMap<Long, Store>
                ois.close()

//                // Using GSON library instead of ObjectOutputStream for now because Instance (found in Store) is not Serializable at the moment
//                val fis = FileInputStream(messageDatabaseFile)
//                val ois = ObjectInputStream(fis)
//                val jsonHashMapResult = ois.readObject().toString()
//                ois.close()
//                // debugging
//                Log.d("HypeLifeCycle", "jsonHashMapResult: $jsonHashMapResult")
//                val result: HashMap<Long, Store> = Gson().fromJson<HashMap<Long, Store>>(jsonHashMapResult)
                // debugging
                Log.d("HypeLifeCycle ", "reading messageDatabase (from file) is: ${result.entries.toString()}")

                for(x in result.values) {
                    for(y in x.getMessages()) {
                        Log.d("HypeLifeCycle", "Store contents (from reading messageDatabase (from file)): ${y.first}")
                    }
                }
                return result
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return HashMap()
    }

    // updates the message database file whenever a new message is sent
    private fun updateMessageDatabase() {
        // debugging
        Log.d("HypeLifeCycle", "updateMessageDatabase() being called")
        try {
            val messageDatabaseFile = File(dirPath, "messageDatabase")
            if(!(messageDatabaseFile.exists())) {
                messageDatabaseFile.createNewFile()
                Log.d("HypeLifeCycle", "File for message database does not exist; creating new one (from update)")
            }
            // TODO: DO THIS FIRST - figure out how to serialize messageDatabase
            // TODO: since Instance from Hype SDK not serializable, it won't let me serialize all of messageDatabase
            val fos = FileOutputStream(messageDatabaseFile)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(messageDatabase)
            // debugging
            Log.d("HypeLifeCycle", "Right after writing the file, the new file is: ${readMessageDatabase()}")
            oos.close()

            // Using GSON library instead of ObjectOutputStream for now because Instance (found in Store) is not Serializable at the moment
//            val fos = FileOutputStream(messageDatabaseFile)
//            val gsonSerializer = Gson().toJson(messageDatabase)
//
//            val oos = ObjectOutputStream(fos)
//            oos.writeObject(gsonSerializer)
            // debugging
            Log.d("HypeLifeCycle", "Right after writing the messageDatabase file, the new file is: ${readMessageDatabase()}")

            // Checking if the file is empty, meaning the messageDatabase didn't write properly
            // debugging
            if(messageDatabaseFile.length() == 0.toLong()) {
                Log.d("HypeLifeCycle", "The messageDatabaseFile is still empty after writing; this means there's a problem in updateMessageDatabase()")
            }
            oos.close()

        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun readContactsDatabase(): HashMap<Long, User> {
        val contactsFile = File(dirPath, "contactsFile")
        if(!(contactsFile.exists()) || contactsFile.length() == 0.toLong()) {
            contactsFile.createNewFile()
            Log.d("HypeLifeCycle", "File for contacts database does not exist; creating new one (from read)")
            return hashMapOf()
        } else {
            try {
                val fis = FileInputStream(contactsFile)
                val ois = ObjectInputStream(fis)
                val result = ois.readObject() as HashMap<Long, User>
                ois.close()
                Log.d("HypeLifeCycle: ", "readContactsDatabase() returned: " + result.toString())
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
                Log.d("HypeLifeCycle", "File for contacts database does not exist; creating new one (from update)")
            }
            val fos = FileOutputStream(contactsFile)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(contactsDatabase)
            Log.d("HypeLifeCycle", "Right after writing the contacts file, the new file is: ${readContactsDatabase()}")
            oos.close()

        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addToResolvedInstancesMap(instance: Instance) {
        // Instances should be strongly kept by some data structure. Their identifiers
        // are useful for keeping track of which instances are ready to communicate.
        getOnlinePeers().put(instance.userIdentifier, instance)
        setOnlinePeers(instance.userIdentifier, instance, true)
        Log.d("HypeLifeCycle ", "New onlinePeers: " + getOnlinePeers().toString())
        if(getAllMessages()[instance.userIdentifier] == null) {
            Log.d("HypeLifeCycle", "Could not find userIdentifier in getAllMessages() (aka messageDatabase), therefore starting a brand new Store")
            setMessageDatabase(instance.userIdentifier, Store(instance, instance.userIdentifier))
            Log.d("HypeLifeCycle ", "New messageDatabase (from variable): " + getAllMessages().toString())
        }
        if(!(readContactsDatabase().containsKey(instance.userIdentifier))) {
            // restoring User object from serialized byteArray
            val bis = ByteArrayInputStream(instance.announcement)
            val ois = ObjectInputStream(bis)
            val newUser: User = ois.readObject() as User
            // for now, userIdentifier in User object is null (because Hype SDK only allows for 255 bytes
            // and adding the userIdentifier exceeds the limit)
            Log.d("HypeLifeCycle ", "NEWUSER OBJECT: ${newUser.toString()}")
            setContactsDatabase(instance.userIdentifier, newUser)
        } else {
            Log.d("HypeLifeCycle", "User recognized in ContactsDatabase, no need to set anything new")
        }

    }

    fun removeFromResolvedInstancesMap(instance: Instance) {
        // Cleaning up is always a good idea. It's not possible to communicate with instances
        // that were previously lost.
//        getOnlinePeers().remove(instance.userIdentifier)

        // debugging
        Log.d("HypeLifeCycle", "Lost instance")
        setOnlinePeers(instance.userIdentifier, instance,false)

        // Notify the conversationList activity to refresh the UI
//        val conversationListActivity = ConversationListActivity()
//        conversationListActivity.notifyOnlinePeersChanged()
    }

    companion object {

        private const val TAG = "HypeServices"
    }
}