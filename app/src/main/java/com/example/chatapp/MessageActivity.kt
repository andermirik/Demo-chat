package com.example.chatapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.text.set
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.example.chatapp.Adapters.MessagesAdapter
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {

    var user_id: String = ""
    var firebaseUser: FirebaseUser? = null

    var messagesAdapter : MessagesAdapter? = null
    var messages_list: List<Message>? = null

    var reference: DatabaseReference? = null
    //lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        user_id = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //recyclerView = findViewById(R.id.message_list)
        message_list.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        message_list.layoutManager = linearLayoutManager

        //recyclerView.adapter = MessagesAdapter(this@MessageActivity, ArrayList<Message>())
        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(user_id)
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val user : User? = p0.getValue(User::class.java)
                tv_username.text = user!!.username
                Picasso.get().load(user.profile).into(image_avatar)
                retreave_message(firebaseUser!!.uid, user_id, user.profile)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        image_avatar.setOnClickListener{
            intent = Intent(this, VisitProfileActivity::class.java)
            intent.putExtra("visit_id", user_id)
            startActivity(intent)
        }

        image_send.setOnClickListener{
            send_message(firebaseUser!!.uid, user_id, edit_message.text.toString().trim())
            edit_message.setText("")
        }

        edit_message.addTextChangedListener{
            if(edit_message.text.length > 0){
                image_send.visibility = View.VISIBLE
            }
            else{
                image_send.visibility = View.INVISIBLE
            }
        }

        image_upload.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"), 438)
        }

        seen_message(user_id)

    }

    fun send_message(sender_id: String, receiver_id:String?, message:String){
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key
        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = sender_id
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiver_id
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["message_id"] = messageKey

        reference
            .child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful) {
                    val chatListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(user_id)

                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                chatListReference.child("id").setValue(user_id)
                            }
                            val chatListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(user_id)
                                .child(firebaseUser!!.uid)
                            chatListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }
                        override fun onCancelled(p0: DatabaseError) {
                        }
                    })



                    //here will pe push notifircations

                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users")
                        .child(firebaseUser!!.uid)
                }

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data!!.data != null){
            val loadingbar = ProgressDialog(this)
            loadingbar.setMessage("Please weit, image is sending...")
            loadingbar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val message_id = ref.push().key
            val file_path = storageReference.child("$message_id.jpg")

            var uploadTask = file_path.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if(!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation file_path.downloadUrl
            }).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = user_id
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["message_id"] = message_id

                    ref.child("Chats").child(message_id!!).setValue(messageHashMap)
                    loadingbar.dismiss()
                }
            }
        }
    }

    private fun retreave_message(sender_id:String, receiver_id: String?, receiver_image_url: String?){

        messages_list = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (messages_list as ArrayList<Message>).clear()
                for(snapshot in p0.children){
                    val message = snapshot.getValue(Message::class.java)
                    if((message!!.receiver.equals(sender_id)&& message.sender.equals(receiver_id))
                        || (message!!.receiver.equals(receiver_id)&& message.sender.equals(sender_id))) {
                        (messages_list as ArrayList<Message>).add(message)
                    }
                }
                messagesAdapter = MessagesAdapter(this@MessageActivity, (messages_list as ArrayList<Message>))
                message_list.adapter = messagesAdapter
            }
            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    var seenListener: ValueEventListener? = null

    private fun seen_message(user_id: String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(snapshot in p0.children){
                    val chat = snapshot.getValue(Message::class.java)

                    if(chat!!.receiver == firebaseUser!!.uid && chat!!.sender == user_id){
                        val messageHashMap = HashMap<String, Any?>()
                        messageHashMap["isseen"] = true
                        snapshot.ref.updateChildren(messageHashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

    }

    override fun onPause() {
        super.onPause()

        reference!!.removeEventListener(seenListener!!)
    }

}