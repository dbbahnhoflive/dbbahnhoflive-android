package de.deutschebahn.bahnhoflive.util

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.ui.map.MapConsentDialogFragment
import de.deutschebahn.bahnhoflive.ui.map.OnMapConsentDialogListener

fun <K, V> Map<K, V>?.asMutable() =
    this?.toMutableMap() ?: mutableMapOf()


class GoogleLocationPermissions {

    companion object {

        private fun showExplanation(
            context: Context,
            permissionCheckFunction: () -> Unit,
            permissionResponseFunction: (response: Boolean) -> Unit,
            title: String,
            message: String
        ) {

            val builder: android.app.AlertDialog.Builder =
                android.app.AlertDialog.Builder(context, R.style.App_Dialog_Theme)
            builder.setMessage(message)

/*        setTitle(title)*/
                .setCancelable(false)
                .setPositiveButton(R.string.tutorial_button_location_permission,
                    DialogInterface.OnClickListener { dialog, id ->
                        permissionCheckFunction()

                    })
                .setNegativeButton(
                    R.string.dlg_cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        permissionResponseFunction(false)
                    })
            builder.create().show()
        }


        // returns true if ONE of the permissions is granted
        private fun checkSelfPermissions(
            activity: Activity,
            permissionNames: Array<String>
        ): Boolean {
            permissionNames.forEach {

                val permission = ContextCompat.checkSelfPermission(
                    activity,
                    it,
                )

                if (permission == PackageManager.PERMISSION_GRANTED)
                    return true
            }
            return false
        }


        private fun askForGoogleLocationPermissionIfNecessary(
            baseActivity: BaseActivity,
            permissionResponse: (response: Boolean) -> Unit
        ) {

            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, // eigener Standort
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            if (!checkSelfPermissions(baseActivity, permissions)) { // ask only from time to time. if atleast 1 permission is granted

                val shouldShowACCESS_FINE_LOCATION =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        baseActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )

                val shouldShowACCESS_COARSE_LOCATION =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        baseActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )


                if (shouldShowACCESS_FINE_LOCATION || shouldShowACCESS_COARSE_LOCATION) {

                    showExplanation(
                        baseActivity,
                        {
                            baseActivity.requestAtleastOneOfThePermissions(permissions, permissionResponse)
                        },
                        permissionResponse,
                        "",
                        baseActivity.getText(R.string.notice_location_permissions_missing)
                            .toString()

                    )

                    return

                }

                // guideline google
                baseActivity.requestAtleastOneOfThePermissions(permissions, permissionResponse)
            }
            else
             permissionResponse(true)
        }



        @JvmStatic
        fun startMapActivityIfConsent(fragment: Fragment, createMapIntentFunction: () -> Intent?) {

            fun permissionResponse(allowed: Boolean) {
                // google-permissions ok, ask for db-consent
                if (allowed) {
                        createMapIntentFunction()?.let { fragment.startActivity(it) }
                }
            }
        })
        mp.show(fragment.parentFragmentManager, null)
    } else {
        createMapIntentFunction()?.let{fragment.startActivity(it)}
    }

}
