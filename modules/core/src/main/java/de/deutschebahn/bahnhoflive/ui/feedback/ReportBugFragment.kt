package de.deutschebahn.bahnhoflive.ui.feedback

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.util.MailUri
import kotlinx.android.synthetic.main.fragment_report_bug.view.*

class ReportBugFragment : FeedbackFragment(
    R.layout.fragment_report_bug,
    R.string.bugreport_button,
    TrackingManager.Entity.REPORT_BUG
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.send_feedback_button.setOnClickListener {
            openFeedbackMail(it.context)
        }
    }

    fun openFeedbackMail(context: Context) {
        //        TrackingManager.trackActions(trackingManager, new String[]{TrackingManager.TRACK_KEY_FEEDBACK, "contact"});

        val emailIntent = Intent(
            Intent.ACTION_SENDTO,
            MailUri().apply {
                to = context.getString(R.string.feedback_send_to)
                this.subject = context.getString(R.string.feedback_subject)
                body = BaseApplication.get().run {
                    "\n\n\n\n" +
                            "Um meine folgenden Anmerkungen leichter nachvollziehen zu können, sende ich Ihnen anbei meine Geräteinformationen:\n\n" +
                            (stationLiveData.value?.let<Station, String> { "Bahnhof: ${it.title} (${it.id})\n" }
                                ?: "") +
                            "Gerät: $deviceName (${Build.VERSION.SDK_INT})\n" +
                            "App-Version: $versionName ($versionCode)"
                }
            }.build()
        )
        startActivity(Intent.createChooser(emailIntent, "E-Mail senden..."))
    }

}