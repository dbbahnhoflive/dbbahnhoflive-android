/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package de.deutschebahn.bahnhoflive.backend.hafas

import android.util.Log
import com.android.volley.Cache
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import de.deutschebahn.bahnhoflive.BuildConfig
import de.deutschebahn.bahnhoflive.analytics.Trackable
import de.deutschebahn.bahnhoflive.backend.BaseRequest
import de.deutschebahn.bahnhoflive.backend.CappingHttpStack
import de.deutschebahn.bahnhoflive.backend.CappingHttpStack.Cappable
import de.deutschebahn.bahnhoflive.backend.Countable
import de.deutschebahn.bahnhoflive.backend.ForcedCacheEntryFactory
import de.deutschebahn.bahnhoflive.backend.VolleyRestListener
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

abstract class HafasRequest<T>(
    method: Int, endpoint: String, parameters: String, origin: String,
    listener: VolleyRestListener<T>, shouldCache: Boolean, minimumCacheTime: Int
) : BaseRequest<T>(method, (endpoint + parameters).replace(" ".toRegex(), "%20"), listener), Countable,
    Trackable, Cappable {
    private val endpoint: String
    private val parameters: String
    private val trackingContextVariables: MutableMap<String, Any> = HashMap()
    private val cacheOverrider: ForcedCacheEntryFactory

    init {
        setShouldCache(shouldCache)
        retryPolicy = DefaultRetryPolicy(
            10 * 1000,
            3,
            1.2f
        )
        this.endpoint = endpoint
        this.parameters = parameters
        cacheOverrider = ForcedCacheEntryFactory(minimumCacheTime)
        setTrackingContextVariable("origin", origin)
        setTrackingContextVariable("endpoint", endpoint)
    }


    override fun getCountKey(): String? {
        return endpoint
    }

    override fun getTrackingTag(): String {
        return "request : hafas:$legacyTrackingTag"
    }

    protected fun getCacheEntry(response: NetworkResponse): Cache.Entry {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "getCacheEntry: " + response.statusCode + " headers: " + response.headers.toString())
        }
        return cacheOverrider.createCacheEntry(response)
    }

    abstract val legacyTrackingTag: String
    override fun getTrackingContextVariables(): Map<String, Any> {
        return trackingContextVariables
    }

    private fun setTrackingContextVariable(key: String, value: String) {
        trackingContextVariables[key] = value
    }

    override fun isFailOnExcess(): Boolean {
        return true
    }

    override fun getCapTag(): String {
        return CappingHttpStack.CapTag.HAFAS
    }

    companion object {
        private val TAG = HafasRequest::class.java.simpleName
        @JvmStatic
        protected fun encodeParameter(value: String?): String {
            return try {
                URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8.toString())
            } catch (e: UnsupportedEncodingException) {
                @Suppress("DEPRICATED")
                URLEncoder.encode(value)
            }
        }
    }
}
