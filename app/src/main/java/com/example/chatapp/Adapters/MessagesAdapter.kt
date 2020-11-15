package com.example.chatapp.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.ImageViewActivity
import com.example.chatapp.Models.Message
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class MessagesAdapter(
    private var context: Context,
    private var messages_list: List<Message>
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder?>(){

    private var firebase_user = FirebaseAuth.getInstance().currentUser


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        var image_message : ImageView? = null
        var image_unread : ConstraintLayout? = null
        var tv_message : TextView? = null
        var card_image : CardView? = null
        var wrapper : ConstraintLayout? = null
        init {
            image_message = view.findViewById(R.id.image_message)
            image_unread = view.findViewById(R.id.image_unread)
            tv_message = view.findViewById(R.id.tv_message)
            card_image = view.findViewById(R.id.card_image)
            wrapper = view.findViewById(R.id.wrapper)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if(viewType == 1){
            val view : View = LayoutInflater.from(context).inflate(R.layout.message_left, parent, false)
            return ViewHolder(view)
        }
        else{
            val view : View = LayoutInflater.from(context).inflate(R.layout.message_right, parent, false)
            return ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messages_list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages_list[position]
        //images
        if(!message.isseen && message.sender == firebase_user!!.uid){
            holder.image_unread!!.visibility = View.VISIBLE
        }else{
            holder.image_unread!!.visibility = View.GONE
        }

        if(message.message.equals("sent you an image.") && !message.url.equals("")){
            if(message.sender.equals(firebase_user!!.uid)){
                //right side
                holder.tv_message!!.visibility = View.GONE
                holder.image_message!!.visibility = View.VISIBLE
                holder.card_image!!.visibility = View.VISIBLE
                Picasso.get().load(message.url).into(holder.image_message)

                holder.wrapper!!.setOnClickListener {
                    var intent =
                        Intent(holder.itemView.context, ImageViewActivity::class.java)
                    intent.putExtra("url", message.url)
                    holder.itemView.context.startActivity(intent)
                }

                holder.wrapper!!.setOnLongClickListener{
                    val options = arrayOf<CharSequence>(
                        "Delete Image",
                        "Cancel"
                    )
                    var builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(options, DialogInterface.OnClickListener{
                        dialog, which ->
                        if(which == 0){
                            delete_message(position, holder)
                        }
                    })
                    builder.show()
                    true
                }
            }else{
                //left side
                holder.tv_message!!.visibility = View.GONE
                holder.image_message!!.visibility = View.VISIBLE
                holder.card_image!!.visibility = View.VISIBLE
                Picasso.get().load(message.url).into(holder.image_message)

                holder.wrapper!!.setOnClickListener {
                    var intent =
                        Intent(holder.itemView.context, ImageViewActivity::class.java)
                    intent.putExtra("url", message.url)
                    holder.itemView.context.startActivity(intent)
                }
            }
        }else{
            if(message.sender.equals(firebase_user!!.uid)){
                //right side
                holder.tv_message!!.visibility = View.VISIBLE
                holder.image_message!!.visibility = View.GONE
                holder.card_image!!.visibility = View.GONE
                holder.tv_message!!.text = message.message

                holder.wrapper!!.setOnLongClickListener{
                    val options = arrayOf<CharSequence>(
                        "Delete Message",
                        "Cancel"
                    )
                    var builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What do you want?")
                    builder.setItems(options, DialogInterface.OnClickListener{
                            dialog, which ->
                        if(which == 0){
                            delete_message(position, holder)
                        }
                    })
                    builder.show()
                    true
                }
            }else {
                //left side
                holder.tv_message!!.visibility = View.VISIBLE
                holder.image_message!!.visibility = View.GONE
                holder.card_image!!.visibility = View.GONE
                holder.tv_message!!.text = message.message
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages_list[position].sender.equals(firebase_user!!.uid)){
            0
        }else {
            1
        }

    }

    private fun delete_message(position: Int, holder: MessagesAdapter.ViewHolder){
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(messages_list.get(position).message_id)
            .removeValue()
            .addOnCompleteListener{task ->
                if(task.isSuccessful){
                    Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Failed. not deleted.", Toast.LENGTH_SHORT).show()
                }
            }
    }

}