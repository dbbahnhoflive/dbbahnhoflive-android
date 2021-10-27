/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.local.model

import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.repository.Station


private val String.isChatbotAvailable
    get() = true //ChatbotStation.isInTeaserPeriod && ChatbotStation.ids.contains(this)

val Station.isChatbotAvailable: Boolean
    get() = true //id.isChatbotAvailable

val DetailedStopPlace?.isChatbotAvailable
    get() = true //stadaId.isChatbotAvailable
