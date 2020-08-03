package de.deutschebahn.bahnhoflive

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.android.volley.Network
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HttpStack
import com.android.volley.toolbox.HurlStack
import de.deutschebahn.bahnhoflive.analytics.IssueTracker
import de.deutschebahn.bahnhoflive.analytics.TrackingDelegate
import de.deutschebahn.bahnhoflive.analytics.TrackingHttpStack
import de.deutschebahn.bahnhoflive.backend.*
import de.deutschebahn.bahnhoflive.backend.db.DbAuthorizationTool
import de.deutschebahn.bahnhoflive.repository.ApplicationServices
import de.deutschebahn.bahnhoflive.repository.RepositoryHolder
import de.deutschebahn.bahnhoflive.repository.elevator.Fasta2ElevatorStatusRepository
import de.deutschebahn.bahnhoflive.repository.poisearch.PoiSearchConfigurationProvider
import de.deutschebahn.bahnhoflive.repository.station.PublicTrainStationStationRepository
import de.deutschebahn.bahnhoflive.repository.travelcenter.PublicTrainStationTravelCenterRepository
import de.deutschebahn.bahnhoflive.tutorial.TutorialManager
import de.deutschebahn.bahnhoflive.ui.TimeTableProvider
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

    lateinit var timeTableProvider: TimeTableProvider
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

        applicationServices = ApplicationServices(this)

        issueTracker = onInitializeIssueTracker()

        trackingDelegate = onInitializeTrackingDelegate()

        val requestQueue = createRequestQueue()
        restHelper = RestHelper(requestQueue)
        timeTableProvider = TimeTableProvider()
        poiSearchConfigurationProvider = PoiSearchConfigurationProvider(this)
        TutorialManager.getInstance(this).seedTutorials()

        repositories = onCreateRepositories(restHelper)
    }

    protected open fun onInitializeIssueTracker(): IssueTracker = IssueTracker(this)

    protected open fun onCreateRepositories(restHelper: RestHelper): RepositoryHolder {
        val dbAuthorizationTool = DbAuthorizationTool(BuildConfig.BUSINESS_HUB_API_KEY)

        return RepositoryHolder(
            stationRepository = PublicTrainStationStationRepository(
                restHelper,
                dbAuthorizationTool
            ),
            travelCenterRepository = PublicTrainStationTravelCenterRepository(
                restHelper,
                dbAuthorizationTool
            ),
            elevatorStatusRepository = Fasta2ElevatorStatusRepository(
                restHelper,
                dbAuthorizationTool
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