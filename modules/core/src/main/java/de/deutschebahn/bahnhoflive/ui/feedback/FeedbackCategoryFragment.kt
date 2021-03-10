package de.deutschebahn.bahnhoflive.ui.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.station.*

class FeedbackCategoryFragment : CategorySelectionFragment(
    R.string.title_feedback,
    TrackingManager.TRACK_KEY_FEEDBACK,
    TrackingManager.Screen.D2
) {

    companion object {
        val TAG = FeedbackCategoryFragment::class.java.simpleName
    }

    val viewModel by activityViewModels<StationViewModel>()

    val stationLiveData get() = viewModel.stationResource.data

    private val categories = listOf<Category>(
        SimpleCategory(R.string.rating_button, R.drawable.app_rate, "bewerten") {
            push(RateAppFragment())
        },
        SimpleCategory(R.string.bugreport_button, R.drawable.app_reportbug, "kontakt") {
            push(ReportBugFragment())
        }
    )

    init {
        adapter?.setCategories(categories)
    }

    override fun onStart() {
        super.onStart()

        TrackingManager.fromActivity(activity).track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D2,
            TrackingManager.Entity.FEEDBACK
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.stationWhatsappFeedbackLiveData.observe(viewLifecycleOwner) {
            adapter?.setCategories(
                if (it.isNullOrBlank()) {
                    categories
                } else {
                    categories.toMutableList().apply {
                        add(0,
                            SimpleCategory(
                                R.string.complaint_button,
                                R.drawable.app_complaint,
                                "verschmutzung"
                            ) {
                                push(StationComplaintFragment.create())
                            }
                        )
                    }
                }
            )
        }
    }

    fun openFeedbackMail(context: Context) {
        //        TrackingManager.trackActions(trackingManager, new String[]{TrackingManager.TRACK_KEY_FEEDBACK, "contact"});
        val subject = context.getString(R.string.feedback_subject)
        val recipient = context.getString(R.string.feedback_send_to)

        val text = BaseApplication.get().run {
            "\n\n\n\n" +
                    "Um meine folgenden Anmerkungen leichter nachvollziehen zu können, sende ich Ihnen anbei meine Geräteinformationen:\n\n" +
                    (stationLiveData.value?.let { "Bahnhof: ${it.title} (${it.id})\n" } ?: "") +
                    "Gerät: $deviceName (${android.os.Build.VERSION.SDK_INT})\n" +
                    "App-Version: $versionName ($versionCode)"
        }

        val emailIntent = Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("mailto", recipient, null)
        )
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(emailIntent, "E-Mail senden..."))
    }

}