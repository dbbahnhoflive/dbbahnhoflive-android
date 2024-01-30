package de.deutschebahn.bahnhoflive.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import de.deutschebahn.bahnhoflive.BaseActivity
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.ui.map.MapConsentDialogFragment
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.MapViewModel
import de.deutschebahn.bahnhoflive.ui.map.OnMapConsentDialogListener


fun <K, V> Map<K, V>?.asMutable() =
    this?.toMutableMap() ?: mutableMapOf()


class GoogleLocationPermissions {

    companion object {

        private fun showExplanation(
            context: Context,
            permissionCheckFunction: () -> Unit,
            permissionResponseFunction: (response: Boolean) -> Unit,
            @Suppress("UNUSED_PARAMETER", "SameParameterValue")
            title: String,
            message: String
        ) {

            val builder: android.app.AlertDialog.Builder =
                android.app.AlertDialog.Builder(context, R.style.App_Dialog_Theme)
            builder.setMessage(message)

/*        setTitle(title)*/
                .setCancelable(false)
                .setPositiveButton(R.string.tutorial_button_location_permission,
                    DialogInterface.OnClickListener { _, _ ->
                        permissionCheckFunction()

                    })
                .setNegativeButton(
                    R.string.dlg_cancel,
                    DialogInterface.OnClickListener { _, _ ->
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
                    createMapIntentFunction()?.let {
                        if (fragment is MapPresetProvider) {
                            (fragment as MapPresetProvider).prepareMapIntent(it)
                        }
                        fragment.startActivity(it)
                    }
                }
            }

            @Suppress("UNUSED")
            fun showMapButAskForLocationPermissionsIfNecessary(baseActivity: BaseActivity) {
                // consent if ok, but - if needed - ask for Location-Permissions
                askForGoogleLocationPermissionIfNecessary(baseActivity, ::permissionResponse)
            }

            try {
                val baseActivity: BaseActivity? = fragment.requireActivity() as? BaseActivity

                if (baseActivity == null) {
                    val activityName: String? = fragment.activity?.localClassName
                    if (activityName != null)
                        Log.d(
                            "cr",
                            "class $activityName has to inherit from BaseActivity to use map !!!!!!!!!"
                        )
                    else
                        Log.d(
                            "cr",
                            "activity from class ${fragment.javaClass.name} has to inherit from BaseActivity to use map !!!!!!!!!"
                        )

                } else {

                    val mapViewModel: MapViewModel =
                        ViewModelProvider(baseActivity)[MapViewModel::class.java]



                    if (mapViewModel.mapConsentedLiveData.value == false && BuildConfig.DBG_MAP_CONSENT_OVERRIDE==false) {
                        MapConsentDialogFragment().setOnMapConsentDialogListener(object :
                            OnMapConsentDialogListener {

                            override fun onConsentAccepted() {
                                permissionResponse(true)
//                                showMapButAskForLocationPermissionsIfNecessary(baseActivity)
                            }

                        })
                            .show(fragment.parentFragmentManager, null)
                    } else
                        permissionResponse(true)
//                        showMapButAskForLocationPermissionsIfNecessary(baseActivity)


                }
            } catch (e: Exception) {
                e.message?.let { Log.d("cr", it) }
            }
        }




    }
}
