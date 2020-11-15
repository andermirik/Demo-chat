package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
    }

    fun register_onclick(view: View) {
        val username = edit_username.text.toString()
        val email = edit_email.text.toString()
        val password = edit_password.text.toString()

        if(username== ""){
            Toast.makeText(this@RegisterActivity, "please write username", Toast.LENGTH_SHORT).show()
        }else if(email=="") {
            Toast.makeText(this@RegisterActivity, "please write email", Toast.LENGTH_SHORT).show()
        }
        else if(password==""){
            Toast.makeText(this@RegisterActivity, "please write password", Toast.LENGTH_SHORT).show()
        }
        else{
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        firebaseUserID = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)
                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"]=firebaseUserID
                        userHashMap["username"]=username
                        userHashMap["profile"]="https://firebasestorage.googleapis.com/v0/b/chatapp-56f01.appspot.com/o/default_avatar_0.png?alt=media&token=985ac89a-95af-44b0-8a92-e353e44cbf1a"
                        userHashMap["cover"]="https://firebasestorage.googleapis.com/v0/b/chatapp-56f01.appspot.com/o/minimalism-reflection-blue-rainbows-fox-stupid-fox-light-line-screenshot-computer-wallpaper-atmosphere-of-earth-220929.jpg?alt=media&token=6e09b3a7-309d-4de9-9147-6a876b4ee9d0"
                        userHashMap["status"]=username
                        userHashMap["search"]=username.toLowerCase()
                        userHashMap["vk"]="https://vk.com/id0"
                        userHashMap["website"]="https://github.com"

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener{ task ->
                                if(task.isSuccessful){
                                    intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }

                            }

                    } else{
                        Toast.makeText(this@RegisterActivity, "Error Message: "+ task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}