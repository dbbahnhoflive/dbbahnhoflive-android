package de.deutschebahn.bahnhoflive.util

import android.net.MailTo
import android.net.Uri

class MailUri(url: String? = null) {

    var to: String? = null
    var cc: String? = null
    var subject: String? = null
    var body: String? = null

    init {
        if (MailTo.isMailTo(url)) {
            MailTo.parse(url).also { source ->
                to = source.to
                cc = source.cc
                subject = source.subject
                body = source.body
            }
        }
    }

    fun build() = Uri.parse(

        "mailto:$to" + sequenceOf(
            "cc" to cc,
            "subject" to subject,
            "body" to body
        ).filter { it.second != null }.joinToString("&", "?") {
            Uri.encode(it.first) + "=" + Uri.encode(it.second)
        }
    )

    private fun Uri.Builder.parameter(key: String, value: String?) {
        value?.let { appendQueryParameter(key, it) }
    }
}