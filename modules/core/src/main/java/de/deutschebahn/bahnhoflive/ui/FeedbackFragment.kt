package de.deutschebahn.bahnhoflive.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.DummyObserver

class FeedbackFragment : Fragment() {

    val actionBarTitle: String
        get() = resources.getString(R.string.menu_feedback)

    val isShowingActionBar: Boolean
        get() = true

    lateinit var station: LiveData<Station>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val stationViewModel = ViewModelProviders.of(activity!!).get(StationViewModel::class.java)
        station = stationViewModel.stationResource.data
        station.observe(this, DummyObserver())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_feedback, container, false)

        ToolbarViewHolder(view, R.string.menu_feedback)

        return view
    }

    override fun onStart() {
        super.onStart()

        val trackingManager = TrackingManager.fromActivity(activity)
        trackingManager.track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D2, TrackingManager.Entity.FEEDBACK)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.send_feedback_button).setOnClickListener { openFeedbackMail(view.context) }

        view.findViewById<View>(R.id.rate_app_button).setOnClickListener { openAppInPlayStore(view.context) }
    }


    fun openFeedbackMail(context: Context) {
        //        TrackingManager.trackActions(trackingManager, new String[]{TrackingManager.TRACK_KEY_FEEDBACK, "contact"});
        val subject = context.getString(R.string.feedback_subject)
        val recipient = context.getString(R.string.feedback_send_to)
        val text = "\n\n\n\n" +
                "Um meine folgenden Anmerkungen leichter nachvollziehen zu können, sende ich Ihnen anbei meine Geräteinformationen:\n\n" +
                (station.value?.let { "Bahnhof: ${it.title} (${it.id})\n" } ?: "")  +
                "Gerät: ${FeedbackFragment.deviceName} (${android.os.Build.VERSION.SDK_INT})\n" +
                "App-Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        val emailIntent = Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", recipient, null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(emailIntent, "E-Mail senden..."))
    }

    companion object {

        fun openAppInPlayStore(context: Context) {
            //        TrackingManager.trackActions(trackingManager, new String[]{TrackingManager.TRACK_KEY_FEEDBACK, "rating"});
            val link = "market://details?id=" + context.packageName.replace(".debug", "")
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
        }

        fun registerBanner(target: ViewGroup, onClick: View.OnClickListener) {
            val inflater = LayoutInflater.from(target.context)
            val view = inflater.inflate(R.layout.item_feedback_banner, target, false)
            val close = view.findViewById<View>(R.id.close_button)

            target.addView(view)
            close.setOnClickListener { target.removeView(view) }
            view.setOnClickListener { v ->
                target.removeView(view)
                onClick.onClick(v)
            }
        }

        /** Returns the consumer friendly device name  */
        val deviceName: String?
            get() {
                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                return if (model.startsWith(manufacturer)) {
                    capitalize(model)
                } else capitalize(manufacturer) + " " + model
            }

        private fun capitalize(str: String): String? {
            if (TextUtils.isEmpty(str)) {
                return str
            }
            val arr = str.toCharArray()
            var capitalizeNext = true

            //        String phrase = "";
            val phrase = StringBuilder()
            for (c in arr) {
                if (capitalizeNext && Character.isLetter(c)) {
                    //                phrase += Character.toUpperCase(c);
                    phrase.append(Character.toUpperCase(c))
                    capitalizeNext = false
                    continue
                } else if (Character.isWhitespace(c)) {
                    capitalizeNext = true
                }
                //            phrase += c;
                phrase.append(c)
            }

            return phrase.toString()
        }

        fun create(): FeedbackFragment {
            return FeedbackFragment()
        }
    }
}
