package de.deutschebahn.bahnhoflive.repository

import android.content.Context

class AssetDocumentBroker(context: Context) {

    companion object {
        const val FILE_NAME_LEGAL_NOTICE = "imprint.html"
        const val FILE_NAME_PRIVACY_POLICY = "datenschutz.html"
    }

    val assets = context.applicationContext.assets

    private fun hasFile(fileName: String) = assets.list("")?.contains(fileName) == true

    val hasLegalNotice get() = hasFile(FILE_NAME_LEGAL_NOTICE)

    val hasPrivacyPolicy get() = hasFile(FILE_NAME_PRIVACY_POLICY)


}