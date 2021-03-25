package com.example.firebase1

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.firebase1.databinding.ActivityLoginSuccessBinding
import com.example.firebase1.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class LoginSuccessActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage
    lateinit var binder : ActivityLoginSuccessBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_success)

        binder = ActivityLoginSuccessBinding.inflate(layoutInflater)

        storage = Firebase.storage

        // Create a storage reference from our app
        var storageRef = storage.reference

        // Create a reference to "mountains.jpg"
        val mountainsRef = storageRef.child("mountains.jpg")

    // Create a reference to 'images/mountains.jpg'
        val mountainImagesRef = storageRef.child("image/mountains.jpg")

    // While the file names are the same, the references point to different files
        mountainsRef.name == mountainImagesRef.name // true
        mountainsRef.path == mountainImagesRef.path // false

        binder.buttonUpload.setOnClickListener {
            // Get the data from an ImageView as bytes
            binder.imageView.isDrawingCacheEnabled = true
            binder.imageView.buildDrawingCache()
            val bitmap = (binder.imageView.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = mountainsRef.putBytes(data)
            var uploadTaskUnderImage = mountainImagesRef.putBytes(data)

            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Toast.makeText(this, "업로드 실패", Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                Toast.makeText(this, "업로드 성공", Toast.LENGTH_SHORT).show()
            }

        }

        setContentView(binder.root)
    }

}