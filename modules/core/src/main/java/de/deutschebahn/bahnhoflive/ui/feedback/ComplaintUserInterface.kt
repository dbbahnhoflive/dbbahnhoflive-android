package de.deutschebahn.bahnhoflive.ui.feedback

import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.databinding.IncludeComplaintBinding
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.util.MailUri
import de.deutschebahn.bahnhoflive.util.PhoneIntent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class ComplaintUserInterface(
    private val includeComplaintBinding: IncludeComplaintBinding,
    val stationLiveData: LiveData<Station>,
    val whatsAppInstallationLiveData: WhatsAppInstallation,
    val whatsAppContactliveData: LiveData<String?>,
    val activityStarter: (Intent) -> Unit
) {

    val view get() = includeComplaintBinding.root

    init {

        includeComplaintBinding.feedbackMail.apply {
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, MailUri().apply {
                    to = "feedback@bahnhof.de"
                    subject = stationLiveData.value?.let {
                        "Verschmutzungs-Meldung: ${it.title} ${it.id}"
                    } ?: "Verschmutzungs-Meldung"
                }.build())
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
                try {
                    activityStarter(intent)
                } catch (e: Exception) {
                    Log.w(ComplaintUserInterface::class.java.simpleName, "Could not send mail", e)
                }
            }
        }

        includeComplaintBinding.feedbackPhone.apply {
            val phoneNumber = view.context.getString(R.string.feedback_phone_number)
            setOnClickListener {
                try {
                    activityStarter(
                        PhoneIntent(phoneNumber)
                    )
                } catch (e: Exception) {
                    Log.w(
                        ComplaintUserInterface::class.java.simpleName,
                        "Could not initiate phone call",
                        e
                    )
                }
            }
        }
    }

    private fun openFeedbackWhatsapp(whatsappContact: String) {
        val station = stationLiveData.value ?: return

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "https://wa.me/$whatsappContact?text=${
                    URLEncoder.encode(
                        view.context.getString(
                            R.string.feedback_whatsapp_template,
                            station.title,
                            station.id
                        ),
                        StandardCharsets.UTF_8.name()
                    )
                }"
            )
        )

        activityStarter(intent)
    }

    private fun onIsWhatsAppInstalled(whatsAppInstalled: Boolean) {
        if (whatsAppInstalled) {
            includeComplaintBinding.whatsappContainer.visibility = View.VISIBLE
            includeComplaintBinding.whatsappMissing.visibility = View.GONE
        } else {
            includeComplaintBinding.whatsappContainer.visibility = View.GONE
            includeComplaintBinding.whatsappMissing.visibility = View.VISIBLE
        }
    }

    private fun onWhatsappContact(whatsappContact: String?) {
        if (whatsappContact.isNullOrBlank()) {
            includeComplaintBinding.whatsapp.visibility = View.GONE
        } else {
            includeComplaintBinding.whatsapp.visibility = View.VISIBLE
            includeComplaintBinding.whatsapp.setOnClickListener {
                openFeedbackWhatsapp(whatsappContact)
            }
        }
    }

    fun attach(lifecycleOwner: LifecycleOwner) {
        whatsAppInstallationLiveData.observe(lifecycleOwner, ::onIsWhatsAppInstalled)

        whatsAppContactliveData.observe(lifecycleOwner, ::onWhatsappContact)
    }

    fun detach() {
        whatsAppInstallationLiveData.removeObserver(::onIsWhatsAppInstalled)

        whatsAppContactliveData.removeObserver(::onWhatsappContact)
    }

}