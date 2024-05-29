package de.deutschebahn.bahnhoflive.ui.station.railreplacement

import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.backend.db.newsapi.GroupId
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.Group
import de.deutschebahn.bahnhoflive.backend.db.newsapi.model.News
import de.deutschebahn.bahnhoflive.repository.MergedStation
import de.deutschebahn.bahnhoflive.util.isActualDateInRange
import java.util.Calendar


object SEV_Static_Riedbahn {
// Juli-Dezember 2024
class EvItem(
    val evaId:Int=0,
    val stationName : String = ""  // SPNV-Halt = Schienenpersonennahverkehr
)
    private const val testIsDbCompanionServiceAvailable=false // todo: false in production
    private const val testIsInAnnouncementPhase=false // todo: false in production
    private const val testIsInConstructionPhase=false // todo: false in production

    // // cal-format: year (2024), month (1..12), day (1..31), hour (0..23), minute (0..59), second (0..59)
    private val startOfAnnouncementPhase = arrayOf(2024, 7, 8, 0, 0, 0) // dummy
    private val endOfAnnouncementPhase = arrayOf(2024, 7, 14, 23, 59, 59)

    private val startOfConstructionPhase =  arrayOf(2024, 7, 15,  0,0,0) // Wegbegleitung
    private val endOfConstructionPhase = arrayOf(2024, 12, 14,  23,59,59)

    private val startOfShowAdHocBox = arrayOf(2024, 7, 8, 0, 0, 0)
    private val endOfShowAdHocBox = arrayOf(2034, 12, 31, 23, 59, 59) // dummy

//         EvItem(2545,  8000152,"Hannover Hbf"), // Test Hannover

    private val evMap : Map<Int, EvItem> = mapOf(
//        2545 to EvItem(8000152,"Hannover Hbf"), // Test Hannover
        1866 to EvItem(8000105, "Frankfurt Hbf"),
        1876 to EvItem(8002050, "Frankfurt-Niederrad"),
        1854 to EvItem(8002040, "Frankfurt Stadion"),
        8268 to EvItem(8002060, "Frankfurt-Gateway Gardens"),
        6999 to EvItem(8006648, "NI-Zeppelinheim"),
        6503 to EvItem(8006421, "Wiesloch-Walldorf"),
        4174 to EvItem(8004065, "Mörfelden"),
        2299 to EvItem(8000136, "Groß-Gerau"),
        2300 to EvItem(8002386, "Groß Gerau-Dornberg"),
        1278 to EvItem(8001511, "Groß Gerau-Dornheim"),
        3608 to EvItem(8003605, "Riedstadt-Wolfskehlen"),
        2161 to EvItem(8000126, "Riedstadt-Goddelau"),
        6035 to EvItem(8005739, "Stockstadt (Rhein)"),
        619 to EvItem(8000951, "Biebesheim"),
        2097 to EvItem(8002249, "Gernsheim"),
        2316 to EvItem(8002391, "Groß-Rohrheim"),
        614 to EvItem(8000503, "Biblis"),
        721 to EvItem(8001034, "Bobstadt"),
        1002 to EvItem(8098360, "Bürstadt"),
        3500 to EvItem(8003503, "Lampertheim"),
        3936 to EvItem(8003848, "Mannheim-Waldhof"),
        3931 to EvItem(8006509, "Mannheim-Luzenberg"),
        3933 to EvItem(8006511, "Mannheim-Neckarstadt"),
        3929 to EvItem(8006508, "Mannheim Handelshafen"),
        3925 to EvItem(8000244, "Mannheim Hbf"),
        3898 to EvItem(8000240, "Mainz Hbf"),
        3900 to EvItem(8003816, "Mainz Röm. Theater"),
        3905 to EvItem(8003819, "Mainz-Laubenheim"),
        739 to EvItem(8000359, "Bodenheim"),
        4293 to EvItem(8004193, "Nackenheim"),
        4551 to EvItem(8004432, "Nierstein"),
        4772 to EvItem(8004680, "Oppenheim"),
        8252 to EvItem(8001448, "Dienheim"),
        2419 to EvItem(8002474, "Guntersblum"),
        66 to EvItem(8000506, "Alsheim"),
        4082 to EvItem(8004003, "Mettenheim"),
        4808 to EvItem(8004714, "Osthofen"),
        6887 to EvItem(8000257, "Worms Hbf"),
        716 to EvItem(8001032, "Bobenheim"),
        1848 to EvItem(8000332, "Frankenthal Hbf"),
        8210 to EvItem(8002025, "Frankenthal Süd"),
        3839 to EvItem(8003766, "Ludwigshafen-Oggersheim"),
        3836 to EvItem(8000236, "Ludwigshafen (Rhein) Hbf"),
        7385 to EvItem(8003759, "Ludwigshafen Mitte"),
//        4351 to EvItem(8004246, "Neu-Isenburg"),
        3524 to EvItem(8003523, "Langen"),
        1126 to EvItem(8000068, "Darmstadt Hbf"),
        1129 to EvItem(8001377, "Darmstadt Süd"),
        1131 to EvItem(8001379, "Darmstadt-Eberstadt"),
        8264 to EvItem(8004816, "Pfungstadt"),
        618 to EvItem(8000948, "Bickenbach"),
        2471 to EvItem(8002498, "Hähnlein-Alsbach"),
        7075 to EvItem(8006687, "Zwingenberg"),
        489 to EvItem(8000877, "Bensheim-Auerbach"),
        488 to EvItem(8000031, "Bensheim"),
        2693 to EvItem(8002757, "Heppenheim"),
        3578 to EvItem(8003571, "Laudenbach"),
        2684 to EvItem(8002748, "Hemsbach"),
        8290 to EvItem(8006283, "Sulzbach"),
        6622 to EvItem(8000377, "Weinheim Hbf"),
        3873 to EvItem(8003792, "Weinheim-Lützelsachsen"),
        2362 to EvItem(8002430, "Heddesheim/Hirschberg"),
        3490 to EvItem(8003489, "Ladenburg"),
        5272 to EvItem(8005089, "Riedrode"),
        3786 to EvItem(8003755, "Lorsch")
    )



    // Mo-Fr 7-22 Uhr
    fun isCompanionServiceAvailable() : Boolean   {
        val c = Calendar.getInstance()

        val now = c.timeInMillis
        val day = c.get(Calendar.DAY_OF_WEEK)

        c.set(Calendar.MILLISECOND, 0)

        c.set(Calendar.HOUR_OF_DAY, 7)
        c.set(Calendar.MINUTE, 0)
        val today7_00h = c.timeInMillis

        c.set(Calendar.HOUR_OF_DAY, 19)
        c.set(Calendar.MINUTE, 0)
        val today19_00h = c.timeInMillis

        return (((day >= Calendar.MONDAY && day <= Calendar.FRIDAY) && (now >= today7_00h && now < today19_00h)) || testIsDbCompanionServiceAvailable)
    }

    fun containsStationId(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull()
        return (stationIdAsInt != null && evMap.containsKey(stationIdAsInt))
    }


    fun isInAnnouncementPhase() : Boolean {
        return isActualDateInRange(startOfAnnouncementPhase, endOfAnnouncementPhase) || testIsInAnnouncementPhase
    }

    fun isInConstructionPhase() : Boolean {
        return isActualDateInRange(startOfConstructionPhase, endOfConstructionPhase) || testIsInConstructionPhase
    }


    // "+++ Ersatzverkehr...+++  Box auch anzeigen wenn Bauphase noch nicht begonnen hat !
    // siehe NewsAdapter,  "Ankündigung Ersatzverkehr"
    fun shouldShowAdhocBox() : Boolean {
        return isActualDateInRange(startOfShowAdHocBox, endOfShowAdHocBox)
    }


    fun isStationInConstructionPhase(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        val isInConstructionPhase = isInConstructionPhase()
        return isInConstructionPhase && evMap[stationIdAsInt] !=null
    }

    fun isStationInConstructionPhase(station: MergedStation): Boolean {
        return isStationInConstructionPhase(station.id)
    }

    fun hasStationDbCompanionByStationId(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        return evMap[stationIdAsInt] !=null
    }

    fun hasStationDbCompanionByEvaId(evaId: String?): Boolean {
        val evaIdAsInt = evaId?.toIntOrNull() ?: 0
        return evMap.filter { it.value.evaId==evaIdAsInt }.isNotEmpty()
    }


    fun isStationReplacementStopByStationID(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        return evMap[stationIdAsInt] !=null
    }

    fun isStationReplacementStopByEvaID(evaId: String?): Boolean {
        val evaIdAsInt = evaId?.toIntOrNull() ?: 0
        return evMap.filter { it.value.evaId==evaIdAsInt  }.isNotEmpty()
    }

    fun getSEV_News(stationId: String?) : List<News>? {

        if(containsStationId(stationId)) {
            val isInAnnouncementPhase = isInAnnouncementPhase()
            val isInConstructionPhase = isStationInConstructionPhase(stationId)
            val showAdhoc = shouldShowAdhocBox()

            if (isInAnnouncementPhase || isInConstructionPhase || showAdhoc) {

                // create content of adhoc box

                val l = mutableListOf<News>()
                val n = News()
                val g = Group()

                g.id = GroupId.REPLACEMENT_ANNOUNCEMENT.id
                g.title = BaseApplication.get() // ggf. animierte überschrift
                    .getString(if (isInAnnouncementPhase) R.string.nev_announcement_headline else R.string.nev_phase_headline)
                g.optionalData = BaseApplication.get()
                    .getString(if (isInAnnouncementPhase) R.string.sr_nev_announcement_headline else R.string.sr_nev_phase_headline)

                n.group = g

                n.id = "1"
                n.title = BaseApplication.get().getString(R.string.sev_date_range)
                n.titleForScreenReader = BaseApplication.get().getString(R.string.sr_sev_date_range)
                n.content = BaseApplication.get().getString(R.string.nev_copy)
                n.contentForScreenReader = BaseApplication.get().getString(R.string.sr_nev_copy)

                val cal = Calendar.getInstance()
                cal.add(Calendar.DATE, -1)
                n.startTimestamp = cal.time
                cal.add(Calendar.DATE, 1)
                n.endTimestamp = cal.time
                cal.add(Calendar.DATE, -10)
                n.createdAt = cal.time

                l.add(n)
                return l
            }

        }
        return null
    }

    fun getSEVStationNames() : List<String> {
        return evMap.map{it.value.stationName}.sortedBy { it }
    }
}
