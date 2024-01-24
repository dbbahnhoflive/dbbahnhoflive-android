/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive

import android.content.Context
import android.os.Build
import androidx.multidex.MultiDexApplication
import com.android.volley.Network
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import de.deutschebahn.bahnhoflive.analytics.IssueTracker
import de.deutschebahn.bahnhoflive.analytics.TrackingDelegate
import de.deutschebahn.bahnhoflive.analytics.TrackingHttpStack
import de.deutschebahn.bahnhoflive.backend.*
import de.deutschebahn.bahnhoflive.backend.db.MultiHeaderDbAuthorizationTool
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
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
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


        // SSL-Fix
        if (Build.VERSION.SDK_INT in 17..20) {
            try {
                ProviderInstaller.installIfNeeded(applicationContext)
            } catch (e: GooglePlayServicesRepairableException) {
                e.printStackTrace()
            } catch (e: GooglePlayServicesNotAvailableException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: KeyManagementException) {
                e.printStackTrace()
            }
        }


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

//            FacilityFirebaseService.debugPrintFirebaseToken()
//
//            // todo !!!!!!!!! cr:test
//            if (FacilityPushManager.isPushEnabled(this)) {
//
//                val testEquipmentNumbers = arrayOf(
//
//                    10315223, 10503244, 10315352, 10804843, 10491002, 10409032,
//                    10464407, 10490981, 10801908, 10801910, 10569817, 10185526,
//                    10315224, 10500157, 10015807, 10121792, 10316250, 10318903,
//                    10315225, 10482243, 10315353, 10015810, 10060095, 10028019,
//                    10464408, 10500158, 10801909, 10028028, 10316251, 10500168,
//                    10470423, 10314752, 10020626, 10020397, 10316245, 10499262,
//                    10122518, 10561326, 10015809, 10015811, 10316246, 10504602,
//                    10299484, 10563637, 10315354, 10779734, 10316254, 10449075,
//                    10315228, 10776764, 10315425, 10015805, 10316332, 10020629,
//                    10315229, 10561327, 10315355, 10801913, 10804989, 10448345,
//                    10028022, 10563638, 10315426, 10015812, 10316256, 10020636,
//                    10318901, 10499260, 10020635, 10015813, 10316334, 10020637,
//                    10408331, 10499261, 10020993, 10015806, 10315222, 10316332,
//                    10315425, 10315355, 10315353, 10316332, 10315425, 10315355,
//                    10315353, 10560984
//                )
//
//                val fpm: FacilityPushManager = FacilityPushManager.instance
//
//                testEquipmentNumbers.forEach {
//                    fpm.subscribePushMessage(this, it)
//                }
//            }

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
        stack: BaseHttpStack
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