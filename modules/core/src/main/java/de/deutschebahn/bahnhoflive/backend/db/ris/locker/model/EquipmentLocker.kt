/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.backend.db.ris.locker.model

class EquipmentLocker {

    var equipmentID: String? = null
    var stationID: String? = null
    var lockers: List<Locker?>? = null
}