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
import de.deutschebahn.bahnhoflive.util.startSafely
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager
import de.deutschebahn.bahnhoflive.view.inflater


class RailReplacementStopInfoViewHolder(
    private val binding : CardExpandableRailReplacementStopInfoBinding,
    selectionManager: SingleSelectionManager,
) : CommonDetailsCardViewHolder<ServiceContent>(
    binding.root,
    selectionManager
) {

    var railReplacementText : String = ""

    override fun onBind(item: ServiceContent?) {
        super.onBind(item)

        item?.let {
            titleView.text = it.title
            iconView.setImageResource(IconMapper.contentIconForType(it))
        }

        // bahnhof.de öffnen
        binding.linkReplacementTraffic.visibility = View.VISIBLE
        binding.linkReplacementTraffic.setOnClickListener {
            itemView.context?.let { it1 ->
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://bahnhof.de/bfl/ev-nw")
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

            railReplacementNev.apply {
                if (visibility == View.VISIBLE) {
                    contentDescription = text.toString().replace("26. Mai", "26.5.2023")
                        .replace("11. September 2023", "11.9.2023")
                        .replace("06. August 2023", "6.8.2023")
                        .replace("06. August", "6.8.2023")
                        .replace("05. August 2023", "5.8.2023")
                        .replace("05. August", "5.8.2023")
                }
            }

            railReplacementNev2.apply {
                if (visibility == View.VISIBLE) {
                    contentDescription = text.toString().replace("26. Mai", "26.5.2023")
                        .replace("11. September 2023", "11.9.2023")
                        .replace("05. August 2023", "5.8.2023")
                        .replace("05. August", "5.8.2023")
                        .replace("06. August 2023", "6.8.2023")
                        .replace("06. August", "6.8.2023")
                }
            }


//            titleBar.staticTitleBar.screenTitle.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }


    }

    fun setNevContent(nevData: List<News>) {
        binding.apply {
            nevInfoTop.visibility = View.VISIBLE
            icon.visibility = View.VISIBLE
            newsHeadline.visibility = View.VISIBLE
            newsCopy.visibility = View.VISIBLE

            railReplacementNev.visibility = View.VISIBLE
//            railReplacementNev2.visibility = View.VISIBLE

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

        setScreenReaderText()
    }

}