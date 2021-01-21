package de.deutschebahn.bahnhoflive.view

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View


typealias UrlClickListener = (String) -> Unit

class InterceptedURLSpan(
    val url: String,
    val urlClickListener: UrlClickListener
) : ClickableSpan() {

    override fun onClick(widget: View) {
        urlClickListener(url)
    }

}

fun CharSequence.replaceURLSpans(urlClickListener: UrlClickListener) =
    (this as? Spannable)?.replaceURLSpans(urlClickListener)

fun Spannable.replaceURLSpans(urlClickListener: UrlClickListener) =
    SpannableStringBuilder(this).also { result ->
        result.clearSpans()

        getSpans(0, length, URLSpan::class.java).forEach { urlSpan ->
            result.setSpan(
                InterceptedURLSpan(urlSpan.url, urlClickListener),
                getSpanStart(urlSpan),
                getSpanEnd(urlSpan),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
    }