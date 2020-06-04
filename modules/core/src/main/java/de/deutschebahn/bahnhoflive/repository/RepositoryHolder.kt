package de.deutschebahn.bahnhoflive.repository

import android.content.Context
import de.deutschebahn.bahnhoflive.BaseApplication
import de.deutschebahn.bahnhoflive.repository.elevator.ElevatorStatusRepository
import de.deutschebahn.bahnhoflive.repository.localtransport.LocalTransportRepository
import de.deutschebahn.bahnhoflive.repository.map.MapRepository
import de.deutschebahn.bahnhoflive.repository.misc.EinkaufsbahnhofRepository
import de.deutschebahn.bahnhoflive.repository.news.NewsRepository
import de.deutschebahn.bahnhoflive.repository.station.StationRepository
import de.deutschebahn.bahnhoflive.repository.timetable.TimetableRepository
import de.deutschebahn.bahnhoflive.repository.travelcenter.TravelCenterRepository
import de.deutschebahn.bahnhoflive.repository.wagonorder.WagonOrderRepository

data class RepositoryHolder(
    val stationRepository: StationRepository = StationRepository(),
    val localTransportRepository: LocalTransportRepository = LocalTransportRepository(),
    val travelCenterRepository: TravelCenterRepository = TravelCenterRepository(),
    val newsRepository: NewsRepository = NewsRepository(),
    val elevatorStatusRepository: ElevatorStatusRepository = ElevatorStatusRepository(),
    val wagonOrderRepository: WagonOrderRepository = WagonOrderRepository(),
    val timetableRepository: TimetableRepository = TimetableRepository(),
    val einkaufsbahnhofRepository: EinkaufsbahnhofRepository = EinkaufsbahnhofRepository(),
    val mapRepository: MapRepository = MapRepository()
)

val Context.appRepositories get(): RepositoryHolder = (applicationContext as BaseApplication).repositories