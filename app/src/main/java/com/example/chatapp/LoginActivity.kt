package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
    }

    fun register_onclick(view: View) {
        var intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    fun login_onclick(view: View) {
        val email = edit_email.text.toString()
        val password = edit_password.text.toString()

        if(email=="") {
            Toast.makeText(this@LoginActivity, "please write email", Toast.LENGTH_SHORT).show()
        }
        else if(password==""){
            Toast.makeText(this@LoginActivity, "please write password", Toast.LENGTH_SHORT).show()
        }else{
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this@LoginActivity, "Error Message: "+ task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}