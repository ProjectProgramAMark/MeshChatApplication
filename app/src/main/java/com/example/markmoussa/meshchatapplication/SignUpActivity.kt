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
import android.graphics.Matrix
import android.widget.ImageView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


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
            // TODO: Edit this and uncomment line below once I find out whether I'm doing this right (by converting string to integer)
            sharedPrefEditor.putString("USERNAME", usernameTextEdit.text.toString())
            sharedPrefEditor.apply()
//            Hype.setUserIdentifier(usernameTextEdit.text.toString().toInt())
            val intent = Intent(this, ConversationListActivity::class.java)
            startActivity(intent)
        }
        uploadProfilePicButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 1)
        }
        if(sharedPreferences.contains("USERNAME")) {
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
    fun saveImage(bitmap: Bitmap) {
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
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }

    // Resizing profile pic; taken from Stack Overflow at:
    // https://stackoverflow.com/questions/11688982/pick-image-from-sd-card-resize-the-image-and-save-it-back-to-sd-card/11689101
    fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }
}
