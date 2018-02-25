package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class SignUpActivity : AppCompatActivity() {


    /* For now we'll have the user data remain on the phone
     * I'm not entirely sure as to how I should go about authenticating the user since
     * the mesh chat is such that you should not need an internet connection for it
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }
}
