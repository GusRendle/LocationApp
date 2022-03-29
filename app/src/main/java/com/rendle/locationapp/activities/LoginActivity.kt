package com.rendle.locationapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.rendle.locationapp.R
import com.rendle.locationapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    //Uses a Data Binding to refer to objects by their XML IDs
    private lateinit var b: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        b = ActivityLoginBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()

        b.btnLogin.setOnClickListener {
            //Get email and password strings
            val email: String = b.etEmail.text.toString()
            val password: String = b.etPassword.text.toString()

            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_LONG).show()
            } else {
                //Uses Firebase's default email sign in method
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

//        signupBtn.setOnClickListener{
//            val intent = Intent(this, SignUpActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        resetPasswordTv.setOnClickListener{
//            val intent = Intent(this, ForgotPasswordActivity::class.java)
//            startActivity(intent)
//        }
    }
}