package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import de.deutschebahn.bahnhoflive.ui.accessibility.GlobalPreferences
import de.deutschebahn.bahnhoflive.ui.accessibility.TrackingPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class VersionManager private constructor(manager: PackageManager, packageName : String, val global_preferences:SharedPreferences, tracking_preferences:SharedPreferences) {

    class SoftwareVersion(val versionName: String) : Comparable<SoftwareVersion> { // versionString can be rc3.20.1-demo or 3.20.1 or ...

        var major: Long = 0L
            private set

        var minor: Long = 0L
            private set

        var patch: Long = 0L
            private set

        fun asVersionLong() : Long = major*100000000L + minor*1000L + patch
        fun asVersionStringWithoutDecoration() : String = String.format("%d.%d.%d", major, minor, patch)

        init {
            val parts = versionName.replace(Regex("[^0-9.]"), "").split(".")
            if (parts.size == 3) {
                major = parts[0].toLongOrNull() ?: 0L
                minor = parts[1].toLongOrNull() ?: 0L
                patch = parts[2].toLongOrNull() ?: 0L
            }
        }

        override fun equals(other: Any?) =
            ((other is SoftwareVersion) && (major == other.major)
                    && (minor == other.minor)
                    && (patch == other.patch))

        override fun compareTo(other: SoftwareVersion) =
            asVersionLong().compareTo(other.asVersionLong())

        override fun hashCode(): Int {
            var result = major.hashCode()
            result = 31 * result + minor.hashCode()
            result = 31 * result + patch.hashCode()
            return result
        }
    }

    val actualVersion : SoftwareVersion
        get() = _actualVersion

    val lastVersion : SoftwareVersion
        get() = _lastVersion

    val isFreshInstallation : Boolean
        get() = _isFreshInstallation

    val usageCountDays : Int
        get() = _usageCountDays

    var pushWasEverUsed : Boolean
     get() = _pushWasEverUsed
     set(value) {
         if(value!=_pushWasEverUsed) {
             global_preferences.edit()
                 .putBoolean("PushWasEverUsed", value)
                 .apply()
         }
         _pushWasEverUsed = value
     }

    var pushTutorialGeneralShowCounter: Int
        get() = _pushTutorialGeneralShowCounter
        set(value) {
            if(value!=_pushTutorialGeneralShowCounter) {
                global_preferences.edit()
                    .putInt("PushTutorialGeneralShowCounter", value)
                    .apply()
            }
            _pushTutorialGeneralShowCounter = value
        }


    fun isUpdate() : Boolean  {
        if(_isFreshInstallation) return false
        return (_actualVersionFromFile.asVersionLong()!=0L) && _actualVersion != _actualVersionFromFile
    }

    private var _isFreshInstallation : Boolean = false

    private var _lastVersion : SoftwareVersion = SoftwareVersion("")
    private var _actualVersion : SoftwareVersion = SoftwareVersion("")
    private var _actualVersionFromFile : SoftwareVersion = SoftwareVersion("")

    private var _usageCountDays : Int = 0

    private var _pushWasEverUsed : Boolean = false
    private var _pushTutorialGeneralShowCounter : Int = 0

    init {

        // get actual version from package
        val packageInfo : PackageInfo? = getPackageInfoCompat(manager, packageName, PackageManager.GET_META_DATA)
        packageInfo?.let {
            _actualVersion = SoftwareVersion(packageInfo.versionName)  // bbNUM.NUM.NUMcc 3.20.1-demo  rc3.21.1
        }

        // get actual version from file
        _actualVersionFromFile = SoftwareVersion(global_preferences.getString("ActualVersion", "") ?: "")

        // get last version from file
        _lastVersion = SoftwareVersion(global_preferences.getString("LastVersion", "") ?: "")

       // check if installation is fresh (tracking-permission is asked for after installation and creates
       // tracking_preferences-file), VersionManager is initialized BEFORE in HubActivity
        _isFreshInstallation = tracking_preferences.contains("consentState")==false


        // _actualVersionFromFile nach LastVersion speichern +
        // actualVersion nach ActualVersion speichern

        // aktuelle Version in LastVersion speichern, wenn _actualVersion!=_actualVersionFromFile
        if(_actualVersion!=_actualVersionFromFile) {

            global_preferences.edit()
                .putString(
                    "LastVersion",
                    _actualVersionFromFile.asVersionStringWithoutDecoration()
                ) // no decoration
                .putString("LastVersionName", _actualVersionFromFile.versionName)
                .putString("ActualVersion", _actualVersion.asVersionStringWithoutDecoration())
                .putString("ActualVersionName", _actualVersion.versionName)
                .apply()
        }

        val restartedDayCount = global_preferences.getInt("restartedDayCount", 0 )
        val lastDayStarted = global_preferences.getInt("lastStartedDay", 0 )


        val usageLastDateInFile : String = global_preferences.getString("UsageLastDate", "") ?: ""
        _usageCountDays = global_preferences.getInt("UsageCountDays", 0)

        val cal = Calendar.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY)
//        val dayAsString = today.plusDays((_usageCountDays+1).toLong()).toString() // test only, simulate daychange every time the app restarts
        val dayAsString = today.format(cal.time)

        if(dayAsString != usageLastDateInFile) {
            _usageCountDays++
            global_preferences.edit()
                .putString("UsageLastDate", dayAsString)
                .putInt("UsageCountDays", _usageCountDays)
                .apply()
        }

        _pushWasEverUsed = global_preferences.getBoolean("PushWasEverUsed", false)
        _pushTutorialGeneralShowCounter = global_preferences.getInt("PushTutorialGeneralShowCounter", 0)

    }




    private fun getPackageInfoCompat(manager: PackageManager, packageName: String, flags: Int = 0): PackageInfo? =

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                manager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(flags.toLong())
                )
            } else {
                @Suppress("DEPRECATION") manager.getPackageInfo(packageName, flags)
            }
        } catch (e: Exception) {
            null
        }

    companion object {
        @Volatile private var instance : VersionManager? = null

        fun  getInstance(context: Context) : VersionManager {

            if (instance == null)
                instance = VersionManager(context.packageManager, context.packageName, context.GlobalPreferences, context.TrackingPreferences)

            return instance!!
        }

    }


}