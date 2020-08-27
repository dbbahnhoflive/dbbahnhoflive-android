package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

import de.deutschebahn.bahnhoflive.util.append

class EmbeddedTravelCenter {

    var identifier = -1

    var name: String? = null

    var address: Address? = null

    lateinit var type: String

    var location: Location? = null

    var openingHours: List<AvailabilityEntry>? = null

    var distanceToStopPlace: Int? = Int.MAX_VALUE

    val composedAddress by lazy {
        sequenceOf(
            name,
        ).run {
            address?.run {
                append(
                    sequenceOf(
                        street,
                        sequenceOf(postalCode?.nonBlank, city?.nonBlank).joinNonNull(" ")
                    )
                )
            } ?: this
        }.joinNonNull("<br/>\n")
    }

    val String.nonBlank get() = takeUnless { it.isBlank() }

    fun Sequence<String?>.joinNonNull(separator: CharSequence) =
        filterNotNull()
            .joinToString(separator = separator)
            .nonBlank
}