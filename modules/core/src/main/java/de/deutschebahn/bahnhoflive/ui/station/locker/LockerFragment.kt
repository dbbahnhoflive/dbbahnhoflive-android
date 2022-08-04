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
import de.deutschebahn.bahnhoflive.repository.locker.FeePeriod
import de.deutschebahn.bahnhoflive.repository.locker.PaymentType
import de.deutschebahn.bahnhoflive.ui.map.MapPresetProvider
import de.deutschebahn.bahnhoflive.ui.map.content.rimap.RimapFilter
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel


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

                        var string = ""

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
                        lockerSize.contentDescription = lockerSize.text

                        lockerAmount.text = getString(R.string.locker_amount_lockers, it.amount)
                        lockerAmount.contentDescription = lockerAmount.text

                        lockerDimensions.text = getString(
                            R.string.locker_dimensions,
                            it.dimDepth,
                            it.dimWidth,
                            it.dimHeight
                        )
                        lockerDimensions.contentDescription = lockerDimensions.text
//                        getString(
//                            R.string.locker_dimensions_VO,
//                            it.dimDepth,
//                            it.dimWidth,
//                            it.dimHeight
//                        )

                        if (it.paymentTypes.contains(PaymentType.CARD))
                            string += "Karte"
                        if (it.paymentTypes.contains(PaymentType.CASH)) {
                            if (!string.isEmpty())
                                string += ", "
                            string += "bar"
                        }
                        if (it.paymentTypes.contains(PaymentType.UNKNOWN)) {
                            if (!string.isEmpty())
                                string += ", "
                            string += "unbekannt"
                        }
                        lockerPaymentTypes.text = getString(R.string.locker_payment, string)
                        lockerPaymentTypes.contentDescription = lockerPaymentTypes.text


                        var maxLeaseDurationAsString = (it.datePart + it.timePart)

                        if (maxLeaseDurationAsString.length > 2)
                            maxLeaseDurationAsString = maxLeaseDurationAsString.dropLast(2)


                        var datePartVO = it.datePart
                        var timePartVO = it.timePart

                        datePartVO = datePartVO
                            .replace("y", getString(R.string.date_years))
                            .replace("m", getString(R.string.date_months))
                            .replace("w", getString(R.string.date_weeks))
                            .replace("d", getString(R.string.date_days))

                        timePartVO = timePartVO
                            .replace("s", getString(R.string.date_seconds))
                            .replace("h", getString(R.string.date_hours))
                            .replace("m", getString(R.string.date_minutes))

                        var maxLeaseDurationAsStringVO = (datePartVO + timePartVO)

                        if (maxLeaseDurationAsStringVO.length > 2)
                            maxLeaseDurationAsStringVO = maxLeaseDurationAsStringVO.dropLast(2)

                        lockerMaxLeaseDuration.text = resources.getString(
                            R.string.locker_max_lease_duration,
                            maxLeaseDurationAsString
                        )
                        lockerMaxLeaseDuration.contentDescription = resources.getString(
                            R.string.locker_max_lease_duration_VO,
                            maxLeaseDurationAsStringVO
                        )


                        val lockerFeePeriods = resources.getStringArray(R.array.locker_fee_periods)
                        string =
                            String.format(lockerFeePeriods[it.feePeriod.ordinal], it.feeAsString)
                        if (it.feePeriod == FeePeriod.PER_MAX_LEASE_DURATION)
                            string += maxLeaseDurationAsString //maxLeaseDurationDateTimePart
                        lockerFee.text = string

                        string =
                            String.format(lockerFeePeriods[it.feePeriod.ordinal], it.feeAsString)
                        if (it.feePeriod == FeePeriod.PER_MAX_LEASE_DURATION)
                            string += maxLeaseDurationAsStringVO

                        lockerFee.contentDescription = "Hand" // string

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

        return true
    }
}