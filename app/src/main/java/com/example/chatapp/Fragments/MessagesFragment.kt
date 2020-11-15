package com.example.chatapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.UserAdapter
import com.example.chatapp.Models.ChatList
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.view.*
import kotlinx.android.synthetic.main.fragment_messages.view.*
import kotlinx.android.synthetic.main.fragment_messages.view.image_avatar
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.android.synthetic.main.fragment_settings.view.tv_username

class MessagesFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var users: List<User>? = null
    private var chat_list : List<ChatList>? = null
    private var rv_chat_list : RecyclerView? = null
    private var firebase_user : FirebaseUser? = null

    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null

    private var storageRef : StorageReference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_messages, container, false)

        rv_chat_list = view.rv_chat_list
        rv_chat_list!!.setHasFixedSize(true)
        rv_chat_list!!.layoutManager = LinearLayoutManager(context)

        firebase_user = FirebaseAuth.getInstance().currentUser

        chat_list = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebase_user!!.uid)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (chat_list as ArrayList<ChatList>).clear()
                for (snapshot in p0.children) {
                    val chat = snapshot.getValue(ChatList::class.java)
                    (chat_list as ArrayList<ChatList>).add(chat!!)
                }
                retrive_chat_list()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

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
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })

        return view
    }

    private fun retrive_chat_list(){
        users = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("Users")

        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (users as ArrayList<User>).clear()
                for(snapshot in p0.children){
                    val user = snapshot.getValue(User::class.java)

                    for(eachChatList in chat_list!!){
                        if(user!!.uid == eachChatList.id){
                            (users as ArrayList<User>).add(user!!)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!, (users as ArrayList<User>), true)
                rv_chat_list!!.adapter = userAdapter
            }
            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }
}
