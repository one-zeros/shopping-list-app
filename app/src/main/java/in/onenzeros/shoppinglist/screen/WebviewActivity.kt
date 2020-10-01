package `in`.onenzeros.shoppinglist.screen

import `in`.onenzeros.shoppinglist.R
import `in`.onenzeros.shoppinglist.databinding.ActivityWebviewBinding
import `in`.onenzeros.shoppinglist.utils.BaseActivity
import `in`.onenzeros.shoppinglist.utils.EventObserver
import `in`.onenzeros.shoppinglist.viewModel.WebviewActivityViewModel
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider


class WebviewActivity : BaseActivity() {

    companion object{
        val SYORY_LINK = "https://quickshoppinglist.com/story.html?source=app"
        val ABOUT_LINK = "https://quickshoppinglist.com/about.html"
        val HELP_LINK = "https://www.youtube.com/watch?v=8lfitdnMaEs&feature=youtu.be"
        val ARG_URL = "arg_url"
    }

    private var binding: ActivityWebviewBinding? = null
    lateinit var viewModel: WebviewActivityViewModel
    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_webview)
        viewModel =  ViewModelProvider(this).get(WebviewActivityViewModel::class.java)
        binding?.viewModel = viewModel
        binding?.lifecycleOwner = this
        binding?.executePendingBindings()
        initWebView()

        initObserver()
    }

    private fun initWebView() {
        binding?.webView?.webViewClient = MyWebViewClient(this)
        binding?.webView?.webChromeClient = WebChromeClient()
        url = intent.extras?.getString(ARG_URL).toString()

        binding?.webView?.requestFocus()
        binding?.webView?.settings?.lightTouchEnabled = true
        binding?.webView?.settings?.javaScriptEnabled = true
        binding?.webView?.settings?.setGeolocationEnabled(true)
        binding?.webView?.isSoundEffectsEnabled = true
        binding?.webView?.loadUrl(url)
    }

    private fun initObserver() {
        viewModel.onToolbarNavigationClickEvent.observe(this, EventObserver {
           finish()
        })
    }

    class MyWebViewClient internal constructor(private val activity: Activity) : WebViewClient() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url: String = request?.url.toString()
            view?.loadUrl(url)
            return false
        }

        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            webView.loadUrl(url)
            return false
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            Toast.makeText(activity, "Got Error! $error", Toast.LENGTH_SHORT).show()
        }
    }
}