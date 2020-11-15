package com.example.chatapp.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.chatapp.LoginActivity
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null

    private var storageRef : StorageReference? = null

    var cover_checker : String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_settings, container, false)


        view.btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent = Intent(activity, LoginActivity::class.java)
            var last_activity = activity
            startActivity(intent)
            last_activity!!.finish()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference!!.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user: User? = p0.getValue(User::class.java)
                    if(context!= null){
                        view.tv_username.text = user!!.username
                        Picasso.get().load(user.profile).into(view.image_avatar)
                        Picasso.get().load(user.cover).into(view.image_cover)
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })

        view.image_avatar.setOnLongClickListener{
            cover_checker=""
            pick_image()
            return@setOnLongClickListener true
        }

        view.image_cover.setOnLongClickListener{
            cover_checker="cover"
            pick_image()
            return@setOnLongClickListener true
        }

        return view
    }

    private fun pick_image(){
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 438)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == Activity.RESULT_OK && data!= null && data!!.data != null){
            var image_uri = data!!.data
            Toast.makeText(context, "uploading....", Toast.LENGTH_LONG).show()
            upload_image_database(image_uri)
        }
    }

    private fun upload_image_database(image_uri: Uri?){
        var progressbar = ProgressDialog(context)
        progressbar.setMessage("image is uploading, please wait....")
        progressbar.show()

        if(image_uri!=null){
            var fileRef = storageRef!!.child(System.currentTimeMillis().toString()+".jpg")

            var uploadTask = fileRef.putFile(image_uri)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if(!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener{task ->  
                if(task.isSuccessful){
                    val downloadurl = task.result
                    val url = downloadurl.toString()

                    val map = HashMap<String, Any>()
                    if(cover_checker=="cover"){
                        map["cover"] = url
                    }else{
                        map["profile"] = url
                    }
                    userReference!!.updateChildren(map)
                    progressbar.dismiss()
                }
            }
        }

    }


}
