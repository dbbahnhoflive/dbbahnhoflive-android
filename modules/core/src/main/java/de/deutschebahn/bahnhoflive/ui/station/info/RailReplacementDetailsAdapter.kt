package de.deutschebahn.bahnhoflive.ui.station.info

import android.content.Intent
import android.os.Build
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.databinding.CardExpandableStationInfoBinding
import de.deutschebahn.bahnhoflive.repository.Station
import de.deutschebahn.bahnhoflive.ui.feedback.createPlaystoreIntent
import de.deutschebahn.bahnhoflive.ui.feedback.deviceName
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.ui.station.StationActivity
import de.deutschebahn.bahnhoflive.util.MailUri
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.view.inflater
import java.util.Locale

class RailReplacementDetailsAdapter(
    private val serviceContents: List<ServiceContent>,
    val trackingManager: TrackingManager,
    private val dbActionButtonParser: DbActionButtonParser,
    private val stationLiveData: LiveData<out Station>,
    val activityStarter: (Intent) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<CommonDetailsCardViewHolder<ServiceContent>>() {
    val singleSelectionManager: SingleSelectionManager = SingleSelectionManager(this)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommonDetailsCardViewHolder<ServiceContent> = when (viewType) {
        VIEW_TYPE_STOP_PLACE_INFORMATION -> ServiceContentViewHolder(
            CardExpandableStationInfoBinding.inflate(parent.inflater, parent, false),
            singleSelectionManager,
            trackingManager,
            dbActionButtonParser
        ) {}
        else -> ServiceContentViewHolder(  // VIEW_TYPE_COMPANION
            CardExpandableStationInfoBinding.inflate(parent.inflater, parent, false),
            singleSelectionManager,
            trackingManager,
            dbActionButtonParser
        ) { dbActionButton: DbActionButton ->
            if (dbActionButton.type == DbActionButton.Type.ACTION) {
                when (dbActionButton.data) {
                    "appIssue" -> {
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO,
                            MailUri().apply {
                                to = parent.context.getString(R.string.feedback_send_to)
                                this.subject = parent.context.getString(R.string.feedback_subject)
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
                        activityStarter(Intent.createChooser(emailIntent, "E-Mail senden..."))

                    }
                    "chatbot" -> {

                    }
                    "rateApp" -> {
                        activityStarter(parent.context.createPlaystoreIntent())
                    }
                    "mobilitaetsservice" -> run {
                        (parent.context as? StationActivity)?.showMobilityServiceNumbers()
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: CommonDetailsCardViewHolder<ServiceContent>,
        position: Int
    ) {
        holder.bind(serviceContents[position])
    }

    override fun getItemCount(): Int {
        return serviceContents.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (serviceContents[position].type.lowercase(Locale.GERMAN)) {
            ServiceContentType.Local.STOP_PLACE -> VIEW_TYPE_STOP_PLACE_INFORMATION
            else -> VIEW_TYPE_COMPANION
        }
    }

    val selectedItem get() = singleSelectionManager.getSelectedItem(serviceContents)

    companion object {
        const val VIEW_TYPE_STOP_PLACE_INFORMATION = 0
        const val VIEW_TYPE_COMPANION = 1
    }
}