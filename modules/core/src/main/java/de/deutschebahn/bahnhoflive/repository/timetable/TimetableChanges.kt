package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo

class TimetableChanges(
        val evaId: String,
        val trainInfos: List<TrainInfo>
)