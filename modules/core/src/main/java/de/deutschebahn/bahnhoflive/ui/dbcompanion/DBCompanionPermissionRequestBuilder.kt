package de.deutschebahn.bahnhoflive.ui.dbcompanion

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import de.deutschebahn.bahnhoflive.BaseActivity
import de.deutschebahn.bahnhoflive.BaseApplication


class DBCompanionPermissionRequestBuilder {
    enum class PermState {
        PERMISSION_DENIED,
        PERMISSION_GRANTED,
        REQUEST
    }

    sealed class DBPermRequest(private val config: DBPermissionFactory) {
        var cbOnPermissionsGranted: () -> Unit =
            { println("All permissions are accepted. !! No Option set to launch!!") }
        private val permRequester: ActivityResultLauncher<Array<String>> =
            (BaseApplication.activityManager.activity as BaseActivity).registerResponseFunction { result ->
            val deniedPermissions = result.filterNot { it.value }
            // This condition is met if the user did not accept all relevant permissions
            if (deniedPermissions.isNotEmpty()) {
                val denied = deniedPermissions.map { it.key }
                config.cbPermissionDenied(this, denied)
            }
            // All needed permissions are granted
            else {
                cbOnPermissionsGranted()
            }
        }

        private fun getPermissionState(permission: String): PermState = run {
            with(config.activity) {
                val notGranted = ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED

                // Check for Runtime-permissions since android API 23
                if (Build.VERSION.SDK_INT >= 23) {

                    when {
                        // this condition only becomes true if the user has denied the permission previously
                        notGranted -> if (shouldShowRequestPermissionRationale(permission)) {
                            PermState.PERMISSION_DENIED
                        } else PermState.REQUEST

                        else -> PermState.PERMISSION_GRANTED
                    }
                }
                // No Runtime-permissions in android API < 23 -> granted on Installation so always granted
                else PermState.PERMISSION_GRANTED

            }
        }

        private fun printPermissionState(stage: String) {
            val mapped = config.permissions.map { it to getPermissionState(it) }
            val granted =
                mapped.filter { it.second == PermState.PERMISSION_GRANTED }.map { it.first }
            val denied = mapped.filter { it.second == PermState.PERMISSION_DENIED }.map { it.first }
            val request = mapped.filter { it.second == PermState.REQUEST }.map { it.first }
            println("$stage [Granted]: ").also { granted.forEach { println("\t$it") } }
            println("$stage [Denied]: ").also { denied.forEach { println("\t$it") } }
            println("$stage [Not Granted]: ").also { request.forEach { println("\t$it") } }
        }

        fun permissionsRequestUser() {
            printPermissionState("REQUEST Perm ")
            val mapped = config.permissions.map { it to getPermissionState(it) }
            // Permissions with state PermState.PERMISSION_DENIED and PermState.REQUEST
            val notGrantedPermissions =
                mapped.filter { it.second != PermState.PERMISSION_GRANTED }.map { it.first }
            val deniedPermissions =
                mapped.filter { it.second == PermState.PERMISSION_DENIED }.map { it.first }
                    .isNotEmpty()
            // there are permissions, the user denied previously
            if (deniedPermissions) {
                config.permissionRequestDialogCallback {
                    openPermissionRequestDialogs(
                        notGrantedPermissions
                    )
                }
            }
            // only show Permission-request-dialog if there are outstanding permissions
            else if (notGrantedPermissions.isNotEmpty()) {
                openPermissionRequestDialogs(notGrantedPermissions)
            }
            // continue loading if all needed permission are accepted
            else {
                cbOnPermissionsGranted()
            }
        }

        private fun openPermissionRequestDialogs(permissions: List<String>) {
            permRequester.launch(permissions.toTypedArray())
        }

        fun openAppSystemSettings() {
            with(config.activity) {
                startActivity(Intent().apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", packageName, null)
                })
            }
        }

        fun unregisterResponseFunction() {
            (BaseApplication.activityManager.activity as BaseActivity).unregisterResponseFunction()
        }
    }

    private class PrivatePermissionRequest(config: DBPermissionFactory) : DBPermRequest(config)
    private class PrivatePermissionFactory(activity: ComponentActivity) :
        DBPermissionFactory(activity)

    sealed class DBPermissionFactory(var activity: ComponentActivity) {
        var permissions: Set<String> = emptySet()
        var permissionRequestDialogCallback: (accepted: () -> Unit) -> Unit = {}
        var cbPermissionDenied: (DBPermRequest, denied: List<String>) -> Unit = { _, _ -> }
        fun build(onSuccess: () -> Unit): DBPermRequest = PrivatePermissionRequest(this).apply {
            cbOnPermissionsGranted = onSuccess
        }
    }

    companion object {
        fun from(
            activity: ComponentActivity,
            config: DBPermissionFactory.() -> Unit
        ): DBPermissionFactory = PrivatePermissionFactory(activity).apply(config)
    }
}
