/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.backend.db.fasta2.model.FacilityStatus
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType
import de.deutschebahn.bahnhoflive.repository.locker.LockerResource
import de.deutschebahn.bahnhoflive.repository.parking.ParkingsResource
import de.deutschebahn.bahnhoflive.ui.ServiceContentFragment
import de.deutschebahn.bahnhoflive.ui.station.*
import de.deutschebahn.bahnhoflive.ui.station.accessibility.AccessibilityFragment
import de.deutschebahn.bahnhoflive.ui.station.elevators.ElevatorStatusListsFragment
import de.deutschebahn.bahnhoflive.ui.station.features.RISServicesAndCategory
import de.deutschebahn.bahnhoflive.ui.station.locker.LockerFragment
import de.deutschebahn.bahnhoflive.ui.station.parking.ParkingListFragment
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.RailReplacementFragment
import de.deutschebahn.bahnhoflive.ui.station.railreplacement.SEV_Static
import de.deutschebahn.bahnhoflive.util.Collections

class InfoCategorySelectionFragment : CategorySelectionFragment(
    R.string.title_stationinfo_categories,
    TrackingManager.Source.INFO
) {

    val stationViewModel by activityViewModels<StationViewModel>()

    private lateinit var infoAndServicesLiveData: InfoAndServicesLiveData
    private lateinit var serviceNumbersLiveData: ServiceNumbersLiveData
    private lateinit var detailedStationLiveData: LiveData<RISServicesAndCategory?>
    private lateinit var staticInfoLiveData: LiveData<StaticInfoCollection?>
    private lateinit var parkingsResource: ParkingsResource
    private lateinit var lockersResource: LockerResource

    private lateinit var elevatorsDataResource: LiveData<List<FacilityStatus>?>
    private lateinit var railReplacementSummaryLiveData: LiveData<MutableMap<String, MutableList<String?>>?>

    private var infoAndServicesCategory: Category? = null
    private var serviceNumbersCategory: Category? = null
    private var wifiCategory: Category? = null
    private var accessibilityCategory: Category? = null
    private var parkingsCategory: Category? = null
    private var elevatorsCategory: Category? = null
    private var railReplacementCategory: Category? = null
    private var lockerCategory: Category? = null

    private fun updateCategories() {
        if (isAdded) {
            val station = detailedStationLiveData.value
            val staticInfoCollection = staticInfoLiveData.value ?: return

            infoAndServicesCategory = addInfoAndServices(infoAndServicesLiveData.value)
            serviceNumbersCategory = addServiceNumbers(serviceNumbersLiveData.value)
            wifiCategory = addWifi(station, staticInfoCollection)
            accessibilityCategory = addAccessibility()
            parkingsCategory = addParkings()

            if (station != null) {
                if (!railReplacementSummaryLiveData.value.isNullOrEmpty() ||  SEV_Static.containsStationId(
                        station.station?.stationID
                    )) {
                        railReplacementCategory = addRailReplacement()
                    }

                    if (Collections.hasContent(elevatorsDataResource.value)) {
                        elevatorsCategory = addElevators()
                    }
            }

            lockerCategory = addLockers()

            adapter?.setCategories(
                listOfNotNull(
                    infoAndServicesCategory,
                    serviceNumbersCategory,
                    wifiCategory,
                    accessibilityCategory,
                    parkingsCategory,
                    elevatorsCategory,
                    railReplacementCategory,
                    lockerCategory
                )
            )
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateCategories()
    }

    private fun addServiceNumbers(serviceContents: List<ServiceContent>?): SimpleDynamicCategory? =
        serviceContents?.takeUnless { it.isEmpty() }?.let {
            val labelResource = serviceNumbersLabelResource
            SimpleDynamicCategory(getText(labelResource),
                R.drawable.app_service_rufnummern, TrackingManager.Category.SERVICE_UND_RUFNUMMERN,
                Category.CategorySelectionListener { category ->
                    trackCategoryTap(category)
                    startStationInfoDetailsFragment(it, category, labelResource)
                })
        }

    private val serviceNumbersLabelResource get() = R.string.stationinfo_service_phone_numbers

    private fun addInfoAndServices(infoAndServicesList: List<ServiceContent>?) =
        infoAndServicesList?.takeUnless { it.isEmpty() }?.let {
            SimpleDynamicCategory(getText(R.string.stationinfo_infos_and_services),
                R.drawable.app_info,
                TrackingManager.Category.INFOS_UND_SERVICES,
                Category.CategorySelectionListener { category ->
                    trackCategoryTap(category)
                    startStationInfoDetailsFragment(
                        it,
                        category,
                        R.string.stationinfo_infos_and_services
                    )
                })
        }

    private fun startStationInfoDetailsFragment(
        serviceContents: List<ServiceContent>,
        category: Category,
        titleResource: Int
    ) {
        val stationInfoDetailsFragment = StationInfoDetailsFragment.create(
            ArrayList(serviceContents), getText(
                titleResource
            ), category.trackingTag
        )
        startFragment(stationInfoDetailsFragment)
    }


    private fun addElevators() =
        SimpleDynamicCategory(
            getText(R.string.title_elevators_and_escalators),
            R.drawable.bahnhofsausstattung_aufzug,
            TrackingManager.Category.AUFZUEGE, Category.CategorySelectionListener { category ->
                trackCategoryTap(category)
                startFragment(ElevatorStatusListsFragment.create())
            })

    private fun addParkings() =
        parkingsResource.data.value?.takeUnless { it.isEmpty() }?.let {
            SimpleDynamicCategory(getText(R.string.stationinfo_parkings),
                R.drawable.bahnhofsausstattung_parkplatz,
                TrackingManager.Category.PARKPLAETZE,
                Category.CategorySelectionListener { category ->
                    trackCategoryTap(category)
                    startFragment(ParkingListFragment.create())
                })
        }

    private fun addAccessibility(): SimpleDynamicCategory? {
        return SimpleDynamicCategory(
            "Barrierefreiheit", R.drawable.app_zugang_wege,
            TrackingManager.Category.BARRIEREFREIHEIT
        ) { category ->
            trackCategoryTap(category)
            startFragment(AccessibilityFragment())
        }
    }

    private fun addRailReplacement(): SimpleDynamicCategory? {
        return SimpleDynamicCategory(
            getText(R.string.rail_replacement), R.drawable.app_rail_replacement,
            TrackingManager.Category.SCHIENENERSATZVERKEHR
        ) { category ->
            trackCategoryTap(category)
            startFragment(RailReplacementFragment())
        }
    }

    private fun addWifi(
        station: RISServicesAndCategory?,
        staticInfoCollection: StaticInfoCollection
    ): SimpleDynamicCategory? {
        if (station == null || !station.hasWifi) {
            return null
        }

        val staticInfo = staticInfoCollection.typedStationInfos[ServiceContentType.WIFI]
        return if (staticInfo != null) {
            SimpleDynamicCategory(
                staticInfo.title, R.drawable.rimap_wlan_grau,
                TrackingManager.Category.WLAN, ServiceContentCategorySelectionListener(
                    ServiceContent(
                        staticInfo
                    )
                )
            )
        } else {
            null
        }
    }

    private fun addLockers() =
        lockersResource.data.value?.takeUnless { it.isEmpty() }?.let {
            SimpleDynamicCategory(getText(R.string.stationinfo_lockers),
                R.drawable.bahnhofsausstattung_schlie_faecher,
                TrackingManager.Category.SCHLIESSFAECHER,
                Category.CategorySelectionListener { category ->
                    trackCategoryTap(category)
                    startFragment(LockerFragment())
                })
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detailedStationLiveData = stationViewModel.risServiceAndCategoryResource.data
        elevatorsDataResource = stationViewModel.elevatorsResource.data
        infoAndServicesLiveData = stationViewModel.infoAndServicesLiveData
        serviceNumbersLiveData = stationViewModel.serviceNumbersLiveData
        parkingsResource = stationViewModel.parking.parkingsResource
        staticInfoLiveData = stationViewModel.staticInfoLiveData
        railReplacementSummaryLiveData = stationViewModel.railReplacementSummaryLiveData

        lockersResource = stationViewModel.lockers.lockerResource

        staticInfoLiveData.observe(this) {
            updateCategories()
        }
        parkingsResource.data.observe(this) {
            updateCategories()
        }

        elevatorsDataResource.observe(this) { updateCategories() }
        detailedStationLiveData.observe(this) { updateCategories() }
        infoAndServicesLiveData.observe(this) { updateCategories() }
        serviceNumbersLiveData.observe(this) { updateCategories() }
        railReplacementSummaryLiveData.observe(this) { updateCategories() }

        lockersResource.data.observe(this) {
            updateCategories()
        }


        stationViewModel.selectedServiceContentType.observe(this, Observer {
            if (it != null) {
                when {
                    setOf(
                        ServiceContentType.DB_INFORMATION,
                        ServiceContentType.MOBILE_SERVICE,
                        ServiceContentType.BAHNHOFSMISSION,
                        ServiceContentType.Local.TRAVEL_CENTER,
                        ServiceContentType.Local.DB_LOUNGE
                    ).contains(it) -> {
                        infoAndServicesLiveData.value?.let { serviceContents ->
                            infoAndServicesCategory?.let { category ->
                                startStationInfoDetailsFragment(
                                    serviceContents,
                                    category,
                                    R.string.stationinfo_infos_and_services
                                )
                            }
                        }
                    }

                    it == ServiceContentType.DummyForCategory.FEEDBACK -> {
                        stationViewModel.selectedServiceContentType.value = null
                        startFeedbackCategory()

                    }

                    serviceNumbersLiveData.value?.any { serviceContent ->
                        serviceContent.type == it
                    } == true -> {
                        startFeedbackCategory()
                    }

                    it == ServiceContentType.WIFI -> {
                        wifiCategory?.let { category ->
                            staticInfoLiveData.value?.typedStationInfos?.get(ServiceContentType.WIFI)
                                ?.let { staticInfo ->
                                    startServiceContentFragment(staticInfo, category)
                                }
                        }
                    }

                    it == ServiceContentType.ACCESSIBLE -> {
                        accessibilityCategory?.let { category ->
                            staticInfoLiveData.value?.typedStationInfos?.get(ServiceContentType.ACCESSIBLE)
                                ?.let { staticInfo ->
                                    startServiceContentFragment(staticInfo, category)
                                }
                        }
                    }
                }
            }
        })
    }

    private fun startFeedbackCategory() {
        serviceNumbersLiveData.value?.let { serviceContents ->
            serviceNumbersCategory?.let { category ->
                startStationInfoDetailsFragment(
                    serviceContents,
                    category,
                    serviceNumbersLabelResource
                )
            }
        }
    }

    private fun startServiceContentFragment(staticInfo: StaticInfo, category: Category): Int {
        val serviceContent = ServiceContent(staticInfo)
        return startFragment(
            ServiceContentFragment.create(
                serviceContent.title,
                serviceContent,
                category.trackingTag
            )
        )
    }

    fun startFragment(fragment: Fragment): Int {
        return HistoryFragment.parentOf(this@InfoCategorySelectionFragment).push(fragment)
    }

    private inner class ServiceContentCategorySelectionListener(private val serviceContent: ServiceContent) : Category.CategorySelectionListener {

        override fun onCategorySelected(category: Category) {
            trackCategoryTap(category)

            startFragment(
                ServiceContentFragment.create(
                    serviceContent.title,
                    serviceContent,
                    category.trackingTag
                )
            )
        }
    }
}
