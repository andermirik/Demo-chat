package com.example.chatapp.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.UserAdapter
import com.example.chatapp.Models.User
import com.example.chatapp.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_peoples.*

class PeoplesFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private var mUsers : List<User>? = null
    private var recyclerView : RecyclerView? = null
    private var edit_search : EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_peoples, container, false)


        recyclerView = view.findViewById(R.id.search_list)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        mUsers = ArrayList()
        load_users()
       // FirebaseAuth.getInstance().signOut()

        edit_search = view.findViewById(R.id.edit_search)

        edit_search!!.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mUsers = ArrayList()
                search_for_users(s.toString().toLowerCase())
            }

        })
        //FirebaseAuth.getInstance().signOut()
        return view
    }

    private fun load_users(){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
        refUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<User>).clear()
                for(snapshot in p0.children){
                    val user : User? = snapshot.getValue(User::class.java)
                    if(!user!!.uid.equals(firebaseUserID)){
                        (mUsers as ArrayList<User>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!!, false)
                recyclerView!!.adapter = userAdapter
            }

        })
    }

    private fun search_for_users(str: String){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance()
            .reference.child("Users")
            .orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<User>).clear()
                for(snapshot in p0.children){
                    val user : User? = snapshot.getValue(User::class.java)
                    if(!user!!.uid.equals(firebaseUserID)){
                        (mUsers as ArrayList<User>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUsers!!, false)
                recyclerView!!.adapter = userAdapter
            }

        })
    }

}
