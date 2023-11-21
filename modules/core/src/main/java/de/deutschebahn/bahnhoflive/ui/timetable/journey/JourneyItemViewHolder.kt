package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.findPlatform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.firstLinkedPlatform
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.util.accessibility.AccessibilityUtilities
import de.deutschebahn.bahnhoflive.util.changeAccessibilityActionClickText
import java.text.DateFormat
import java.util.concurrent.TimeUnit

class JourneyItemViewHolder(
    private val itemJourneyDetailedBinding: ItemJourneyDetailedBinding,
    private val onClickPlatformInformation: (view: View, journeyStop: JourneyStop, platforms:List<Platform>) -> Unit) :
    ViewHolder<JourneyStop>(itemJourneyDetailedBinding.root) {

    private val dateFormat = java.text.SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

    constructor(
        parent: ViewGroup,
        inflater: LayoutInflater = LayoutInflater.from(parent.context),
        onClickPlatformInformation: (view: View, journeyStop: JourneyStop, platforms:List<Platform>) -> Unit
    ) : this(
        ItemJourneyDetailedBinding.inflate(
            inflater,
            parent,
            false
        ),
        onClickPlatformInformation
    )

    private val highlightableTextViews = itemJourneyDetailedBinding.run {
        listOf(stopName, scheduledArrival, expectedArrival, scheduledDeparture, expectedDeparture)
    }

    private var platformList : MutableList<Platform> = mutableListOf()

    fun setPlatforms(platformList : List<Platform> ) {
        this.platformList.clear()
        this.platformList.addAll(platformList)
    }

    override fun onBind(item: JourneyStop?) {
        super.onBind(item)

        with(itemJourneyDetailedBinding) {
            stopName.text = item?.name

            platform.text = item?.platform?.let { "Gl. $it" }
            platform.isSelected = item?.isPlatformChange == true

            item?.let {

                val displayPlatform: String = it.platform ?: "" // kann auch 15 D-F sein !
                val thisPlatform: Platform? = platformList.findPlatform(displayPlatform)

                linkPlatform.isVisible =
                    it.current == true && thisPlatform != null && ((platformList.size > 1) || thisPlatform.isHeadPlatform)

                if (linkPlatform.isVisible) {

                    layout.setOnClickListener {
                        platformList.let { it1 ->
                            onClickPlatformInformation(
                                it,
                                item,
                                it1
                            )
                        }
                    }

                    linkPlatform.setOnClickListener {
                        platformList.let { it1 ->
                                    onClickPlatformInformation(
                                        it,
                                        item,
                                it1
                                    )
                                }
                            }

                    /*
                    thisPlatform?.let { itPlatform ->

                        linkPlatform.text = "Gleisinformationen"

                        val levelMask = when  {
                            itPlatform.level < 0 -> "%d. Untergeschoss, Gleis " + itPlatform.formatLinkedPlatformString(
                                true,
                                false
                            )

                            itPlatform.level == 0 -> "Erdgeschoss, Gleis " + itPlatform.formatLinkedPlatformString(
                                true,
                                false
                            )

                            itPlatform.level == LEVEL_UNKNOWN -> "Gleis " + itPlatform.formatLinkedPlatformString(
                                true,
                                false
                            )

                            else -> "%d. Obergeschoss, Gleis " + itPlatform.formatLinkedPlatformString(
                                true,
                                false
                            )
                        }

                        when {
                            itPlatform.level < 0 -> linkPlatform.text =
                                String.format(levelMask, Math.abs(itPlatform.level))

                            itPlatform.level == 0 -> linkPlatform.text = levelMask
                            itPlatform.level == LEVEL_UNKNOWN -> linkPlatform.text =
                                String.format(levelMask)

                            else -> linkPlatform.text =
                                String.format(levelMask, Math.abs(itPlatform.level))

                    }

                        linkPlatform.contentDescription = linkPlatform.text

                }
                    */

                    root.changeAccessibilityActionClickText(itemView.resources.getString(R.string.sr_open_platform_information)) // -> Zum * Doppeltippen
                } else
                    root.changeAccessibilityActionClickText(itemView.resources.getString(R.string.sr_open_station))
            }

            when {

                item?.isAdditional == true -> {
                    advice.setText(R.string.journey_stop_additional)
                    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        advice,
                        0,
                        0,
                        0,
                        0
                    )
                    advice.isSelected = false
                    advice.isGone = false
                }

                item?.isPlatformChange == true -> {
                    advice.setText(R.string.journey_stop_platform_change)
                    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        advice,
                        R.drawable.app_warndreieck,
                        0,
                        0,
                        0
                    )
                    advice.isSelected = true
                    advice.isGone = false
                }

                (item?.departure?.canceled == true || item?.arrival?.canceled == true) -> {
                    advice.setText(R.string.journey_stop_canceled)
                    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        advice,
                        R.drawable.app_warndreieck,
                        0,
                        0,
                        0
                    )
                    advice.isSelected = true
                    advice.isGone = false

                }
                else -> {
                    advice.text = null
                    advice.isGone = true
                }

            }

            bindTimes(scheduledArrival, expectedArrival, item?.arrival)
            bindTimes(scheduledDeparture, expectedDeparture, item?.departure)

            trackStop.isSelected = item?.current == true
            trackStop.isActivated = item?.progress?.let { it >= 0f } == true
            upperTrack.isVisible = item?.first == false
            upperTrackHighlight.isVisible = item?.first == false
            lowerTrack.isVisible = item?.last == false
            lowerTrack.isVisible = item?.last == false

            item?.progress?.let {
                upperTrackHighlight.setImageLevel(
                    (MAX_LEVEL + it * MAX_LEVEL).toInt().coerceIn(0, MAX_LEVEL)
                )
                lowerTrackHighlight.setImageLevel((it * MAX_LEVEL).toInt().coerceIn(0, MAX_LEVEL))
            }


            (if (item?.highlight == true) Typeface.BOLD else Typeface.NORMAL).let { textStyle ->
                highlightableTextViews.forEach { textView ->
                    textView.setTypeface(
                        Typeface.create(textView.typeface, textStyle),
                        textStyle
                    )
                }
            }

            item?.let {
                root.contentDescription = renderContentDescription(it)
            }

        }

    }

    private fun renderContentDescription(journeyStop: JourneyStop): String {

        with(itemView.resources) {

            val platform =
                platformList.firstOrNull { it.number == Platform.platformNumber(journeyStop.platform) }

            return journeyStop.let { itStop ->
                listOfNotNull(
                    listOfNotNull(
                        itStop.name,
                        itStop.platform?.let { "Gleis ${AccessibilityUtilities.convertTrackSpan(it)} " },

                        platform?.let {
                            if (it.isHeadPlatform)
                                " ${getString(R.string.platform_head)}."
                            else
                                null

                        },

                        platform?.let {

                            platformList.firstLinkedPlatform(journeyStop.platform)
                                ?.let { itLinkedPlatform ->
                                        if (it.linkedPlatformNumbers.size == 1) {
                                                " ${
                                                    getString(
                                                        R.string.template_linkplatform,
                                                        itLinkedPlatform.number
                                                    )
                                                }"

                                        } else
                                            null
                                }
                        }


                    ).joinToString(", ", postfix = "."),
                    listOfNotNull(
                        when {
                            itStop.isAdditional -> "(Hinweis: \"Zusätzlicher Halt\")"
                            itStop.isPlatformChange -> "(Hinweis: \"Gleiswechsel\")"
                            else -> null
                        },
                        itStop.arrival?.formatContentDescription("Ankunft", itStop.progress >= 0),
                        itStop.departure?.formatContentDescription("Abfahrt", itStop.progress > 0)
                    ).joinToString("; ", postfix = ".")
                ).joinToString(separator = " ")
            }.also {
//                Log.d(JourneyItemViewHolder::class.java.simpleName, "Content description:\n$it")
            }

        }

    }

    private fun JourneyStopEvent.formatContentDescription(prefix: String, past: Boolean) =
        listOfNotNull(
            prefix,
            parsedScheduledTime?.run {
                "${ AccessibilityUtilities.getSpokenTime(formatTime())}"
            },
            parsedEstimatedTime?.takeUnless { it == parsedScheduledTime }?.run {
                "(heute ${if (!past) "voraussichtlich " else ""}${AccessibilityUtilities.getSpokenTime(formatTime())})"
            }
        ).joinToString(" ")

    private fun Long.formatTime() = dateFormat.format(this)

    private fun bindTimes(
        scheduledTimeView: TextView,
        estimatedTimeView: TextView,
        journeyStopEvent: JourneyStopEvent?
    ) {
        val parsedScheduledTime = journeyStopEvent?.parsedScheduledTime

        scheduledTimeView.text =
            parsedScheduledTime?.let { dateFormat.format(it) }
        estimatedTimeView.text =
            journeyStopEvent?.parsedEstimatedTime?.let { dateFormat.format(it) }
        estimatedTimeView.setTextColor(
            estimatedTimeView.context.resources.getColor(
                journeyStopEvent?.let { journeyStopEvent ->
                    journeyStopEvent.parsedScheduledTime?.let {
                        journeyStopEvent.parsedEstimatedTime?.minus(
                            it
                        )?.takeIf { it > TimeUnit.MINUTES.toMillis(5) }?.let {
                            Status.NEGATIVE.color
                        }
                    }
                } ?: Status.POSITIVE.color)
        )

        val viewsGone = parsedScheduledTime == null
        scheduledTimeView.isGone = viewsGone
        estimatedTimeView.isGone = viewsGone
    }

    companion object {
        const val MAX_LEVEL = 10000
    }

    }
