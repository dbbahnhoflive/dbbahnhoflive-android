/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import android.content.Context
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.WebViewActivity

class AssetDocumentBroker(
    private val context: Context,
    private val trackingManager: TrackingManager = TrackingManager()
) {

    companion object {
        const val FILE_NAME_LEGAL_NOTICE = "imprint.html"
        const val FILE_NAME_PRIVACY_POLICY = "datenschutz.html"
    }

    val assets = context.applicationContext.assets

    private fun hasFile(fileName: String) = assets.list("")?.contains(fileName) == true

    val hasLegalNotice get() = hasFile(FILE_NAME_LEGAL_NOTICE)

    val hasPrivacyPolicy get() = hasFile(FILE_NAME_PRIVACY_POLICY)

    enum class Document(
        val assetFileName: String,
        val trackingTag: String,
        val title: String
    ) {
        LEGAL_NOTICE(
            FILE_NAME_LEGAL_NOTICE, TrackingManager.Entity.IMPRESSUM, "Impressum"
        ),
        PRIVACY_POLICY(
            FILE_NAME_PRIVACY_POLICY, TrackingManager.Entity.DATENSCHUTZ, "Datenschutz"
        )
    }

    fun showDocument(document: Document) {
        trackingManager.track(
            TrackingManager.TYPE_STATE,
            TrackingManager.Screen.D2,
            document.trackingTag
        )

        val intent = WebViewActivity.createIntent(context, document.assetFileName, document.title)
        context.startActivity(intent)
    }
}