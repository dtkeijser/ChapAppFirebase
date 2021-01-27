package com.example.chapapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.chapapp.messages.LatestMessagesActivity
import com.example.chapapp.models.User
import com.example.chapapp.registerlogin.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    companion object{
        val TAG ="RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_register.setOnClickListener {
            performRegister()
        }

        tv_already_account.setOnClickListener {
            Log.d(TAG, "log activity")
            //Launch the login activity

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btn_image_register.setOnClickListener {
            Log.d(TAG, "try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type ="image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG, "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            select_phote_imageview_register.setImageBitmap(bitmap)

            btn_image_register.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btn_image_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {
        val email = etEmailRegister.text.toString()
        val password = etPasswordRegister.text.toString()


        Log.d(TAG, "Email is: " + email)
        Log.d(TAG, "password is: $password")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return

        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                //else if succesful
                Log.d(TAG, "successfully created user with uid: ${it.result?.user?.uid}")

                upLoadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
    private fun upLoadImageToFirebaseStorage(){
        if (selectedPhotoUri == null) return
//        if (selectedPhotoUri == null) {
//            saveUserToFirebaseDatabase("")
//            return
//        }
        val filename = UUID.randomUUID().toString()
        val ref =FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Photo succesfully uploaded: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d(TAG, "fileLocation: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrL: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid,etUserNameRegister.text.toString(), profileImageUrL)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "User saved")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to set value to database: ${it.message}")
            }
    }


}

