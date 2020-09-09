package de.deutschebahn.bahnhoflive.ui

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.AvailabilityEntry
import de.deutschebahn.bahnhoflive.ui.station.info.InfoAndServicesLiveData

class AvailabilityRenderer() {

    private val hourMinuteSecondPattern = Regex("(\\d{1,2}:\\d{1,2}):\\d{1,2}")

    private fun String.stripSeconds() =
        hourMinuteSecondPattern.matchEntire(this)?.let { matchResult ->
            matchResult.groupValues[1]
        } ?: this

    fun renderSchedule(availability: List<AvailabilityEntry?>?): String? {
        val stringBuilder = StringBuilder()

        availability?.asSequence()?.filterNotNull()
            ?.forEach { availabilityEntry: AvailabilityEntry ->
                stringBuilder.append(
                    "<br/>${
                        InfoAndServicesLiveData.dayLabels[availabilityEntry.day]
                            ?: availabilityEntry.day
                    }: ${availabilityEntry.openTime?.stripSeconds()}-${availabilityEntry.closeTime?.stripSeconds()}"
                )
            }

        if (stringBuilder.isNotEmpty()) {
            return "<b>Öffnungszeiten</b>$stringBuilder"
        }

        return null
    }

}