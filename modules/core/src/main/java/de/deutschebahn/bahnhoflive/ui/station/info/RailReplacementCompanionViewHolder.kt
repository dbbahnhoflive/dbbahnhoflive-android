package de.deutschebahn.bahnhoflive.ui.station.info

import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.IconMapper
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.databinding.CardExpandableRailReplacementCompanionBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static_Riedbahn
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager


class RailReplacementCompanionViewHolder(
    private val binding : CardExpandableRailReplacementCompanionBinding,
    private val selectionManager: SingleSelectionManager,
    private val activityStarter: (intent:CustomTabsIntent, url:String) -> Unit,
    private val companionHintStarter : () -> Unit
) : CommonDetailsCardViewHolder<ServiceContent>(
    binding.root,
    selectionManager
) {


    override fun onBind(item: ServiceContent?) {
        super.onBind(item)

        item?.let {
            titleView.text = it.title
            iconView.setImageResource(IconMapper.contentIconForType(it))
        }


        if (SEV_Static_Riedbahn.isInAnnouncementPhase()) {

            binding.serviceAnnouncement.isVisible = true

            binding.servicetimes.isVisible = false
            binding.textServicetime.isVisible = false
            binding.linkVideoCall.isVisible = false
        } else
            if (SEV_Static_Riedbahn.isInConstructionPhase()) {
                binding.serviceAnnouncement.isVisible=false
                binding.servicetimes.isVisible=true
                binding.textServicetime.isVisible=true
                binding.linkVideoCall.isVisible=true
            }




        binding.linkVideoCall.setOnClickListener {

            val url =
                itemView.context.getString(R.string.rail_replacement_db_companion_video_call_url)
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .build()
            activityStarter(intent, url)

        }


        binding.layoutHint.let {
            it.linkButton.setImageResource(R.drawable.app_links_pfeil)
            it.linkText.text =
                itemView.context.getString(R.string.rail_replacement_db_companion_hint)
            it.layout.contentDescription = it.linkText.text
            it.layout.setOnClickListener{
                companionHintStarter()
            }
        }

        binding.layoutImprint.let {
            it.linkText.text =
                itemView.context.getString(R.string.rail_replacement_db_companion_imprint)
            it.layout.contentDescription = it.linkText.text
            it.layout.setOnClickListener {
                val url =
                    itemView.context.getString(R.string.rail_replacement_db_companion_imprint_url)
                val intent = CustomTabsIntent.Builder()
                    .build()
                activityStarter(intent, url)
            }
        }

        binding.layoutLegalPolicy.let {
            it.linkText.text =
                itemView.context.getString(R.string.rail_replacement_db_companion_legal_notice)
            it.layout.contentDescription = it.linkText.text

            it.layout.setOnClickListener {
                val url =
                    itemView.context.getString(R.string.rail_replacement_db_companion_legal_notice_url)
                val intent = CustomTabsIntent.Builder()
                    .build()
                activityStarter(intent, url)
            }
        }


    }

    fun setDbCompanionServiceState(isAvailable : Boolean ) {
        binding.linkVideoCall.isEnabled = isAvailable

        @StringRes
        val statusText = if(isAvailable) R.string.rail_replacement_db_companion_service_state_available else R.string.rail_replacement_db_companion_service_state_not_available
        val status : Status = if(isAvailable) Status.POSITIVE else Status.NEGATIVE

        this.setStatus(status, statusText)
    }
    private fun setScreenReaderText()  {

        binding.apply {
        }


    }


}