package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.backend.RestHelper
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.backend.db.ris.RISJourneysByRelationRequest
import de.deutschebahn.bahnhoflive.backend.db.ris.model.DepartureMatches

open class RisTimetableRepository(
    protected val restHelper: RestHelper,
    protected val dbAuthorizationTool: DbAuthorizationTool
) : TimetableRepository() {
    override fun queryJourneysByRelation(
        listener: VolleyRestListener<DepartureMatches>,
        number: String,
        category: String,
        line: String?
    ) {
        restHelper.add(
            RISJourneysByRelationRequest(
                RISJourneysByRelationRequest.Parameters(
                    number, category, line
                ), dbAuthorizationTool, listener
            )
        )
    }
}
