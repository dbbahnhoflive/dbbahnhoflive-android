package de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model

class OpeningTimes {

    lateinit var links: List<Link?>

    var mon: List<String>? = null
    var tue: List<String>? = null
    var wed: List<String>? = null
    var thu: List<String>? = null
    var fri: List<String>? = null
    var sat: List<String>? = null
    var sun: List<String>? = null

    val rendered: String? by lazy {
        sequenceOf("Montag" to mon,
                "Dienstag" to tue,
                "Mittowch" to wed,
                "Donnerstag" to thu,
                "Freitag" to fri,
                "Samstag" to sat,
                "Sonntag" to sun)
                .mapNotNull { input ->
                    input.second?.takeUnless { it.isEmpty() }?.joinToString(prefix = "${input.first}: ")
                }
                .joinToString("<br/>\n").takeUnless { it.isBlank() }
    }

}