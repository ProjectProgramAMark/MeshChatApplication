package com.example.markmoussa.meshchatapplication

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton = findViewById<Button>(R.id.signInButton) as Button
        signInButton.setOnClickListener {
//            Toast.makeText(this, "Just a test!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        val signUpButton = findViewById<Button>(R.id.signInButton) as Button
        signUpButton.setOnClickListener {
//            Toast.makeText(this, "Just a test!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
