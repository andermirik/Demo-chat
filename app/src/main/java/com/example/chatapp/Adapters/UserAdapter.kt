package com.example.chatapp.Adapters

import android.content.Context
import android.content.Intent
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.MessageActivity
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mikhaellopez.circularimageview.CircularImageView
import com.squareup.picasso.Picasso

class UserAdapter(mContext: Context, mUsers: List<User>, isChatCheck: Boolean):
    RecyclerView.Adapter<UserAdapter.ViewHolder?>() {

    private var mContext: Context
    private var mUsers : List<User>
    private var isChatCheck : Boolean
    private var lastmsg = ""


    init {
        this.mContext = mContext
        this.mUsers = mUsers
        this.isChatCheck = isChatCheck
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var tv_username : TextView
        var tv_last_message : TextView
        var image_profile : CircularImageView
        var image_online : CircularImageView
        var image_offline : CircularImageView
        var wrapper : ConstraintLayout
        init {
            tv_username = itemView.findViewById(R.id.tv_username)
            tv_last_message = itemView.findViewById(R.id.tv_last_message)
            image_profile = itemView.findViewById(R.id.image_profile)
            image_online = itemView.findViewById(R.id.image_online)
            image_offline = itemView.findViewById(R.id.image_offline)
            wrapper = itemView.findViewById(R.id.wrapper)
        }
    }

    override fun onCreateViewHolder(viewgroup: ViewGroup, viewType: Int): ViewHolder {
        var view : View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item, viewgroup, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var user : User = mUsers[position]
        holder.tv_username.text = user.username
        Picasso.get().load(user.profile).placeholder(R.drawable.ic_baseline_person_24).into(holder.image_profile)

        if(isChatCheck){
            retreave_last_message(user.uid, holder.tv_last_message)
        }else{
            retreave_last_message(user.uid, holder.tv_last_message)
            //holder.tv_last_message.visibility = View.GONE
        }

        if(user.status=="online"){
            holder.image_online.visibility=View.VISIBLE
        }else{
            holder.image_online.visibility = View.GONE
        }

        holder.wrapper.setOnClickListener{
            var intent = Intent(mContext, MessageActivity::class.java)
            intent.putExtra("visit_id", user.uid)
            mContext.startActivity(intent)
        }

    }

    private fun retreave_last_message(uid: String, tv_last_message: TextView) {
        var firebaseUser = FirebaseAuth.getInstance().currentUser
        var reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(snapshot in p0.children){
                    val message = snapshot.getValue(com.example.chatapp.Models.Message::class.java)

                    if(firebaseUser!= null && message != null){
                        if((message.receiver == firebaseUser!!.uid && message.sender == uid) ||
                            (message.sender == firebaseUser!!.uid && message.receiver == uid)){
                            lastmsg = message.message
                        }
                    }
                }
                tv_last_message.text = lastmsg
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}