package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class SignInActivity : AppCompatActivity() {

    /* For now we'll have the user authentication strictly on the phone
     * I'm not entirely sure how to go about this since the user should never have to
     * connect to the internet. Will think about later
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initializing variables
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val signInButton = findViewById<Button>(R.id.signInButton)
        signInButton.setOnClickListener {
            if(authenticateUser(emailField.text.toString(), passwordField.text.toString())) {
                sendToChat()
            }
        }
    }

    private fun authenticateUser(email: String, password: String): Boolean {
        // TODO: Do actual authentication of user once we figure out how to go about that
        // Assume user authenticated for now
        return true
    }

    private fun sendToChat() {
        val intent = Intent(this, ConversationListActivity::class.java)
        startActivity(intent)
    }
}
