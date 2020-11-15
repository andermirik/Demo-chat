package com.example.chatapp

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.chatapp.Fragments.MessagesFragment
import com.example.chatapp.Fragments.PeoplesFragment
import com.example.chatapp.Fragments.SettingsFragment
import com.example.chatapp.Models.ChatList
import com.example.chatapp.Models.Message
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_badge.view.*
import java.lang.Exception
import java.lang.reflect.InvocationTargetException


class MainActivity : AppCompatActivity() {

    val messagesFragment = MessagesFragment()
    val peoplesFragment = PeoplesFragment()
    val settingsFragment = SettingsFragment()
    var currentFragment: Fragment? = null

    var firebaseUser : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(navListener)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        //currentFragment = messagesFragment

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, peoplesFragment)
            .add(R.id.fragment_container, messagesFragment)
            .add(R.id.fragment_container, settingsFragment)
            .hide(peoplesFragment)
            .hide(settingsFragment)
            .hide(messagesFragment)
            //.show(currentFragment!!)
            .commit()


        bottom_navigation.selectedItemId = R.id.nav_messages

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                var count_messages = 0
                for(snapshot in p0.children){
                    val message = snapshot.getValue(Message::class.java)
                    if(message!!.receiver == firebaseUser!!.uid && !message.isseen){
                        count_messages++
                    }
                }
                if(count_messages!=0){
                    addBadge(count_messages)
                }else{
                    removeBadge()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })



        defaultStatusBarColor = window.statusBarColor
    }

    var notificationsBadge : View?  = null
    private fun getBadge() : View {
        if (notificationsBadge != null){
            return notificationsBadge!!
        }
        val mbottomNavigationMenuView = bottom_navigation.getChildAt(0) as BottomNavigationMenuView
        notificationsBadge = LayoutInflater.from(this).inflate(R.layout.custom_badge,
            mbottomNavigationMenuView,false)
        return notificationsBadge!!
    }

    private fun addBadge(count : Int) {
        try {
            getBadge()
            notificationsBadge?.notifications_badge?.text = count.toString()
            bottom_navigation?.addView(notificationsBadge)
        }catch (ee: Exception){

        }

    }
    private fun removeBadge(){
        bottom_navigation.removeView(notificationsBadge)
    }

    private val navListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_messages -> {
                    selectedFragment = messagesFragment
                }
                R.id.nav_peoples -> {
                    selectedFragment = peoplesFragment
                }
                R.id.nav_settings -> {
                    selectedFragment = settingsFragment
                }
                else -> false
            }
            if(currentFragment != null) {
                supportFragmentManager.beginTransaction()
                    .hide(currentFragment!!)
                    .commit()
            }
            supportFragmentManager.beginTransaction().show(selectedFragment!!).setCustomAnimations(0,0).commit()

            if(selectedFragment == settingsFragment){
                set_settings_window_flags()
            }else{
                clear_flags()
            }

            currentFragment = selectedFragment
            true
        }

        var defaultStatusBarColor: Int = 255

        fun getAppUsableScreenSize(context: Context): Point {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size
        }

        fun getRealScreenSize(context: Context): Point {
            val windowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val size = Point()
            if (Build.VERSION.SDK_INT >= 17) {
                display.getRealSize(size)
            } else if (Build.VERSION.SDK_INT >= 14) {
                try {
                    size.x = Display::class.java.getMethod("getRawWidth").invoke(display) as Int
                    size.y = Display::class.java.getMethod("getRawHeight").invoke(display) as Int
                } catch (e: IllegalAccessException) {
                } catch (e: InvocationTargetException) {
                } catch (e: NoSuchMethodException) {
                }
            }
            return size
        }

        fun getNavigationBarSize(context: Context): Point {
            val appUsableSize: Point = getAppUsableScreenSize(context)
            val realScreenSize: Point = getRealScreenSize(context)

            // navigation bar on the side
            if (appUsableSize.x < realScreenSize.x) {
                return Point(realScreenSize.x - appUsableSize.x, appUsableSize.y)
            }

            // navigation bar at the bottom
            return if (appUsableSize.y < realScreenSize.y) {
                Point(appUsableSize.x, realScreenSize.y - appUsableSize.y)
            } else Point()

            // navigation bar is not present
        }

        fun get_height_navbar() : Int{
            val resources: Resources = this.getResources()
            val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
            //return getNavigationBarSize(this).y
        }

        fun clear_flags(){
           window.decorView.systemUiVisibility = 0
           window.statusBarColor = ContextCompat.getColor(this,R.color.colorPrimaryDark)
            val bottom_params = FrameLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            root_layout.layoutParams = bottom_params
        }

        fun set_settings_window_flags(){
            window.decorView.systemUiVisibility =
               SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.statusBarColor = Color.TRANSPARENT

            val navbarheight = getNavigationBarSize(this).y //get_height_navbar()
            if (navbarheight != 0) {
                val bottom_params = FrameLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )

                bottom_params.bottomMargin = get_height_navbar()
                root_layout.layoutParams = bottom_params
            }

        }

        private fun update_status(status: String){
            val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
            val hashMap = HashMap<String, Any>()

            hashMap["status"] = status
            ref!!.updateChildren(hashMap)
        }

    override fun onResume() {
        super.onResume()
        update_status("online")
    }

    override fun onPause() {
        super.onPause()
        update_status("offline")
    }

}