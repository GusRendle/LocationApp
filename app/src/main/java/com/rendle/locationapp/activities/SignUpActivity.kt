package com.rendle.locationapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.rendle.locationapp.databinding.ActivitySignUpBinding

//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivitySignUpBinding
private lateinit var auth: FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        b = ActivitySignUpBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()

        b.btnSignUp.setOnClickListener{
            val email: String = b.etEmail.text.toString()
            val password: String = b.etPassword.text.toString()

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter a valid username and password", Toast.LENGTH_LONG).show()
            } else{
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this
                ) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Successfully Registered", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        //Ensures user can't navigate back to registration page
                        finish()
                    } else {
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        b.tvBack.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}