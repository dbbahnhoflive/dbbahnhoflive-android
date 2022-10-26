/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.multidex.MultiDexApplication
import com.android.volley.Network
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HttpStack
import com.android.volley.toolbox.HurlStack
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import de.deutschebahn.bahnhoflive.analytics.IssueTracker
import de.deutschebahn.bahnhoflive.analytics.TrackingDelegate
import de.deutschebahn.bahnhoflive.analytics.TrackingHttpStack
import de.deutschebahn.bahnhoflive.backend.*
import de.deutschebahn.bahnhoflive.backend.db.MultiHeaderDbAuthorizationTool
import de.deutschebahn.bahnhoflive.push.FacilityPushManager
import de.deutschebahn.bahnhoflive.push.createNotificationChannels
import de.deutschebahn.bahnhoflive.repository.ApplicationServices
import de.deutschebahn.bahnhoflive.repository.RepositoryHolder
import de.deutschebahn.bahnhoflive.repository.elevator.Fasta2ElevatorStatusRepository
import de.deutschebahn.bahnhoflive.repository.locker.LockerInfoLockerRepository
import de.deutschebahn.bahnhoflive.repository.parking.ParkingInfoParkingRepository
import de.deutschebahn.bahnhoflive.repository.poisearch.PoiSearchConfigurationProvider
import de.deutschebahn.bahnhoflive.repository.station.OfficialStationRepository
import de.deutschebahn.bahnhoflive.repository.timetable.RisTimetableRepository
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.util.font.FontUtil
import de.deutschebahn.bahnhoflive.util.volley.TLSSocketFactory
import java.io.File
import javax.net.ssl.SSLSocketFactory


abstract class BaseApplication(
    val versionName: String,
    val versionCode: Int
) : MultiDexApplication() {

    lateinit var issueTracker: IssueTracker

    lateinit var restHelper: RestHelper
        private set

    lateinit var poiSearchConfigurationProvider: PoiSearchConfigurationProvider
        private set

    lateinit var repositories: RepositoryHolder
        private set

    lateinit var trackingDelegate: TrackingDelegate

    lateinit var applicationServices: ApplicationServices
        private set

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        FontUtil.init(this)


        issueTracker = onInitializeIssueTracker()

        trackingDelegate = onInitializeTrackingDelegate()

        val requestQueue = createRequestQueue()
        restHelper = RestHelper(requestQueue)
        poiSearchConfigurationProvider = PoiSearchConfigurationProvider(this)
        TutorialManager.getInstance(this).seedTutorials()

        repositories = onCreateRepositories(restHelper)
        applicationServices = ApplicationServices(this, repositories)

        createNotificationChannels()

        if (BuildConfig.DEBUG) {
            try {
                Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
                    .invoke(null, true)
            } catch (e: ReflectiveOperationException) {
                throw java.lang.RuntimeException(e)
            }

            FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
                if (!TextUtils.isEmpty(token)) {
                    Log.d("cr", "retrieve token successful : $token")
                } else {
                    Log.d("cr", "token should not be null...")
                }
            }.addOnFailureListener { e: Exception? -> }.addOnCanceledListener {}
                .addOnCompleteListener { task: Task<String> ->
                    Log.d(
                        "cr", "This is the token : $task.result"
                    )
                }
            // todo !!!!!!!!! cr:test
            if (FacilityPushManager.isPushEnabled(this)) {

                val testEquipmentNumbers = arrayOf(
                    10503244,
                    10490981,
                    10500157,
                    10482243,
                    10500158,
                    10314752,
                    10561326,
                    10563637,
                    10776764,
                    10561327,
                    10563638,
                    10499260,
                    10499261,
                    10500168,
                    10499262,
                    10504602,
                    10449075,
                    10491002,
                    10569817,
                    10316250,
                    10060095,
                    10316251,
                    10316245,
                    10316246,
                    10316254,
                    10316332,
                    10804989,
                    10316256,
                    10316334,
                    10315222,
                    10315223,
                    10464407,
                    10315224,
                    10315225,
                    10464408,
                    10470423,
                    10122518,
                    10299484,
                    10315228,
                    10315229,
                    10028022,
                    10318901,
                    10408331,
                    10409032,
                    10185526,
                    10318903,
                    10028019,
                    10804843,
                    10801910,
                    10121792,
                    10015810,
                    10028028,
                    10020397,
                    10015811,
                    10779734,
                    10015805,
                    10801913,
                    10015812,
                    10015813,
                    10015806,
                    10315352,
                    10801908,
                    10015807,
                    10315353,
                    10801909,
                    10020626,
                    10015809,
                    10315354,
                    10315425,
                    10315355,
                    10315426,
                    10020635,
                    10020993,
                    10020629,
                    10448345,
                    10020636,
                    10020637
                )

                val fpm: FacilityPushManager = FacilityPushManager.instance

                testEquipmentNumbers.forEach {
                    fpm.subscribe(it)
                }

            }

        }
    }

    
    protected open fun onInitializeIssueTracker(): IssueTracker = IssueTracker(this)

    protected open fun onCreateRepositories(restHelper: RestHelper): RepositoryHolder {
        val risAndParkingAuthorizationTool = MultiHeaderDbAuthorizationTool(
            BuildConfig.RIS_STATIONS_API_KEY, BuildConfig.RIS_STATIONS_CLIENT_ID
        )

        return RepositoryHolder(
            stationRepository = OfficialStationRepository(
                restHelper,
                risAndParkingAuthorizationTool
            ),
            elevatorStatusRepository = Fasta2ElevatorStatusRepository(
                restHelper,
                MultiHeaderDbAuthorizationTool(
                    BuildConfig.FASTA_API_KEY, BuildConfig.FASTA_CLIENT_ID
                )
            ),
            timetableRepository = RisTimetableRepository(
                restHelper, risAndParkingAuthorizationTool
            ),
            lockerRepository = LockerInfoLockerRepository(
                restHelper,
                risAndParkingAuthorizationTool
            ),
            parkingRepository = ParkingInfoParkingRepository(
                restHelper,
                risAndParkingAuthorizationTool
            )
        )
    }

    open fun onInitializeTrackingDelegate() = TrackingDelegate()

    private fun createRequestQueue(): RequestQueue {
        val defaultSSLSocketFactory =
            SSLSocketFactory.getDefault() as? SSLSocketFactory
                ?: throw RuntimeException()
        val trackingHttpStack = TrackingHttpStack(
            CountingHttpStack(
                HurlStack(
                    null,
                    TLSSocketFactory(defaultSSLSocketFactory)
                ),
                RequestCounter(this)
            )
        )
        return createRequestQueue(
            this,
            CappingHttpStack(
                LoggingHttpStack(
                    trackingHttpStack
                )
            )
        )
    }

    /**
     * This method is a customization of [Volley.newRequestQueue].
     */
    private fun createRequestQueue(
        context: Context,
        stack: HttpStack
    ): RequestQueue {
        val cacheDir = File(context.cacheDir, "volley")
        val network: Network = BasicNetwork(stack)
        val queue = RequestQueue(DiskBasedCache(cacheDir, 200 * 1024 * 1024), network)
        queue.start()
        return queue
    }

    val isActive = false

    companion object {
        lateinit var INSTANCE: BaseApplication
            private set

        @JvmStatic
        fun get() = INSTANCE
    }
}