package com.example.markmoussa.meshchatapplication

/**
 * Created by markmoussa on 2/24/18.
 */

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.widget.ImageView
import com.hypelabs.hype.Hype
import java.io.*
import java.nio.ByteBuffer


class SignUpActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val usernameTextEdit = findViewById<EditText>(R.id.usernameField)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val uploadProfilePicButton = findViewById<Button>(R.id.profilePicUploadButton)
        val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("sp", Context.MODE_PRIVATE)
        continueButton.setOnClickListener {
            val sharedPrefEditor: SharedPreferences.Editor = sharedPreferences.edit()
            sharedPrefEditor.putString("USERNAME", usernameTextEdit.text.toString())
            val userIdentifierByteArray: ByteArray = usernameTextEdit.text.toString().toByteArray(charset("UTF-8")).copyOf(64)
            val bb = ByteBuffer.wrap(userIdentifierByteArray)
            val userIdentifier: Int = bb.int
            // TODO: Should hash userIdentifier with random salt (and take half so it's 64 bits) to make sure it's not repeated ever
            sharedPrefEditor.putInt("USER_IDENTIFIER", userIdentifier)
            sharedPrefEditor.apply()
            val profilePicPath = sharedPreferences.getString("PROFILE_PIC_PATH", null)
            var profilePic: Bitmap? = null
            if(profilePicPath != null) {
                profilePic = BitmapFactory.decodeFile(profilePicPath)
            }
            // Making userIdentifier null in order to save space since the User object serializes to 288
            // bytes and the limit for Hype SDK for now is 255 bytes
            Hype.setAnnouncement(User(usernameTextEdit.text.toString(), profilePicPath, null, profilePic).serializeUser())
            val intent = Intent(this, ConversationListActivity::class.java)
            startActivity(intent)
        }
        uploadProfilePicButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 1)
        }
        if(sharedPreferences.contains("USERNAME") && sharedPreferences.contains("USER_IDENTIFIER")) {
            // Setting Hype announcement so host can send their info to other contact on handshake
            val username = sharedPreferences.getString("USERNAME", null)
            val userIdentifier = sharedPreferences.getInt("USER_IDENTIFIER", -1)
            val profilePicPath = sharedPreferences.getString("PROFILE_PIC_PATH", null)
            var profilePic: Bitmap? = null
            if(profilePicPath != null) {
                profilePic = BitmapFactory.decodeFile(profilePicPath)
            }
            // Making userIdentifier null in order to save space since the User object serializes to 288
            // bytes and the limit for Hype SDK for now is 255 bytes
            Hype.setAnnouncement(User(username, profilePicPath, null, profilePic).serializeUser())
            // Starting ConversationListActivity
            val intent = Intent(this, ConversationListActivity::class.java)
            startActivity(intent)
        }
    }

    // Saving profile pic
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val profilePicImageView = findViewById<ImageView>(R.id.profilePicImageView)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            val imageUri = data.data
            var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            bitmap = getResizedBitmap(bitmap, 170, 170)
            saveImage(bitmap)
            profilePicImageView.setImageBitmap(bitmap)
            val pathToImage = selectedImage!!.path
            val sharedPreferences: SharedPreferences = applicationContext.getSharedPreferences("sp", Context.MODE_PRIVATE)
            val sharedPrefEditor: SharedPreferences.Editor = sharedPreferences.edit()
            sharedPrefEditor.putString("PROFILE_PIC_PATH", pathToImage)
            sharedPrefEditor.apply()
        }
    }

    // saving profile pic; taken from Stack Overflow at:
    // https://stackoverflow.com/questions/34414608/set-profile-picture-and-save-in-internal-memory-folder-android-app
    private fun saveImage(bitmap: Bitmap) {
        val output: OutputStream

        // Create a name for the saved image
        val file = File(this.filesDir, "profilePic")
        try {
            output = FileOutputStream(file)
            // Compress into png format image from 0% - 100%
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.flush()
            output.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    // Resizing profile pic; taken from Stack Overflow at:
    // https://stackoverflow.com/questions/11688982/pick-image-from-sd-card-resize-the-image-and-save-it-back-to-sd-card/11689101
    private fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }
}
