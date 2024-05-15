package de.deutschebahn.bahnhoflive.ui.station.info

import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import de.deutschebahn.bahnhoflive.IconMapper
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.databinding.CardExpandableRailReplacementCompanionBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
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



        binding.linkVideoCall.setOnClickListener {

            val url = itemView.context.getString(R.string.rail_replacement_db_companion_video_call_url)
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .build()
            activityStarter(intent, url)

        }


        binding.layoutHint.let {
            it.linkButton.setImageResource(R.drawable.app_links_pfeil)
            it.linkText.text = itemView.context.getString(R.string.rail_replacement_db_companion_hint)
            it.layout.setOnClickListener{

                selectionManager.clearSelection()
                companionHintStarter()
            }
        }

        binding.layoutImprint.let {
            it.linkText.text = itemView.context.getString(R.string.rail_replacement_db_companion_imprint)
            it.layout.setOnClickListener {
                val url = itemView.context.getString(R.string.rail_replacement_db_companion_imprint_url)
                val intent = CustomTabsIntent.Builder()
                    .build()
                activityStarter(intent, url)
            }
        }

        binding.layoutLegalPolicy.let {
            it.linkText.text = itemView.context.getString(R.string.rail_replacement_db_companion_legal_notice)
            it.layout.setOnClickListener {
                val url = itemView.context.getString(R.string.rail_replacement_db_companion_legal_notice_url)
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
//        statusView?.text = if(isAvailable) itemView.context.getString(R.string.rail_replacement_db_companion_service_state_available) else
//            itemView.context.getString(R.string.rail_replacement_db_companion_service_state_not_available)

    }
    private fun setScreenReaderText()  {

        binding.apply {
        }


    }


}