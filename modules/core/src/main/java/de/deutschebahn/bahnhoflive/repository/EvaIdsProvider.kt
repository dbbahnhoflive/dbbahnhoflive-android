package de.deutschebahn.bahnhoflive.repository

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds

typealias EvaIdsProvider = suspend Station.() -> EvaIds?
