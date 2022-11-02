/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableCollectorFactory

class StationSearchViewModel : ViewModel() {

    val timetableCollectorFactory = TimetableCollectorFactory(viewModelScope)

    val searchResource = SearchResultResource()

}