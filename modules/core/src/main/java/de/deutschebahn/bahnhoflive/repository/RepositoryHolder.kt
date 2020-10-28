/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import android.content.Context
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.repository.elevator.ElevatorStatusRepository
import de.deutschebahn.bahnhoflive.repository.localtransport.LocalTransportRepository
import de.deutschebahn.bahnhoflive.repository.map.MapRepository
import de.deutschebahn.bahnhoflive.repository.misc.EinkaufsbahnhofRepository
import de.deutschebahn.bahnhoflive.repository.news.NewsRepository
import de.deutschebahn.bahnhoflive.repository.occupancy.OccupancyRepository
import de.deutschebahn.bahnhoflive.repository.parking.ParkingRepository
import de.deutschebahn.bahnhoflive.repository.station.StationRepository
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.repository.wagonorder.WagonOrderRepository

data class RepositoryHolder(
    val stationRepository: StationRepository = StationRepository(),
    val localTransportRepository: LocalTransportRepository = LocalTransportRepository(),
    val newsRepository: NewsRepository = NewsRepository(),
    val elevatorStatusRepository: ElevatorStatusRepository = ElevatorStatusRepository(),
    val wagonOrderRepository: WagonOrderRepository = WagonOrderRepository(),
    val timetableRpository: TimetableRepository = TimetableRepository(),
    val einkaufsbahnhofRepository: EinkaufsbahnhofRepository = EinkaufsbahnhofRepository(),
    val mapRepository: MapRepository = MapRepository(),
    val parkingRepository: ParkingRepository = ParkingRepository(),
    val occupancyRepository: OccupancyRepository = OccupancyRepository()
)

val Context.appRepositories get(): RepositoryHolder = (applicationContext as BaseApplication).repositories