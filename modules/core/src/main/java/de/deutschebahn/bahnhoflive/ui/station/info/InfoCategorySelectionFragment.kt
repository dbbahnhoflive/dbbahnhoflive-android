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
import de.deutschebahn.bahnhoflive.backend.db.publictrainstation.model.DetailedStopPlace
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent
import de.deutschebahn.bahnhoflive.repository.parking.ParkingsResource
import de.deutschebahn.bahnhoflive.ui.ServiceContentFragment
import de.deutschebahn.bahnhoflive.ui.station.*
import de.deutschebahn.bahnhoflive.ui.station.elevators.ElevatorStatusListsFragment
import de.deutschebahn.bahnhoflive.ui.station.parking.ParkingListFragment
import de.deutschebahn.bahnhoflive.util.Collections

class InfoCategorySelectionFragment : CategorySelectionFragment(
    R.string.title_stationinfo_categories,
    TrackingManager.Source.INFO
) {

    val stationViewModel by activityViewModels<StationViewModel>()

    private lateinit var infoAndServicesLiveData: InfoAndServicesLiveData
    private lateinit var serviceNumbersLiveData: ServiceNumbersLiveData
    private lateinit var detailedStationLiveData: LiveData<DetailedStopPlace?>
    private lateinit var staticInfoLiveData: LiveData<StaticInfoCollection?>
    private lateinit var parkingsResource: ParkingsResource

    private lateinit var elevatorsDataResource: LiveData<List<FacilityStatus>?>

    private var infoAndServicesCategory: Category? = null
    private var serviceNumbersCategory: Category? = null
    private var wifiCategory: Category? = null
    private var accessibilityCategory: Category? = null
    private var parkingsCategory: Category? = null
    private var elevatorsCategory: Category? = null

    private fun updateCategories() {
        if (isAdded) {
            val station = detailedStationLiveData.value
            val staticInfoCollection = staticInfoLiveData.value
            if (station == null || staticInfoCollection == null) {
                return
            }

            infoAndServicesCategory = addInfoAndServices(infoAndServicesLiveData.value)
            serviceNumbersCategory = addServiceNumbers(serviceNumbersLiveData.value)
            wifiCategory = addWifi(station, staticInfoCollection)
            accessibilityCategory = addAccessibility(station, staticInfoCollection)
            parkingsCategory = addParkings()

            if (Collections.hasContent(elevatorsDataResource.value)) {
                elevatorsCategory = addElevators()
            }

            adapter?.setCategories(
                listOfNotNull(
                    infoAndServicesCategory,
                    serviceNumbersCategory,
                    wifiCategory,
                    accessibilityCategory,
                    parkingsCategory,
                    elevatorsCategory
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

    private fun addAccessibility(
        station: DetailedStopPlace?,
        staticInfoCollection: StaticInfoCollection
    ): SimpleDynamicCategory? {
        if (station == null || !station.hasSteplessAccess) {
            return null
        }

        val staticInfo = staticInfoCollection.typedStationInfos[ServiceContent.Type.ACCESSIBLE]
            ?: return null

        val steplessAccessInfo = station.steplessAccessInfo
        val serviceContent = ServiceContent(
            staticInfo,
            if (steplessAccessInfo == null) null else "Zusatzinfomation: $steplessAccessInfo"
        )
        return SimpleDynamicCategory(
            serviceContent.title, R.drawable.app_zugang_wege,
            TrackingManager.Category.ZUGANG_WEGE, ServiceContentCategorySelectionListener(
                serviceContent
            )
        )
    }

    private fun addWifi(
        station: DetailedStopPlace?,
        staticInfoCollection: StaticInfoCollection
    ): SimpleDynamicCategory? {
        if (station == null || !station.hasWifi) {
            return null
        }

        val staticInfo = staticInfoCollection.typedStationInfos[ServiceContent.Type.WIFI]
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detailedStationLiveData = stationViewModel.detailedStopPlaceResource.data
        elevatorsDataResource = stationViewModel.elevatorsResource.data
        infoAndServicesLiveData = stationViewModel.infoAndServicesLiveData
        serviceNumbersLiveData = stationViewModel.serviceNumbersLiveData
        parkingsResource = stationViewModel.parking.parkingsResource
        staticInfoLiveData = stationViewModel.staticInfoLiveData

        staticInfoLiveData.observe(this, Observer {
            updateCategories()
        })
        parkingsResource.data.observe(this, Observer {
            updateCategories()
        })

        elevatorsDataResource.observe(this, Observer { updateCategories() })
        detailedStationLiveData.observe(this, Observer { updateCategories() })
        infoAndServicesLiveData.observe(this, Observer { updateCategories() })
        serviceNumbersLiveData.observe(this, Observer { updateCategories() })

        stationViewModel.selectedServiceContentType.observe(this, Observer {
            if (it != null) {
                when {
                    setOf(
                        ServiceContent.Type.DB_INFORMATION,
                        ServiceContent.Type.MOBILE_SERVICE,
                        ServiceContent.Type.BAHNHOFSMISSION,
                        ServiceContent.Type.Local.TRAVEL_CENTER,
                        ServiceContent.Type.Local.DB_LOUNGE
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

                    setOf(
                        ServiceContent.Type.MOBILITY_SERVICE,
                        ServiceContent.Type.MOBILITY_SERVICE_DEPRECATED,
                        ServiceContent.Type.THREE_S,
                        ServiceContent.Type.Local.LOST_AND_FOUND,
                        ServiceContent.Type.Local.CHATBOT
                    ).contains(it) -> {
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

                    it == ServiceContent.Type.WIFI -> {
                        wifiCategory?.let { category ->
                            staticInfoLiveData.value?.typedStationInfos?.get(ServiceContent.Type.WIFI)
                                ?.let { staticInfo ->
                                    startServiceContentFragment(staticInfo, category)
                                }
                        }
                    }

                    it == ServiceContent.Type.ACCESSIBLE -> {
                        accessibilityCategory?.let { category ->
                            staticInfoLiveData.value?.typedStationInfos?.get(ServiceContent.Type.ACCESSIBLE)
                                ?.let { staticInfo ->
                                    startServiceContentFragment(staticInfo, category)
                                }
                        }
                    }
                }
            }
        })
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
