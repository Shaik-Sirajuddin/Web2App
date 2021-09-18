package com.web2app.www

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import android.content.Intent
import android.net.Uri
import android.view.inputmethod.InputMethodManager
import android.widget.*

class AppFragment: Fragment() {
    private  var webView:WebView? = null
    private lateinit var splashPage:ConstraintLayout
    private var listener:DoubleClick? = null
    var toast:Toast? = null
    var clearHistory = false
    var isLoading = false
    private var flag = false
    lateinit var progressBar:ProgressBar
    lateinit var chromeClient: MainActivity.MyChrome
    val user_agent= "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            val view = inflater.inflate(R.layout.res_frag, container, false)
          try{  progressBar = view.findViewById(R.id.ProgressBar)
            webView = view.findViewById(R.id.webView)
            splashPage = view.findViewById(R.id.layoutToHide)
           }catch(e:Exception){
             e.printStackTrace()
           }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DoubleClick
        }catch (e:Exception){
            Log.e("implement interface",e.message.toString())
        }
    }
    override fun onDetach() {
        super.onDetach()
        listener = null
    }
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

     try {
         if(savedInstanceState == null){
             flag = true
         }
         val button: Button = view.findViewById(R.id.button)
         val editText: EditText = view.findViewById(R.id.url)
         //WebView Settings ->
         webView?.webViewClient =object: WebViewClient(){
             @SuppressLint("ShowToast")
             override fun onReceivedError(
                 view: WebView?,
                 request: WebResourceRequest?,
                 error: WebResourceError?
             ) {
                 super.onReceivedError(view, request, error)
                 progressBar.visibility = View.INVISIBLE
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                     toast?.cancel()
                     toast = Toast.makeText(context,error?.description.toString(),Toast.LENGTH_SHORT)
                     toast?.show()
                 }
                 //goBack()
             }

             override fun onPageFinished(view: WebView?, url: String?) {
                 super.onPageFinished(view, url)
                 if(clearHistory){
                     webView?.clearHistory()
                     clearHistory = false
                 }
             }
         }
         progressBar.max = 100
         webView?.settings?.loadsImagesAutomatically = true
         webView?.settings?.javaScriptEnabled = true
         webView?.settings?.allowFileAccess = true
         webView?.settings?.setSupportMultipleWindows(true)
         webView?.settings?.setSupportZoom(true)
         webView?.settings?.builtInZoomControls = true
         webView?.settings?.displayZoomControls = false
         //->1.1
         webView?.settings?.loadWithOverviewMode = true
         webView?.settings?.useWideViewPort = true
         webView?.settings?.userAgentString = user_agent;
         //<-1.1
         chromeClient = MainActivity.chromeClient!!
         webView?.webChromeClient = chromeClient
         //WebView Settings ->
         button.setOnClickListener {
             var text =editText.editableText.toString().trim()
             if(text.isNotEmpty() && !text.startsWith("http")){
                 text = "https://$text"
             }
             if(text.isNotEmpty()){
                 hideKeyboard()
                 listener?.hideBar()
                 loadWebView(text)
                 loadMainPage()
                 listener?.path()?.let { it1 -> listener?.save(text, it1) }
             }else{
                 Toast.makeText(context,"Please Enter A Valid URl",Toast.LENGTH_SHORT).show()
             }
         }
         view.setOnClickListener(object : DoubleClickListener() {
             override fun onDoubleClick(v: View?){
                 listener?.hideBar()
             }
         })
         webView?.setOnTouchListener(object :DoubleClick2(){
             override fun onDoubleClick2(v: View?) {
                 listener?.hideBar()
             }
         })
         webView?.setDownloadListener { url, _, _, _, _ ->
             val i = Intent(Intent.ACTION_VIEW)
             i.data = Uri.parse(url)
             startActivity(i)
         }
         val suggest = view.findViewById<TextView>(R.id.suggestion)
         suggest.setOnClickListener {
             suggestIntent()
         }
     }catch(e:Exception){
         e.printStackTrace()
         Log.e("Frag",e.message.toString())
     }
    }

    override fun onResume() {
        super.onResume()
        if(flag) {
            val ur = listener?.path()?.let { listener?.giveUrl(it) }
            if (ur != null) {
                loadWebView(ur)
                loadMainPage()
                listener?.hideBar()
            }
            flag = false
        }
    }
    private fun suggestIntent() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:techx2002@gmail.com"))
            intent.putExtra(Intent.EXTRA_EMAIL, "techx2002@gmail.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Web2App - Suggestion")
            startActivity(intent)
        }catch(e:Exception){
            Toast.makeText(context,"Your Device Cannot Perform This Action",Toast.LENGTH_SHORT).show()
        }
    }

    fun goBack():Boolean{
            if (webView?.canGoBack() == true) {
                progressBar.visibility = View.INVISIBLE
                webView?.goBack()
                return true
            } else if (splashPage.visibility == View.GONE) {
                reselectedMenu()
                return true
            }
            return false
    }
    fun reselectedMenu(){
        if(splashPage.visibility == View.GONE){
            val builder = AlertDialog.Builder(context,R.style.AlertDialogStyle)
            builder.setMessage("Exit This Site?")
                .setPositiveButton("Yes"
                ) { dialog, _ ->
                    dialog.cancel()
                    listener?.path()?.let { listener?.delete(it) }
                    loadSplashPage()
                }
                .setNegativeButton("Exit App"
                ) { dialog, _ ->
                    dialog.cancel()
                    requireActivity().finishAffinity()
                }
           val dialog =  builder.create()
            dialog.show()
        }
    }
    private fun loadWebView(url:String){
        progressBar.progress = 0
        webView?.loadUrl(url)
    }
    private fun loadSplashPage(){
        progressBar.visibility = View.INVISIBLE
        webView?.clearHistory()
        clearHistory = true
        webView?.loadUrl("about:blank")
        splashPage.visibility = View.VISIBLE
        webView?.visibility = View.GONE
    }
    private fun loadMainPage(){
        splashPage.visibility = View.GONE
        webView?.visibility = View.VISIBLE
    }
    private fun hideKeyboard(){
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken,0)
    }
    fun progressBarUpdate(newProgress:Int){
        if(newProgress == 100){
            progressBar.visibility = View.INVISIBLE
            isLoading = false
        }
        else{
            isLoading = true
            if(progressBar.visibility != View.VISIBLE) {
                progressBar.visibility = View.VISIBLE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                progressBar.setProgress(newProgress,true)
            }
            else{
                progressBar.progress = newProgress
            }
        }
    }
    interface DoubleClick{
        fun hideBar()
        fun save(url: String,path:String)
        fun giveUrl(path:String): String?
        fun path():String
        fun delete(path: String)
    }
}
