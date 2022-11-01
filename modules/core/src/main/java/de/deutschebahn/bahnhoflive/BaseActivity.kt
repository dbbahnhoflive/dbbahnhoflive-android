package de.deutschebahn.bahnhoflive

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


open class BaseActivity : AppCompatActivity() {

    private var permissionResponseFunction: ((response: Boolean) -> Unit)? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGrantedMap ->
            permissionResponseFunction?.invoke(isGrantedMap.count { it.value } > 0)
            permissionResponseFunction=null
        }

    fun requestAtleastOneOfThePermissions(permissionNames:Array<String>, permissionResponse: (response: Boolean) -> Unit) {
        permissionResponseFunction=permissionResponse
        requestPermissionLauncher.launch(permissionNames)
    }

}