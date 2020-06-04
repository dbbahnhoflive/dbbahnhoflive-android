package de.deutschebahn.bahnhoflive.backend.wagenstand.favendo

import de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model.LegacyTrain
import de.deutschebahn.bahnhoflive.backend.wagenstand.favendo.model.Wagenstand
import de.deutschebahn.bahnhoflive.repository.trainformation.Train
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation
import de.deutschebahn.bahnhoflive.repository.trainformation.toTrain
import de.deutschebahn.bahnhoflive.repository.trainformation.toWaggon

fun Wagenstand.toTrainFormation(): TrainFormation {
    val trainMapping = mutableMapOf<LegacyTrain, Train>()
    val trains = subtrains.map {
        val train = it.toTrain()
        trainMapping[it] = train
        train
    }

    return TrainFormation(
            waggons.map {
                it.toWaggon(getDestinatinationForWaggon(it)?.let { legacyTrain ->
                    trainMapping[legacyTrain]
                })
            },
            trains,
            time,
            platform,
            isReversed,
            trainNumbers.first(),
            false
    )
}