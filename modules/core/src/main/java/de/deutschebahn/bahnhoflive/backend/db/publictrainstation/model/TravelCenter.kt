package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

class TravelCenter {

    var identifier = -1

    var name: String? = null

    var address: String? = null

    var postCode: String? = null

    var city: String? = null


    lateinit var type: String

    var lat = -1.0
    var lon = -1.0

    var openingTimes: OpeningTimes? = null

    var distanceInKm = Float.MAX_VALUE

    val composedAddress by lazy {
        sequenceOf(name, address, sequenceOf(postCode?.nonBlank, city?.nonBlank).joinNonNull(" "))
                .joinNonNull("<br/>\n")
    }

    val String.nonBlank get() = takeUnless { it.isBlank() }

    fun Sequence<String?>.joinNonNull(separator: CharSequence) =
            filterNotNull()
                    .joinToString(separator = separator)
                    .nonBlank
}