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
import de.deutschebahn.bahnhoflive.backend.db.ris.model.firstLinkedPlatform
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import de.deutschebahn.bahnhoflive.util.accessibility.AccessibilityUtilities
import de.deutschebahn.bahnhoflive.util.changeAccessibilityActionClickText
import de.deutschebahn.bahnhoflive.util.formatShortTime
import de.deutschebahn.bahnhoflive.util.getColorById
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

        if (item == null)
            itemJourneyDetailedBinding.root.visibility = View.INVISIBLE
        else
        with(itemJourneyDetailedBinding) {

                item.let { itJourneyStop ->
                    run {

                        stopName.text = itJourneyStop.name

                        platform.text = itJourneyStop.platform?.let { "Gl. $it" }
                        platform.isSelected = itJourneyStop.isPlatformChange == true

                        linkPlatform.isVisible =
                            itJourneyStop.current == true && itJourneyStop.platform != null
                        // todo: wenn Gleisinformationen eingebaut werden sollen, diese Zeile einbauen raus und
                                                                                 //      linkPlatform.isVisible = false löschen
                linkPlatform.isVisible = false

                if (linkPlatform.isVisible) { // > Gleisinfomationen

                    layout.setOnClickListener {
                        platformList.let { it1 ->
                            onClickPlatformInformation(
                                it,
                                        itJourneyStop,
                                it1
                            )
                        }
                    }

                    linkPlatform.setOnClickListener {
                        platformList.let { it1 ->
                                    onClickPlatformInformation(
                                        it,
                                        itJourneyStop,
                                it1
                                    )
                                }
                            }

                    root.changeAccessibilityActionClickText(itemView.resources.getString(R.string.sr_open_platform_information)) // -> Zum * Doppeltippen
                } else
                    root.changeAccessibilityActionClickText(itemView.resources.getString(R.string.sr_open_station))


                        var additionTextResId: Int = 0
                        var additionalSymbolResInt: Int = 0

                        when {
                            itJourneyStop.isAdditional -> {
                                additionTextResId = R.string.journey_stop_additional
                                advice.setText(R.string.journey_stop_additional)
            }

                            itJourneyStop.isPlatformChange -> {
                                additionTextResId = R.string.journey_stop_platform_change
                                additionalSymbolResInt = R.drawable.app_warndreieck
                            }

                            (itJourneyStop.departure?.canceled == true || itJourneyStop.arrival?.canceled == true) -> {
                                additionTextResId = R.string.journey_stop_canceled
                                additionalSymbolResInt = R.drawable.app_warndreieck
                            }
                }


                    advice.isGone = false

                        if (additionTextResId != 0) {
                            advice.setText(additionTextResId)
                            advice.isSelected = additionalSymbolResInt != 0
                }

                    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        advice,
                            additionalSymbolResInt,
                        0,
                        0,
                        0
                    )

                        val hasArrivalTime: Boolean = itJourneyStop.let {
                            it.arrival?.let { it.parsedScheduledTime != null } ?: false
                }
                        val hasDepartureTime = itJourneyStop.let {
                            it.departure?.let { it.parsedScheduledTime != null } ?: false
                        }
                        val hasAdvice = additionTextResId != 0

                        // layout is so designt, das elemente sich automatisch vertikal zentrieren
                        // ab 16.4.2024 nicht mehr gewünscht -> Anpassung an IOS design

                        // Normalfall: ankunft+abfahrt vorhanden, mit od. ohne advice -> 2 Zeilen
                        // Sonderfall 1 : keine ankunft, kein advice  -> advice GONE, arrival GONE
                        // Sonderfall 2 : keine abfahrt, kein advice  -> advice GONE, departure GONE
                        // Sonderfall 3 : keine ankunft, advice  -> departure INVISIBLE, departure an arrival-position
                        // Sonderfall 4 : keine abfahrt, advice  -> departure INVISIBLE

                        var arrivalViewMode = View.VISIBLE
                        var departureViewMode = View.VISIBLE

                        if (!hasArrivalTime && !hasAdvice) {
                            advice.isGone = true
                            arrivalViewMode = View.GONE
                        } else
                            if (!hasDepartureTime && !hasAdvice) {
                    advice.isGone = true
                                departureViewMode = View.GONE
                            } else
                                if (!hasArrivalTime && hasAdvice) {
                                    departureViewMode = View.INVISIBLE
                                } else
                                    if (!hasDepartureTime && hasAdvice) {
                                        departureViewMode = View.INVISIBLE

                }


                        if (!hasArrivalTime && hasAdvice)
                            bindTimes(
                                scheduledArrival,
                                expectedArrival,
                                itJourneyStop.departure,
                                arrivalViewMode
                            )
                        else
                            bindTimes(
                                scheduledArrival,
                                expectedArrival,
                                itJourneyStop.arrival,
                                arrivalViewMode
                            )

                        bindTimes(
                            scheduledDeparture,
                            expectedDeparture,
                            itJourneyStop.departure,
                            departureViewMode
                        )

                        trackStop.isSelected = itJourneyStop.current == true
                        trackStop.isActivated = itJourneyStop.progress.let { it >= 0f } == true
                        upperTrack.isVisible = itJourneyStop.first == false
                        upperTrackHighlight.isVisible = itJourneyStop.first == false
                        lowerTrack.isVisible = itJourneyStop.last == false
                        lowerTrack.isVisible = itJourneyStop.last == false

                        itJourneyStop.progress.let {
                upperTrackHighlight.setImageLevel(
                    (MAX_LEVEL + it * MAX_LEVEL).toInt().coerceIn(0, MAX_LEVEL)
                )
                            lowerTrackHighlight.setImageLevel(
                                (it * MAX_LEVEL).toInt().coerceIn(0, MAX_LEVEL)
                            )
            }

                        (if (itJourneyStop.highlight) Typeface.BOLD else Typeface.NORMAL).let { textStyle ->
                highlightableTextViews.forEach { textView ->
                    textView.setTypeface(
                        Typeface.create(textView.typeface, textStyle),
                        textStyle
                    )
                }
            }

                        itJourneyStop.let { itJourneyStop ->
                            root.contentDescription = renderContentDescription(itJourneyStop)
                        }

                    }

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
                                    listOfNotNull(
                                        if (it.countLinkedPlatforms == 1) {
                                            listOfNotNull(
                                                " .${
                                                    getString(
                                                        R.string.template_linkplatform,
                                                        itLinkedPlatform.number
                                                    )
                                                }"

                                            )

                                        } else
                                            null
                                    )
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
            }
//                .also {
//                Log.d(JourneyItemViewHolder::class.java.simpleName, "Content description:\n$it")
//            }

        }

    }

    private fun JourneyStopEvent.formatContentDescription(prefix: String, past: Boolean) =
        listOfNotNull(
            prefix,
            parsedScheduledTime?.run {
                AccessibilityUtilities.getSpokenTime(formatTime())
            },
            parsedEstimatedTime?.takeUnless { it == parsedScheduledTime }?.run {
                "(heute ${if (!past) "voraussichtlich " else ""}${AccessibilityUtilities.getSpokenTime(formatTime())})"
            }
        ).joinToString(" ")

    private fun Long.formatTime() = dateFormat.format(this)

    private fun bindTimes(
        scheduledTimeView: TextView,
        estimatedTimeView: TextView,
        journeyStopEvent: JourneyStopEvent?,
        viewMode:Int
    ) {
        val parsedScheduledTime = journeyStopEvent?.parsedScheduledTime

        scheduledTimeView.text =
            parsedScheduledTime?.formatShortTime()
        estimatedTimeView.text =
            journeyStopEvent?.parsedEstimatedTime?.formatShortTime()
        estimatedTimeView.setTextColor(
            estimatedTimeView.context.getColorById(
                journeyStopEvent?.let { itJourneyStopEvent ->
                    itJourneyStopEvent.parsedScheduledTime?.let {
                        itJourneyStopEvent.parsedEstimatedTime?.minus(it)
                            ?.takeIf { it > TimeUnit.MINUTES.toMillis(5) }?.let {
                            Status.NEGATIVE.color
                        }
                    }
                } ?: Status.POSITIVE.color)
        )

        val viewsGone = parsedScheduledTime == null
//        scheduledTimeView.isGone = !show//viewsGone
//        estimatedTimeView.isGone = !show//viewsGone
//        scheduledTimeView.visibility = if(show) View.VISIBLE else View.INVISIBLE
//        estimatedTimeView.visibility = if(show) View.VISIBLE else View.INVISIBLE

        scheduledTimeView.visibility = viewMode
        estimatedTimeView.visibility = viewMode

    }

    companion object {
        const val MAX_LEVEL = 10000
    }

    }
