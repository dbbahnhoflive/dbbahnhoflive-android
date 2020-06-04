package de.deutschebahn.bahnhoflive.repository.timetable

import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo

class TimetableHour(
        val hour: Long,
        val evaId: String,
        val trainInfos: List<TrainInfo>
)