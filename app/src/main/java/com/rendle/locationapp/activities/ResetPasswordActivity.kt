package com.rendle.locationapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.rendle.locationapp.databinding.ActivityResetPasswordBinding

//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityResetPasswordBinding
private lateinit var auth: FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        b = ActivityResetPasswordBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()

        b.tvBack.setOnClickListener {
            finish()
        }

        b.btnResetPassword.setOnClickListener {
            val email: String = b.etEmail.text.toString()
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email address", Toast.LENGTH_LONG).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Reset link sent", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(this, "Unable to send reset link", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            }
        }
    }
}