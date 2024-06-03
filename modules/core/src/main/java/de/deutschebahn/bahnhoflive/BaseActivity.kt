package de.deutschebahn.bahnhoflive

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


open class BaseActivity : AppCompatActivity() {

    private var permissionResponseFunction: ((response: Map<String, Boolean>) -> Unit)? = null

    // must be initialized very early, not possible after onCreate !
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            permissionResponseFunction?.invoke(isGranted) //isGranted.count { it.value } > 0)
            permissionResponseFunction=null
        }

    fun requestAtleastOneOfThePermissions(permissionNames:Array<String>, permissionResponse: (response: Map<String, Boolean>) -> Unit) {
        permissionResponseFunction=permissionResponse
        requestPermissionLauncher.launch(permissionNames)
    }

    fun requestAllPermissions(permissionNames:Array<String>, permissionResponse: (response: Map<String, Boolean>) -> Unit) {
        permissionResponseFunction=permissionResponse
        requestPermissionLauncher.launch(permissionNames)
    }

    fun getTheLauncher(permissionResponse: (response: Map<String, Boolean>) -> Unit) : ActivityResultLauncher<Array<String>> {
        permissionResponseFunction=permissionResponse
        return requestPermissionLauncher
    }

}