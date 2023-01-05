package de.deutschebahn.bahnhoflive

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


open class BaseActivity : AppCompatActivity() {

    private var permissionResponseFunction: ((response: Boolean) -> Unit)? = null

    // must be initialized very early, not possible after onCreate !
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