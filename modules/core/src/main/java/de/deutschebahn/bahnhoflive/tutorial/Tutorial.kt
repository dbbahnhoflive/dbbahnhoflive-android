/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.tutorial

class Tutorial(// view
    @JvmField var id: String, var title: String, @JvmField var descriptionText: String, countdown: Int
) {
    @JvmField
    var currentCount = Int.MAX_VALUE
    @JvmField
    var countdown = Int.MAX_VALUE // until show...
    @JvmField
    var closedByUser = false

    init {
        this.countdown = countdown
        currentCount = countdown
    }

    override fun toString(): String {
        return "Tutorial{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", descriptionText='" + descriptionText + '\'' +
                ", currentCount=" + currentCount +
                ", countdown=" + countdown +
                ", closedByUser=" + closedByUser +
                '}'
    }
}
