package de.deutschebahn.bahnhoflive.ui.dbcompanion

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import de.deutschebahn.bahnhoflive.BaseActivity
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.util.AlertX

class DbCompanionActivity : BaseActivity() {

    private val webView by lazy { findViewById<WebView>(R.id.webview) }

    class JsWebInterface(val activity: Activity) {
        @JavascriptInterface
        fun postMessage(cmd:String?) {
           val clean_cmd = cmd?:""
           if(clean_cmd.contentEquals("close", true))
            activity.finish()
//            exitProcess(0)
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_db_companion_video_call)

        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.setSupportMultipleWindows(false)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true

        settings.userAgentString += " BahnhofLive/VERSION (BahnhofLive-iOS)"
//        webView.setWebViewClient(WebViewClient())

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                if(handler!=null && error!=null && error.getPrimaryError()== SslError.SSL_DATE_INVALID) {
                    handler.proceed() // todo: remove in release ?
                }
                else
                 super.onReceivedSslError(view, handler, error) // todo: insert in release
            }
        }

        webView.addJavascriptInterface(JsWebInterface(this), "BahnhofLive")

        val ctx :  Context = this


        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                val grantedPermissions = mutableListOf<String>()
                val permissionToRequest = mutableListOf<String>()
                if (request?.resources?.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) == true) {
                    if (ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.CAMERA
                        ) != PermissionChecker.PERMISSION_GRANTED
                    ) {
                        println("NEED TO REQUEST CAMERA")
                        permissionToRequest.add(Manifest.permission.CAMERA)
                    } else {
                        grantedPermissions.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)      //first permission for Camera , just to show it is requested
                    }
                }
                if (request?.resources?.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE) == true) {
                    if (ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.RECORD_AUDIO
                        ) != PermissionChecker.PERMISSION_GRANTED
                    ) {
                        println("NEED TO REQUEST AUDIO")
                        permissionToRequest.add(Manifest.permission.RECORD_AUDIO)
                    } else {
                        grantedPermissions.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)       //second for  Audio, just to show it is requested
                    }
                }
                if (permissionToRequest.isNotEmpty()) {
                    println("LAUNCH PERMISSIONLAUNCHER")
                    requestPermissionsWindow()
                }
                if (grantedPermissions.isNotEmpty()) {
                    request?.grant(grantedPermissions.toTypedArray())
                }
            }

            //Permission for Geolocations is handled separately
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
            }
        }

        val url = getString(R.string.rail_replacement_db_companion_video_call_url)
        webView.loadUrl(url)

//        webView.loadUrl("file:///android_asset/close_test.html");


    }

        private fun requestPermissionsWindow() {
        val permission = requestUserPermissions()
        val showRational = permission.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
        if (showRational && Build.VERSION.SDK_INT >= 23) {

            AlertX.execAlert(this,
                getString(R.string.permissionrequest_title),
                getString(R.string.permissionrequest_message),
                AlertX.buttonPositive(),
                getString(R.string.permissionrequest_accept), {
                    requestPermissions(permission.toTypedArray(), 1)
                },
                getString(R.string.permissionrequest_deny), {
                }
            )

        } else {
            ActivityCompat.requestPermissions(
                this,
                permission.toTypedArray(),
                1
            )
        }
    }

    private fun requestUserPermissions(): MutableList<String> {
        println("REQUEST PERMISSIONS!")
        val permissionToRequest = mutableListOf<String>()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("Missing permissions for CAMERA")
            permissionToRequest.add(Manifest.permission.CAMERA)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("Missing permissions for RECORD_AUDIO")
            permissionToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("Missing permissions for FINE_LOCATION")
            permissionToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return permissionToRequest
    }

    inner class CustomClient: WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            if (url.startsWith("intent://")) {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                if (intent != null) {
                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                    return if (fallbackUrl != null) {
                        webView.loadUrl(fallbackUrl)
                        true
                    } else {
                        false
                    }
                }
            }

            return false
        }
    }

}
