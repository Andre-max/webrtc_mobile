package com.hakemy.videocallingtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_create__room.*
import me.amryousef.webrtc_demo.MainActivity

class Create_Room : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create__room)

        button.setOnClickListener(View.OnClickListener {

            val intent = Intent(applicationContext,MainActivity::class.java)
            intent.putExtra("room",textField.editText?.text.toString())


            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        })


        mAuth = FirebaseAuth.getInstance();


        mAuth!!.signInAnonymously()
            .addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user: FirebaseUser? = mAuth!!.getCurrentUser()
                        Log.e("ss","aliali")
                    } else {
                        // If sign in fails, display a message to the user.

                    }

                    // ...
                })
    }
}