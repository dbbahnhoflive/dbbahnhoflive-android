package de.deutschebahn.bahnhoflive.debug

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper

// this static class defines items which are used from ContentProvider AND Application



object InMemoryDbProviderInterface {

    const val PROVIDER_NAME =
        "de.deutschebahn.bahnhoflive.debug.provider" // same as manifest -> provider android:authorities

    const val TABLE_NAME_VALUES = "table_key_value"
    const val field_name_values_id = "id"
    const val field_name_values_key = "key_name"
    const val field_name_values_type = "var_type"
    const val field_name_values_value = "var_value"

    const val TABLE_NAME_DEBUG  = "table_debug"
    const val field_name_debug_receive_date_time = "receive_date_time"
    const val field_name_debug_key = "debug_key"
    const val field_name_debug_value = "debug_value"


    class DataChangeObserver(private val context:Context, val changedValueHandler : (key:String, value:String)->Unit)
        : ContentObserver(Handler(Looper.getMainLooper())) {

//    override fun deliverSelfNotifications(): Boolean {
//        return false
//    }
//
//    override fun onChange(selfChange: Boolean) { // Added in API level 1
//    }

        override fun onChange(selfChange: Boolean, uri: Uri?) { // Added in API level 16
            if (uri == null) return
            try {
                handleChangedValue(ContentUris.parseId(uri))
            } catch (e: Exception) {
                // todo: log
            }
        }


        private fun handleChangedValue( rowid : Long) {

            val requestedColumns = arrayOf<String>(
                field_name_values_key,
                field_name_values_value
            )

            val cursor : Cursor? = context.contentResolver.query(
                getUri(TABLE_NAME_VALUES),
                requestedColumns,
                "rowid=$rowid",
                null, null
            )

            cursor?.let {

                if (it.moveToFirst()) {
                    if (!it.isAfterLast) {

                        val idxKey = it.getColumnIndex(field_name_values_key)
                        val idxValue = it.getColumnIndex(field_name_values_value)

                        val key = it.getString(idxKey).lowercase()
                        if(key.isNotEmpty() && key[0]=='#') { // from debug-app ?
                          try {
                              changedValueHandler(key, it.getString(idxValue))
                          }
                          catch(_:java.lang.Exception) {

                          }
                        }
                    }
                }
                it.close()
            }
        }
    }



    fun getUrl(tableName:String) : String {
        return "content://${PROVIDER_NAME}/${tableName}"
    }

    fun getUri(tableName:String) : Uri {
        return Uri.parse(getUrl(tableName))
    }

    fun setValue(context: Context, key:String, value:String) {

        val values = ContentValues()
        values.put(field_name_values_key, key)
        values.put(field_name_values_type, 0)
        values.put(field_name_values_value, value)

        try {
            context.contentResolver.insert(
                getUri(
                    TABLE_NAME_VALUES
                ), values
            )
        } catch (e: Exception) {

        }
    }

    fun registerValueChangeObserver(  context:Context, changedValueHandler : (key:String, value:String)->Unit) : ContentObserver {

        val dataChangeObserver = DataChangeObserver(context, changedValueHandler)

        context.contentResolver.registerContentObserver(
            getUri(TABLE_NAME_VALUES),
            true, dataChangeObserver)

        return dataChangeObserver
    }

    fun isContentProviderValid(context: Context): Boolean {

        var ret = false

        try {
            ret = context.contentResolver.getType(getUri(TABLE_NAME_VALUES)) != null
        } catch (_: Exception) {
        }

        return ret
    }

    fun unregisterValueChangeObserver(context:Context, observer:ContentObserver ) {
        try {
            context.contentResolver.unregisterContentObserver(observer)
        }
        catch(_:Exception) {

        }
    }

}


// todo: eventuell den Providernamen aus den package-Informationen holen
// man könnte das label als Schlüssel benutzen...

//    fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
//        } else {
//            @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
//        }

//        val packageManager = context.packageManager
//
//        if (packageManager != null) {
//            val packageInfo = packageManager.getPackageInfoCompat(InMemoryDbProviderInterface.PROVIDER_NAME, GET_PROVIDERS)

//            for (pack in packageManager.getInstalledPackages(PackageManager.GET_PROVIDERS)) {
//                val providers = pack.providers
//                if (providers != null) {
//                    for (provider in providers) {
//                        Log.d("Example", "provider: " + provider.authority)
//                        packageManager?.let { provider.loadLabel(it).toString() }?.let { Log.d("cr", it) }
//                    }
//                }
//            }
//        }