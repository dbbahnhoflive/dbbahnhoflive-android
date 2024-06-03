package de.deutschebahn.bahnhoflive.ui.dbcompanion

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import de.deutschebahn.bahnhoflive.BaseActivity
import de.deutschebahn.bahnhoflive.R

class DbCompanionActivity : BaseActivity() {

    private val webView by lazy { findViewById<WebView>(R.id.webview) }

    private var isBackpressedOnce = false
    /**
     * SYSTEM_CALLBACK: This callback is used, to ensure the applications close behaviour
     */
//    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
//        override fun handleOnBackPressed() {
//            if (isBackpressedOnce) {
//                finishAffinity()
//                exitProcess(0)
//            }
//            isBackpressedOnce = true
//            Toast.makeText(applicationContext, "Press again to exit.", Toast.LENGTH_SHORT).show()
//            Handler(Looper.getMainLooper()).postDelayed({ isBackpressedOnce = false }, 2000)
//        }
//    }
    /**
     * DIALOG: This Dialog is opened, if the user has previously denied the use of essential permissions
     */
    private fun showPermissionRequestDialog(accepted: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.permissionrequest_title)
            .setMessage(R.string.permissionrequest_message)
            .setPositiveButton(R.string.permissionrequest_accept) { _, _ ->
                // all outstanding and denied permissions must be accepted
                accepted()
            }
            .setNegativeButton(R.string.permissionrequest_deny) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    /**
     * DIALOG: Open Dialog to the Application`s settings, to guide the user for essential permissions
     */
    private fun showPermissionDeniedDialog(
        request: DBCompanionPermissionRequestBuilder.DBPermRequest,
        denied: List<String>
    ) {
        AlertDialog.Builder(this)
            .setTitle("Webbegleitung keine Erlaubnis")
            .setMessage("Um die Wegbegleitung verwenden zu können, sind folgende Berechtigungen notwendig: \n\n $denied\n\n Möchten sie ihre Auswahl ändern?")
            .setPositiveButton("Einstellungen") { dialog, _ ->
                dialog.dismiss()
                request.openAppSystemSettings()
            }
            .setNegativeButton(R.string.permissionrequest_deny) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    /**
     * INTENT: Start the webview after all permissions have been granted by the user
     */
//    private fun showWebView() {
//        startActivity(
//            this,
//            Intent(this, CustomWebView::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
//            }, null
//        )
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_db_companion_video_call)
        //initialize Permission-Launcher
        val dbPermRequest = DBCompanionPermissionRequestBuilder
            .from(this) {
                permissions = setOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                permissionRequestDialogCallback = ::showPermissionRequestDialog
                cbPermissionDenied = ::showPermissionDeniedDialog
            }
            .build {
//                showWebView()
            }

        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.setSupportMultipleWindows(false)
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.domStorageEnabled = true
//        settings.databaseEnabled = true

        webView.setWebViewClient(CustomClient())
        webView.setWebChromeClient(WebChromeClient())

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
                    //todo AlertDialog
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
        webView.loadUrl("https://de.webcamtests.com/")
//                webView.loadUrl("https://dev.help-me-iat.comp.db.de/")
    }

        private fun requestPermissionsWindow() {
        val permission = requestUserPermissions()
        val showRational = permission.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
        if (showRational) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog
                .setTitle("Webbegleitung Erlaubnisanfrage")
                .setMessage("Um die Wegbegleitung zu nutzen, sind die Kamera, das Mikrofon und der akutelle Standort notwendig.")
                .setPositiveButton("Erlauben") { _, _ ->
                    requestPermissions(permission.toTypedArray(), 1)
                }
                .setNegativeButton("Ablehnen") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                permission.toTypedArray(),
                1)
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

//@SuppressLint("SetJavaScriptEnabled")
//@Composable
//fun BahnhofLiveDemoWebView(context: Context) {
//    val url = stringResource(id = R.string.wegbegleiter_test)
//    AndroidView(factory = {
//        WebView(context).apply {
//            settings.javaScriptEnabled = true       // need to be enabled
//            settings.domStorageEnabled =
//                true       // need to be enabled for now, Milan mentioned this might not be nessesary later on
//            webViewClient = WebViewClient()         // custom Client
//            webChromeClient = object :
//                WebChromeClient() {     // webChromeClient handles the permission requests in the WebView
//                override fun onPermissionRequest(request: PermissionRequest?) {
//                    val permissions = mutableListOf<String>()
//                    if (request?.resources?.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE) == true) {
//                        permissions.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)      //first permission for Camera , just to show it is requested
//                    }
//                    if (request?.resources?.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE) == true) {
//                        permissions.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)       //second for  Audio, just to show it is requested
//                    }
//                    request?.grant(permissions.toTypedArray())
//                }
//                //Permission for Geolocations is handled separately
//                override fun onGeolocationPermissionsShowPrompt(
//                    origin: String?,
//                    callback: GeolocationPermissions.Callback?
//                ) {
//                    callback?.invoke(origin, true, false)
//                }
//            }
//            loadUrl(url) //open the URL
//        }
//    })
//}
//@Composable
//fun BahnhofLiveDemo(
//    context: Context,
//    permissionRequest: DBCompanionPermissionRequestBuilder.DBPermRequest,
//    modifier: Modifier = Modifier
//) {
//    val url = stringResource(id = R.string.wegbegleiter_test)
//    // sets the click-callback
//    permissionRequest.cbOnPermissionsGranted = { webViewCall(url, context) }
//    Column(
//        verticalArrangement = Arrangement.spacedBy(
//            space = 20.dp,
//            alignment = Alignment.CenterVertically
//        ),
//        modifier = modifier.fillMaxSize()
//    ) {
//        StartVideoCallButton(
//            label = "WebView Videocall starten",
//            onClick = { permissionRequest.permissionsRequestUser() }
//        )
//        StartVideoCallButton(
//            label = "Custom Tab Videocall starten",
//            onClick = { customTabWebViewCall(url, context) }
//        )
//    }
//}
//@Composable
//fun StartVideoCallButton(
//    label: String,
//    onClick: () -> Unit
//) {
//    Button(
//        onClick = onClick,
//        shape = RoundedCornerShape(50),
//        modifier = Modifier
//            .fillMaxWidth()
//    ) {
//        Text(text = label)
//    }
//}
//Custom WebView
//@SuppressLint("SetJavaScriptEnabled")
//fun webViewCall(url: String, context: Context) {
//    startActivity(context, Intent(context, CustomWebView::class.java), null)
//}
//Custom Tab in Chrome
//fun customTabWebViewCall(url: String, context: Context) {
//    val intent = CustomTabsIntent.Builder()
//        .setShowTitle(false)
//        .setUrlBarHidingEnabled(true)
//        .build()
//    intent.launchUrl(context, Uri.parse(url))
//}

//@Preview
//@Composable
//fun LivePreview(modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//    BahnhofLiveDemoWebView(context = context)
//}
//

