package com.web2app.www


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.FrameLayout
import android.webkit.WebChromeClient
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.webkit.WebView
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import java.io.File


class MainActivity : AppCompatActivity(),AppFragment.DoubleClick {
    private lateinit var bottomNav:BottomNavigationView
    private val app1 = AppFragment()
    private val app2 = AppFragment()
    private val app3 = AppFragment()
    private lateinit var currentFragment: Fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Web2App)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item->
           return@setOnItemSelectedListener itemSelected(item)
        }
        bottomNav.setOnItemReselectedListener {
            reselected()
        }
        try {
            chromeClient = object:MyChrome(){
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    progress(newProgress)
                }
            }
            supportFragmentManager.beginTransaction().add(R.id.fragCon2, app2).setMaxLifecycle(app2,Lifecycle.State.CREATED).hide(app2).commit()
            supportFragmentManager.beginTransaction().add(R.id.fragCon3, app3).setMaxLifecycle(app3,Lifecycle.State.CREATED).hide(app3).commit()
            supportFragmentManager.beginTransaction().add(R.id.fragCon1,app1).show(app1).commit()
            currentFragment = app1
        }catch (e:Exception){
            e.printStackTrace()
            Log.e("some",e.message.toString())
        }
        path()
    }
    private fun progress(newProgress:Int) {
        val frag = currentFragment as AppFragment
        frag.progressBarUpdate(newProgress)
    }

    private fun reselected() {
        try {
            val frag = currentFragment as AppFragment
            if (frag.isVisible) {
                frag.reselectedMenu()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun itemSelected(item:MenuItem):Boolean{
      try {
          val manager = supportFragmentManager
          if (item.itemId == R.id.app1) {
              supportFragmentManager.beginTransaction().hide(currentFragment).show(app1).commit()
              manager.beginTransaction().setMaxLifecycle(currentFragment, Lifecycle.State.STARTED).commit()
              manager.beginTransaction().setMaxLifecycle(app1, Lifecycle.State.RESUMED).commit()
              currentFragment = app1
          } else if (item.itemId == R.id.app2) {
              supportFragmentManager.beginTransaction().hide(currentFragment).show(app2).commit()
              manager.beginTransaction().setMaxLifecycle(currentFragment, Lifecycle.State.STARTED).commit()
              manager.beginTransaction().setMaxLifecycle(app2, Lifecycle.State.RESUMED).commit()
              currentFragment = app2
          } else {
              supportFragmentManager.beginTransaction().hide(currentFragment).show(app3).commit()
              manager.beginTransaction().setMaxLifecycle(currentFragment, Lifecycle.State.STARTED).commit()
              manager.beginTransaction().setMaxLifecycle(app3, Lifecycle.State.RESUMED).commit()
              currentFragment = app3
          }
      }catch (e:Exception){
          Log.e("fragmentChange",e.message.toString())
          e.printStackTrace()
      }
        return true
    }
    override fun onBackPressed() {
        try {
            if (currentFragment.isVisible) {
                val frag = currentFragment as AppFragment
                if (!frag.goBack()) {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun hideBar() {
        if(bottomNav.visibility == View.VISIBLE){
            bottomNav.visibility = View.GONE
        }
        else{
            bottomNav.visibility = View.VISIBLE
        }
    }

    override fun path():String {
        var sPath = ""
        if(currentFragment == app1){
            sPath = "app1"
        }
        else if(currentFragment == app2){
            sPath = "app2"
        }
        else{
            sPath = "app3"
        }
        return sPath
    }

    override fun delete(path: String) {
        val file =getExternalFilesDir(path)
        if(file?.exists() == true){
               val a =  file.deleteRecursively()
        }
    }

    override fun save(ur: String,path:String){
        var url = ""
      if(ur.length>8) {
          url = if (ur.startsWith("https")) {
              ur.substring(8)
          } else {
              ur.substring(7)
          }
      }
        val file =getExternalFilesDir(path)
        if(file?.exists() == true){
            file.delete()
        }
        file?.mkdirs()
        File(file?.absolutePath+"/$url").mkdirs()
    }

    override fun giveUrl(path: String):String? {
        try {
            val file = getExternalFilesDir(path)?.listFiles()
            if (file?.isEmpty() == true) return null
            val str = "https://" + file?.get(0)?.name
            if (str.length > 8) {
                return str
            }
        }catch (e:Exception){
            e.printStackTrace()
            Log.e("saveError",e.message.toString())
        }
        return null
    }

    companion object{
      var chromeClient:MyChrome? = null
    }

    open inner class MyChrome : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: CustomViewCallback? = null
      //  protected var mFullscreenContainer: FrameLayout? = null
        private var mOriginalOrientation = 0
        //private var mOriginalSystemUiVisibility = 0
        override fun getDefaultVideoPoster(): Bitmap? {
            return if (mCustomView == null) {
                null
            } else BitmapFactory.decodeResource(applicationContext.resources, 2130837573)
        }

        override fun onHideCustomView() {
            (window.decorView as FrameLayout).removeView(mCustomView)
            mCustomView = null
           //window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
            WindowInsetsControllerCompat(window,window.decorView).show(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsControllerCompat(window,window.decorView).show(WindowInsetsCompat.Type.systemBars())
            requestedOrientation = mOriginalOrientation
            mCustomViewCallback!!.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View,
            paramCustomViewCallback: CustomViewCallback
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            //mOriginalSystemUiVisibility = window.decorView.systemUiVisibility
            mOriginalOrientation = requestedOrientation
            mCustomViewCallback = paramCustomViewCallback

            (window.decorView as FrameLayout).addView(
                mCustomView,
                FrameLayout.LayoutParams(-1, -1)
            )
            WindowInsetsControllerCompat(window,window.decorView).hide(WindowInsetsCompat.Type.navigationBars())
            WindowInsetsControllerCompat(window,window.decorView).hide(WindowInsetsCompat.Type.systemBars())
            WindowInsetsControllerCompat(window,window.decorView).systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        }
    }
}