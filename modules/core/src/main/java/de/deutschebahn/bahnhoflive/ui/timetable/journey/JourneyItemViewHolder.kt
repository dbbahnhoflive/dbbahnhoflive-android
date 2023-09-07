package de.deutschebahn.bahnhoflive.ui.timetable.journey

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.ris.model.Platform
import de.deutschebahn.bahnhoflive.backend.db.ris.model.findLinkedPlatform
import de.deutschebahn.bahnhoflive.databinding.ItemJourneyDetailedBinding
import de.deutschebahn.bahnhoflive.ui.Status
import de.deutschebahn.bahnhoflive.ui.ViewHolder
import java.text.DateFormat
import java.util.concurrent.TimeUnit

class JourneyItemViewHolder(val itemJourneyDetailedBinding: ItemJourneyDetailedBinding) :
    ViewHolder<JourneyStop>(itemJourneyDetailedBinding.root) {

    private val dateFormat = java.text.SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

    constructor(
        parent: ViewGroup,
        inflater: LayoutInflater = LayoutInflater.from(parent.context)
    ) : this(
        ItemJourneyDetailedBinding.inflate(
            inflater,
            parent,
            false
        )
    )

    val highlightableTextViews = itemJourneyDetailedBinding.run {
        listOf(stopName, scheduledArrival, expectedArrival, scheduledDeparture, expectedDeparture)
    }

    private var platforms : List<Platform>? = null

    fun setPlatforms(platforms : List<Platform> ) {
        this.platforms = platforms
    }

    override fun onBind(item: JourneyStop?) {
        super.onBind(item)

        with(itemJourneyDetailedBinding) {
            stopName.text = item?.name

            platform.text = item?.platform?.let { "Gl. $it" }
            platform.isSelected = item?.isPlatformChange == true
            platform.isVisible = item?.current!=true

            item?.let {
                platformButton.layout.isVisible = it.current==true

                if(it.current) {

                    // platforms
                    val platformList : MutableList<String> = mutableListOf() // gleis + gegenueberliegendes gleis

                    val displayPlatform: String = it.platform?:"" // kann auch 15 D-F sein !
                    val linkedPlatformAsInt: Int = platforms?.findLinkedPlatform(displayPlatform) ?: 0
                    val displayPlatformAsInt: Int = Platform.platformNumber(displayPlatform, 0)

                    var rightPlatformAsInt : Int = 0

                    platformList.add(displayPlatform)
                    if(linkedPlatformAsInt!=0) {
                        if(linkedPlatformAsInt<displayPlatformAsInt)
                            platformList.add(0, linkedPlatformAsInt.toString())
                        else
                            platformList.add(linkedPlatformAsInt.toString())
                        rightPlatformAsInt = Platform.platformNumber(platformList[1])
                    }
                    val leftPlatformAsInt = Platform.platformNumber(platformList[0])

                    platformButton.apply {
                        platformLeft.run {
                            text = platformList[0]
                        }

                        if (linkedPlatformAsInt != 0) {
                            linkedplatform.run {
                                text = platformList[1]
                                isVisible = true
                            }
                        } else {
                            linkedplatform?.run {
                                isVisible = false
                            }
                        }

                        platformsplitter.run {
                            isVisible = linkedPlatformAsInt != 0
                        }

                        platformindicatorLeft.run {
                            isVisible =
                                linkedPlatformAsInt != 0 && displayPlatformAsInt == leftPlatformAsInt
                        }

                        platformindicatorRight.run {
                            isVisible =
                                linkedPlatformAsInt != 0 && displayPlatformAsInt == rightPlatformAsInt
                        }


                    }
                }



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

            root.contentDescription = item?.run {
                listOfNotNull(
                    listOfNotNull(
                        name,
                        platform?.let { "Gleis $it " }
                    ).joinToString(", ", postfix = "."),
                    listOfNotNull(
                        when {
                            isAdditional -> "(Hinweis: \"Zusätzlicher Halt\")"
                            isPlatformChange -> "(Hinweis: \"Gleiswechsel\")"
                            else -> null
                        },
                        arrival?.formatContentDescription("Ankunft", progress >= 0),
                        departure?.formatContentDescription("Abfahrt", progress > 0)
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
                "${formatTime()} Uhr"
            },
            parsedEstimatedTime?.takeUnless { it == parsedScheduledTime }?.run {
                "(heute ${if (!past) "voraussichtlich " else ""}${formatTime()} Uhr)"
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
