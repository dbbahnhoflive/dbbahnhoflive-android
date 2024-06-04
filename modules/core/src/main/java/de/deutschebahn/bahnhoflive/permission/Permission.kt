/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat

class Permission private constructor(val name: String, val requestCode: Int) :
    OnRequestPermissionsResultCallback {
    interface Listener {
        fun onPermissionChanged(permission: Permission)
    }

    var isGranted: Boolean = false
        get() = field
        private set(granted) {
            if (field != granted) {
                field = granted
                notifyListeners()
            }
        }

    private val listeners: MutableList<Listener> = ArrayList()


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == this.requestCode) {
            for (i in permissions.indices) {
                if (name == permissions[i]) {
                    isGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED
                }
            }
        }
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.onPermissionChanged(this)
        }
    }

    fun update(context: Context) {
        isGranted = isGranted(context)
    }

    private fun isGranted(context: Context): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, name)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    /**
     * When requesting a permission, the user may choose to permanently deny that permission.
     * The only way for us to get informed about that condition is this method. Unfortunately
     * this also returns true before the very first request of each permission.
     *
     * @param activity the current Activity
     * @return `true` if showing the permission request popup might fail
     */
    fun isPermanentlyDeniedOrFreshInstallation(activity: Activity): Boolean {
        return !(ActivityCompat.shouldShowRequestPermissionRationale(activity, name)
                || isGranted(activity))
    }

    fun request(activity: Activity?) {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(name),
            requestCode
        )
    }

    fun shouldShowRequestPermissionRationale(activity: Activity?): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity!!, LOCATION.name)
    }

    fun openAppSystemSettings(activity: Activity ) {
        with(activity) {
            startActivity(Intent().apply {
                flags =
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = android.net.Uri.fromParts("package", packageName, null)
            })
        }
    }

    fun showPermissionRationaleOrAskToGoForSystemSettings(activity:Activity, showDialog: (acceptor: () -> Unit) -> Unit  ) {
        if(shouldShowRequestPermissionRationale(activity))
            request(activity)
        else {
            showDialog {
                openAppSystemSettings(activity)
            }

        }
    }

    companion object {
        @JvmField
        val LOCATION: Permission = Permission(Manifest.permission.ACCESS_FINE_LOCATION, 815)
    }
}
