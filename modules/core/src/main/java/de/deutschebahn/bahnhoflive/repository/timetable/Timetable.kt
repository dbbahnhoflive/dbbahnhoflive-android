/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository.timetable

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import de.deutschebahn.bahnhoflive.backend.ris.model.RISTimetable
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainEvent
import de.deutschebahn.bahnhoflive.backend.ris.model.TrainInfo
import de.deutschebahn.bahnhoflive.util.DebugX
import java.util.*


class Timetable(private val trainInfos: List<TrainInfo>, val endTime: Long, val duration: Int = 2) :
    Parcelable {

    val departures = RISTimetable.determineSplitMessages(
        RISTimetable.filter(trainInfos,
            TrainEvent.DEPARTURE), TrainEvent.DEPARTURE)
        .prepare(TrainEvent.DEPARTURE)

    val arrivals = RISTimetable.determineSplitMessages(
        RISTimetable.filter(trainInfos,
            TrainEvent.ARRIVAL), TrainEvent.ARRIVAL)
        .prepare(TrainEvent.ARRIVAL)

    fun List<TrainInfo>.prepare(trainEvent: TrainEvent) =
        filter {
            trainEvent.movementRetriever.getTrainMovementInfo(it).let { it.plannedDateTime.before(endTime) || it.correctedDateTime.before(endTime) }
        }
            .sortedBy {
                trainEvent.movementRetriever.getTrainMovementInfo(it).let {
                    it.plannedDateTime.takeUnless { it < 0 }
                        ?: it.correctedDateTime
                }
            }

    protected constructor(`in`: Parcel) : this(
        `in`.createTypedArrayList(TrainInfo.CREATOR) ?: mutableListOf(),
        `in`.readLong(),
        `in`.readInt()
    )

//    init {
//        DebugX.logDateTimeFromMillis(endTime, "endTime :")
//    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedList(trainInfos)
        dest.writeLong(endTime)
        dest.writeInt(duration)
    }

    fun getTrainInfos(): List<TrainInfo> {
        return trainInfos
    }

    companion object CREATOR : Parcelable.Creator<Timetable> {
        override fun createFromParcel(parcel: Parcel): Timetable {
            return Timetable(parcel)
        }

        override fun newArray(size: Int): Array<Timetable?> {
            return arrayOfNulls(size)
        }
    }

    fun Long.before(other: Long) = this > 0 && (other < 0 || other >= this)



//    fun logIt(preText:String) {
//
//        Log.d(preText, "Timetable Abfahrten ${this.endTime}")
//
//        for(i in 0..6) {
//           val dep = departures[i]
//            val stops = dep.departure.via.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
//            val n = stops.size
//            Log.d(preText, DebugX.getFormattedDateTimeFromMillis(dep.departure.plannedDateTime,
//                "", "HH:mm")  + " " + dep.trainCategory + " " +  dep.trainName + " nach  " + stops[n-1])
//        }
//
//    }
}
