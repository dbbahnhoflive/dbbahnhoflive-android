package de.deutschebahn.bahnhoflive.backend.local.model

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.repository.Station
import java.util.*


private val String.isChatbotAvailable
    get() = ChatbotStation.isInTeaserPeriod && ChatbotStation.ids.contains(this)

val Station.isChatbotAvailable: Boolean
    get() = id.isChatbotAvailable

val DetailedStopPlace.isChatbotAvailable
    get() = stadaId.isChatbotAvailable == true

object ChatbotStation {

    val ids = setOf(
        "27", // Ahrensburg
        "2516", // Sternschanze
        "6859", // Wolfsburg Hbf
        "1059", // Coburg
        "1908", // Freising
        "5226", // Renningen (noch unklar, wird vll kein Zukunftsbahnhof sein)
        "2648", // Heilbronn Hbf
        "2498", // Halle (Saale) Hbf
        "6692", // Wernigerode
        "4280", // Münster Hbf (Anm. Maik: Münster (Westfalen))
        "2510", // Haltern am See
        "4859", // Berlin Südkreuz
        "791", // Berlin Bornholmer Straße
        "1077", // Cottbus
        "7171", // Offenbach Marktplatz
        "2827", // Hofheim (Anm. Maik: Hofheim(Taunus))
        "2514", // Hamburg Hbf
        "1866", // Frankfurt (Main) Hbf
        "4234", // München Hbf
        "3320", // Köln Hbf
        "1071", // Berlin Hbf
        "3631", // Leipzig Hbf
        "1401", // Düsseldorf Hbf
        "2545", // Hannover Hbf
        "527", // Berlin Friedrichstraße
        "4809" // Berlin Ostkreuz
    )

    private val teaserPeriodStart by lazy {
        createDateCalendar(2019, Calendar.NOVEMBER, 18)
    }

    /**
     * First day of inactive period.
     */
    private val teaserPeriodEnd by lazy {
        createDateCalendar(2021, Calendar.JANUARY, 1)
    }

    private fun createDateCalendar(year: Int, month: Int, dayOfMonth: Int) =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    val isInTeaserPeriod: Boolean
        get() =
            Calendar.getInstance().run {
                before(teaserPeriodEnd) && after(teaserPeriodStart)
            }
}