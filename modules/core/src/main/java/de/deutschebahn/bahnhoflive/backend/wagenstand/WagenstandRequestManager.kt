/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.wagenstand

import android.util.Log
import com.android.volley.VolleyError
import de.deutschebahn.bahnhoflive.BaseApplication.Companion.get
import de.deutschebahn.bahnhoflive.backend.BaseRestListener
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import de.deutschebahn.bahnhoflive.backend.local.model.EvaIds
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.model.WagenstandIstResponseData
import de.deutschebahn.bahnhoflive.backend.wagenstand.istwr.toTrainFormation
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger

class WagenstandRequestManager(private val listener: VolleyRestListener<TrainFormation>) {
    private val noOfRequestsToWaitFor = AtomicInteger(0)

    private var trainFormation: TrainFormation? = null

    private val fallbackTrainFormation: TrainFormation? = null

    fun loadWagenstand(evaIds: EvaIds, trainNumber: String, trainCategory:String?, date:String?, time: String?) {
        loadWagenstandIst(trainNumber, trainCategory, date, time, evaIds.ids)
    }

    private fun loadWagenstandIst(
        trainNumber: String,
        trainCategory : String?,
        date:String?,
        time: String?,
        evaIds: List<String>
    ) {
//        var dateTime: String? = ""
//        dateTime = if (!TextUtils.isEmpty(time)) {
//            FORMATTERDATE.format(Date()) + time!!.replace(":", "")
//        } else {
//            FORMATTERDATE_TIME.format(Date())
//        }

        val baseApplication = get()
        val risTransportRepository = baseApplication.repositories.risTransportRepository

        for (evaId in evaIds) {
            noOfRequestsToWaitFor.getAndIncrement()

            risTransportRepository.queryWagonOrder(evaId, trainNumber, trainCategory, date,
                object : BaseRestListener<WagenstandIstResponseData>() {
                    override fun onSuccess(payload: WagenstandIstResponseData) {
                        trainFormation = payload.toTrainFormation()
                        noOfRequestsToWaitFor.getAndSet(0) // No need to wait for other
                        this@WagenstandRequestManager.onSuccess()
                    }

                    override fun onFail(reason: VolleyError) {
                        Log.d("cr", "qryTransports.fail")
                        noOfRequestsToWaitFor.getAndDecrement()
                        this@WagenstandRequestManager.onFail()

                    }
                }
            )

//            wagonOrderRepository.queryWagonOrder(object :
//                BaseRestListener<WagenstandIstResponseData>() {
//                override fun onSuccess(payload: WagenstandIstResponseData) {
//                    trainFormation = payload.toTrainFormation()
//
//                    Log.d(TAG, "Received IST Wagenstand")
//
//                    noOfRequestsToWaitFor.getAndSet(0) // No need to wait for other
//
//                    this@WagenstandRequestManager.onSuccess()
//                }
//
//                override fun onFail(reason: VolleyError) {
//                    noOfRequestsToWaitFor.getAndDecrement()
//
//                    // Note: API returns 400 if EVA-ID doesn't return a valid result
//                    //if (noOfRequestsToWaitFor.get() == 0) {
//                    this@WagenstandRequestManager.onFail()
//
//                    //}
//                }
//            }, evaId, trainNumber, dateTime!!)
        }
    }

    /**
     * Checks for completion of all Wagenstand Requests.
     * Tries to merge IST and SOLL if both are available. It is assured that there
     * will always be a non-null, although the response given to the listener might
     * of type VolleyError.
     */
    fun onSuccess() {
        if (noOfRequestsToWaitFor.get() == 0) {
            if (trainFormation != null) {
                // If IST is available, it's enough to return only one Plan
                listener.onSuccess(trainFormation!!)
                return
            }

            if (fallbackTrainFormation != null) {
                listener.onSuccess(fallbackTrainFormation)
            } else {
                listener.onFail(VolleyError("Invalid response received"))
            }
        }
    }

    fun onFail() {
        if (fallbackTrainFormation != null) {
            listener.onSuccess(fallbackTrainFormation)
        } else if (noOfRequestsToWaitFor.get() == 0) {
            listener.onFail(VolleyError("Invalid response received"))
        }
    }

    companion object {
        private val FORMATTERDATE = SimpleDateFormat("yyyyMMdd")
        private val FORMATTERDATE_TIME = SimpleDateFormat("yyyyMMddHHmm")
    }
}
