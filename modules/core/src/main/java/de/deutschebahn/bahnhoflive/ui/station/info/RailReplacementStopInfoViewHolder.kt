package de.deutschebahn.bahnhoflive.ui.station.info

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.isVisible
import de.deutschebahn.bahnhoflive.IconMapper
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.databinding.CardExpandableRailReplacementStopInfoBinding
import de.deutschebahn.bahnhoflive.databinding.IncludeItemRailReplacementBinding
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static_Riedbahn
import de.deutschebahn.bahnhoflive.util.startSafely
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.view.inflater


class RailReplacementStopInfoViewHolder(
    private val binding : CardExpandableRailReplacementStopInfoBinding,
    selectionManager: SingleSelectionManager,
    private val stationViewModel: StationViewModel
) : CommonDetailsCardViewHolder<ServiceContent>(
    binding.root,
    selectionManager
) {

    val isRiedbahnReplacement : Boolean =  SEV_Static_Riedbahn.isStationReplacementStopByStationID(stationViewModel.station?.id) &&
            SEV_Static_Riedbahn.isInConstructionPhase() || SEV_Static_Riedbahn.isInAnnouncementPhase()

    var railReplacementText : String = ""

    override fun onBind(item: ServiceContent?) {
        super.onBind(item)

        item?.let {
            titleView.text = it.title
            iconView.setImageResource(IconMapper.contentIconForType(it))
        }

        binding.header.status.isVisible=false


        binding.moreInfoLink.linkText.text =
            itemView.context.getString(R.string.sev_stop_info_more_information_url_link_text)
        binding.moreInfoLink.linkText.contentDescription =  itemView.context.getString(R.string.sev_stop_info_more_information_url_link_text)
        binding.moreInfoLink.layout.setOnClickListener {
            itemView.context?.let { it1 ->
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse( itemView.context.getString(R.string.sev_stop_info_more_information_url))
                ).startSafely(it1)
            }
        }

    }

    private fun setScreenReaderText()  {

        binding.apply {
            var fullText = "" //(titleBar.staticTitleBar.screenTitle.text?:"") as String

            if (railReplacementNev.visibility == View.VISIBLE)
                fullText += railReplacementNev.text ?: ""

            if (railReplacementEntryLabel.visibility == View.VISIBLE)
                fullText += railReplacementEntryLabel.text ?: ""

            fullText += railReplacementText

            if (railReplacementNev2.visibility == View.VISIBLE)
                fullText += railReplacementNev2.text ?: ""

//            titleBar.staticTitleBar.screenTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }


        }


    // kommt nur, wenn es ein SEV-Stop ist
    fun setStaticSEVContent(nevData: List<News>) {
        binding.apply {
            nevInfoTop.isVisible = true
            icon.isVisible = true
            newsHeadline.isVisible = isRiedbahnReplacement
            newsCopy.isVisible = false

            railReplacementNev.isVisible = isRiedbahnReplacement
            railReplacementNev2.isVisible = false

            binding.riedbahnInfo.isVisible = isRiedbahnReplacement
            binding.moreInfoLink.layout.isVisible = isRiedbahnReplacement
            setScreenReaderText()
        }
    }
    fun setStopPlaceContent(railReplacements: MutableMap<String, MutableList<String?>>) {

        binding.contentList.removeAllViews()

        binding.railReplacementEntryLabel.setText(
            if (!railReplacements.run {
                    entries.fold(0) { count, mutableEntry ->
                        count + mutableEntry.value.size
                    } == 1
                }) R.string.rail_replacement_entry_label_plural else R.string.rail_replacement_entry_label_singular
        )


        railReplacementText=""

        railReplacements.forEach { (directions, texts) ->
            IncludeItemRailReplacementBinding.inflate(
                binding.root.inflater,
                binding.contentList,
                true
            ).apply {
                railReplacementDirections.text = directions

                if (railReplacements.size == 1 && texts.mapNotNull { itText -> itText?.isNotEmpty() }
                        .isEmpty()) {
                    railReplacementTexts.isVisible = false
                    binding.railReplacementEntryLabel.isVisible = false
                } else {
                    railReplacementTexts.text =
                        texts.mapNotNull {
                            "• " + (it.takeUnless { it.isNullOrBlank() }
                                ?: itemView.context.getString(R.string.rail_replacement_additional))
                        }.joinToString("\n")
                    binding.railReplacementEntryLabel.isVisible = true
                }

//                binding.railReplacementEntryLabel.text = "An diesem Bahnhof finden Sie folgende Ersatzhaltestelle(n):"
//                binding.railReplacementEntryLabel.isVisible = true

                railReplacementText += railReplacementTexts.text
                railReplacementText += itemView.context.getString(R.string.rail_replacement_directions)
                railReplacementText += directions


            }

        }

        binding.contentList.isVisible = railReplacementText.isNotEmpty()
        setScreenReaderText()
    }

}