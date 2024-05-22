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
    val stationId:Int=0,
    val stationName : String = "",  // SPNV-Halt = Schienenpersonennahverkehr
    val hasDbCompanion:Boolean=false,
    val evStopName:String="" // ev = Ersatzverkehr
)
    private val testIsDbCompanionServiceAvailable=true // todo: false in production
    private val testHasDbCompanion=true // todo: false in production
    private val testIsInAnnouncementPhase=true // todo: false in production
    private val testIsInConstructionPhase=false // todo: false in production

    // // cal-format: year (2024), month (1..12), day (1..31), hour (0..23), minute (0..59), second (0..59)
    private val startOfAnnouncementPhase = arrayOf(2024, 7, 8, 0, 0, 0) // dummy
    private val endOfAnnouncementPhase = arrayOf(2024, 7, 14, 23, 59, 59)

    private val startOfConstructionPhase =  arrayOf(2024, 7, 15,  0,0,0) // Wegbegleitung
    private val endOfConstructionPhase = arrayOf(2024, 12, 14,  23,59,59)

    private val startOfShowAdHocBox = arrayOf(2024, 7, 8, 0, 0, 0)
    private val endOfShowAdHocBox = arrayOf(2034, 12, 31, 23, 59, 59) // dummy

    private val ev_items = arrayOf<EvItem>(

        EvItem(2545,  "Hannover",true,"hinten"), // Test Hannover
        EvItem(1866, "Frankfurt (Main) Hbf", true,"Hbf Südseite (Mannheimer Straße, vor Parkhaus neben IC-Hotel)")

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

        c.set(Calendar.HOUR_OF_DAY, 22)
        c.set(Calendar.MINUTE, 0)
        val today22_00h = c.timeInMillis

        return (((day >= Calendar.MONDAY && day <= Calendar.FRIDAY) && (now >= today7_00h && now < today22_00h)) || testIsDbCompanionServiceAvailable)
    }

    fun containsStationId(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull()
        return (stationIdAsInt != null && ev_items.map { it.stationId }.contains(stationIdAsInt))
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
        return ev_items.indexOfFirst { (it.stationId == stationIdAsInt) && isInConstructionPhase } >= 0
    }

    fun isStationInConstructionPhase(station: MergedStation): Boolean {
        return isStationInConstructionPhase(station.id)
    }

    fun hasStationDbCompanion(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        return ev_items.indexOfFirst {
            (it.stationId == stationIdAsInt) && (it.hasDbCompanion || testHasDbCompanion)
        } >= 0
    }

    fun isStationSPNV_Stop(stationId: String?): Boolean {
        val stationIdAsInt = stationId?.toIntOrNull() ?: 0
        return ev_items.indexOfFirst {
            (it.stationId == stationIdAsInt)
        } >= 0
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
        return ev_items.map {it.stationName}
    }
}
