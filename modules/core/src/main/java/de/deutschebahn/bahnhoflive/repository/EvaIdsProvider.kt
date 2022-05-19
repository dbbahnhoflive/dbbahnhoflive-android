package de.deutschebahn.bahnhoflive.repository

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds

interface EvaIdsProvider {
    fun withEvaIds(station: Station, action: (evaIds: EvaIds?) -> Unit)
}

open class SimpleEvaIdsProvider(
    val getEvaIds: () -> EvaIds?
) : EvaIdsProvider {
    override fun withEvaIds(station: Station, action: (evaIds: EvaIds?) -> Unit) {
        action(getEvaIds())
    }
}
