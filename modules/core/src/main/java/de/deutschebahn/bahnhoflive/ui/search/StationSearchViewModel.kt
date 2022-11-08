/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search

import androidx.lifecycle.ViewModel
import de.deutschebahn.bahnhoflive.BaseApplication

class StationSearchViewModel : ViewModel() {

    val timetableCollectorFactory = BaseApplication.get().repositories.timetableRepository

    val searchResource = SearchResultResource()

}