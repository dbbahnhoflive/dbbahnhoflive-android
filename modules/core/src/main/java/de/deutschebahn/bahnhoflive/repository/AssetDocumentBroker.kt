/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.repository

import android.content.Context
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.ui.WebViewActivity
import java.util.Calendar
import java.util.Locale

class AssetDocumentBroker(
    private val context: Context,
    private val trackingManager: TrackingManager = TrackingManager()
) {

    companion object {
        const val FILE_NAME_LEGAL_NOTICE_EXPIRING = "impressum.html"
        const val FILE_NAME_LEGAL_NOTICE_2024_01_01 = "impressum_2024-01-01.html"
        const val FILE_NAME_PRIVACY_POLICY_2024_07_01 = "datenschutz_2024-07-01.html"
        const val FILE_NAME_PRIVACY_POLICY_2024_01_01 = "datenschutz_2024-01-01.html"
    }

    val assets = context.applicationContext.assets

    private fun hasFile(fileName: String) = assets.list("")?.contains(fileName) == true

    val hasLegalNotice get() = hasFile(getCurrentLegalNotice().assetFileName)

    val hasPrivacyPolicy get() = hasFile(getCurrentPrivacyPolicy().assetFileName)

    enum class Document(
        val assetFileName: String,
        val trackingTag: String,
        val title: String
    ) {
        LEGAL_NOTICE_EXPRIRING(
            FILE_NAME_LEGAL_NOTICE_EXPIRING, TrackingManager.Entity.IMPRESSUM, "Impressum"
        ),
        LEGAL_NOTICE_UPCOMING(
            FILE_NAME_LEGAL_NOTICE_2024_01_01, TrackingManager.Entity.IMPRESSUM, "Impressum"
        ),
        PRIVACY_POLICY_EXPIRING(
            FILE_NAME_PRIVACY_POLICY_2024_01_01, TrackingManager.Entity.DATENSCHUTZ, "Datenschutz"
        ),
        PRIVACY_POLICY_UPCOMING(
            FILE_NAME_PRIVACY_POLICY_2024_07_01, TrackingManager.Entity.DATENSCHUTZ, "Datenschutz"
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

    fun getCurrentPrivacyPolicy() =
        Calendar.getInstance(Locale.GERMANY).let { calendar ->
            if (calendar.get(Calendar.YEAR) <= 2024 &&
                calendar.get(Calendar.MONTH) < Calendar.JULY
            )
                Document.PRIVACY_POLICY_EXPIRING
            else Document.PRIVACY_POLICY_UPCOMING
        }

    fun getCurrentLegalNotice() =
        Calendar.getInstance(Locale.GERMANY).get(Calendar.YEAR).let { currentYear ->
            if (currentYear < 2024) Document.LEGAL_NOTICE_EXPRIRING else Document.LEGAL_NOTICE_UPCOMING
        }

}