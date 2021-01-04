package de.deutschebahn.bahnhoflive.repository.feedback

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.R
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

class WhatsAppContacts(context: Context) : LiveData<Map<String, String>>() {

    private val context = context.applicationContext

    private var initialize = true

    companion object {
        private val TAG: String
            get() = WhatsAppContacts::class.java.simpleName
    }

    private fun loadData() {
        try {
            val result = mutableMapOf<String, String>()
            InputStreamReader(
                context.resources.openRawResource(R.raw.whatsapp_contacts),
                StandardCharsets.UTF_8.name()
            ).forEachLine { line ->
                line.split(';').takeIf { it.size == 3 }?.also { parts ->
                    result[parts[0]] = parts[2]
                } ?: kotlin.run {
                    Log.i(TAG, "Skipping unreadable line: $line")
                }
            }
            Log.d(TAG, "Read ${result.size} feedback contacts.")
            postValue(result)
        } catch (e: Exception) {
            Log.w(TAG, "Could not read feedback contacts", e)
        }
    }

    override fun onActive() {
        if (initialize) {
            initialize = false

            Executors.newSingleThreadExecutor().run {
                submit {
                    loadData()
                }
                shutdown()
            }
        }
    }

}