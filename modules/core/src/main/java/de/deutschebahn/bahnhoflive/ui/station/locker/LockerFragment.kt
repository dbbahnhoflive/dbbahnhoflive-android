/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.locker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.databinding.FragmentLockerBinding
import de.deutschebahn.bahnhoflive.databinding.IncludeItemLockerBinding
import de.deutschebahn.bahnhoflive.repository.locker.AnyLockerInitialPoi
import de.deutschebahn.bahnhoflive.repository.locker.FeePeriod
import de.deutschebahn.bahnhoflive.repository.locker.PaymentType
import de.deutschebahn.bahnhoflive.ui.map.Content
import de.deutschebahn.bahnhoflive.ui.map.InitialPoiManager
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel
import de.deutschebahn.bahnhoflive.util.setAccessibilityText

class LockerFragment : Fragment(), MapPresetProvider {

    val stationViewModel by activityViewModels<StationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentLockerBinding.inflate(inflater, container, false).apply {

        titleBar.staticTitleBar.screenTitle.setText(R.string.stationinfo_lockers)

        contentList.forEach {
            it.visibility = INVISIBLE
        }

        stationViewModel.lockers.categorizedLockersLiveData.observe(
            viewLifecycleOwner
        ) { uiLockers ->

            if (uiLockers != null) {

                contentList.removeAllViews()

                uiLockers.forEach {

                    contentList.addView(IncludeItemLockerBinding.inflate(inflater).apply {

                        val lockerTypes = resources.getStringArray(R.array.locker_types)
                        lockerSize.text = lockerTypes[it.lockerType.ordinal]
                        if (it.isShortTimeLocker)
                            lockerSize.text = String.format(
                                "%s (%s)",
                                lockerTypes[it.lockerType.ordinal],
                                getString(R.string.locker_short_term)
                            )
                        else
                            lockerSize.text = lockerTypes[it.lockerType.ordinal]

                        lockerAmount.text = getString(R.string.locker_amount_lockers, it.amount)

                        if (it.dimDepth != 0 && it.dimHeight != 0 && it.dimWidth != 0) {
                            lockerDimensions.text = getString(
                                R.string.locker_dimensions,
                                it.dimDepth,
                                it.dimWidth,
                                it.dimHeight
                            )
                            lockerDimensions.setAccessibilityText(
                                getString(
                                    R.string.locker_dimensions_VO,
                                    it.dimDepth,
                                    it.dimWidth,
                                    it.dimHeight
                                )
                            )
                        }


                        var string = ""

                        if (it.paymentTypes.contains(PaymentType.CARD))
                            string += getString(R.string.locker_payment_type_cashless)
                        if (it.paymentTypes.contains(PaymentType.CASH)) {
                            if (string.isNotEmpty())
                                string += ", "
                            string += getString(R.string.locker_payment_type_cash)
                        }
                        if (it.paymentTypes.contains(PaymentType.UNKNOWN)) {
                            if (string.isNotEmpty())
                                string += ", "
                            string += getString(R.string.locker_payment_type_unknown)
                        }
                        lockerPaymentTypes.text = getString(R.string.locker_payment, string)

                        val maxLeaseDurationAsString = it.getMaxLeaseDurationAsHumanReadableString()
                        val maxLeaseDurationAsStringVO =
                            it.getMaxLeaseDurationAsHumanReadableString(
                                getString(R.string.date_years),
                                getString(R.string.date_months),
                                getString(R.string.date_weeks),
                                getString(R.string.date_days),
                                getString(R.string.date_hours),
                                getString(R.string.date_minutes),
                                getString(R.string.date_seconds)
                            )


                        if (maxLeaseDurationAsString.isNotBlank()) {
                            lockerMaxLeaseDuration.text = resources.getString(
                                R.string.locker_max_lease_duration,
                                maxLeaseDurationAsString
                            )

                            lockerMaxLeaseDuration.setAccessibilityText(
                                resources.getString(
                                    R.string.locker_max_lease_duration_VO,
                                    maxLeaseDurationAsStringVO
                                )
                            )
                        }

                        val lockerFeePeriods = resources.getStringArray(R.array.locker_fee_periods)
                        string =
                            String.format(lockerFeePeriods[it.feePeriod.ordinal], it.feeAsString)
                        if (it.feePeriod == FeePeriod.PER_MAX_LEASE_DURATION)
                            string += maxLeaseDurationAsString
                        lockerFee.text = string

                        val lockerFeePeriodsVO =
                            resources.getStringArray(R.array.locker_fee_periods_VO)
                        string =
                            String.format(lockerFeePeriodsVO[it.feePeriod.ordinal], it.feeAsString)
                        if (it.feePeriod == FeePeriod.PER_MAX_LEASE_DURATION)
                            string += maxLeaseDurationAsStringVO
                        lockerFee.setAccessibilityText(string)

                    }.root)
                }

            }

        }


    }.root

    companion object {
        val TAG: String = LockerFragment::class.java.simpleName
    }

    override fun onStart() {
        super.onStart()

        stationViewModel.topInfoFragmentTag = TAG

        TrackingManager.fromActivity(activity).track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D1,
            TrackingManager.Category.SCHLIESSFAECHER
        )
    }

    override fun onStop() {
        if (stationViewModel.topInfoFragmentTag == TAG) {
            stationViewModel.topInfoFragmentTag = null
        }

        super.onStop()
    }

    override fun prepareMapIntent(intent: Intent): Boolean {
        RimapFilter.putPreset(intent, RimapFilter.PRESET_LOCKERS)
        InitialPoiManager.putInitialPoi(intent, Content.Source.RIMAP, AnyLockerInitialPoi)

        return true
    }
}