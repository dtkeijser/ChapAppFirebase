package com.example.chapapp.registerlogin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.chapapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener{
            val email = tv_email_login.text.toString()
            val password = tv_password_login.text.toString()

            Log.d( "Login", "Attempt login with email/pw: $email/***")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    //make new intent to go to new activity
                .addOnCompleteListener { finish()  }
              //  .addOnFailureListener {  }
        }

        tv_back_reg.setOnClickListener {
            finish()
        }
    }
}