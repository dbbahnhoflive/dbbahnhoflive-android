package de.deutschebahn.bahnhoflive.repository

import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds

interface EvaIdsProvider {
    fun withEvaIds(action: (evaIds: EvaIds?) -> Unit)
}

class SimpleEvaIdsProvider(
    val deliver: () -> EvaIds?
) : EvaIdsProvider {
    override fun withEvaIds(action: (evaIds: EvaIds?) -> Unit) {
        action(deliver())
    }
}