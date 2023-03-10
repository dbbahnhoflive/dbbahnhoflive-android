package de.deutschebahn.bahnhoflive.util

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView

// see https://stackoverflow.com/questions/12418279/android-textview-with-clickable-links-how-to-capture-clicks
/**
 *
 * Searches for all URLSpans in current text replaces them with our own ClickableSpans
 * forwards clicks to provided function.
 * usage:
 * ```
 *
 * < string name="link_string">this is my link: < a href="https://www.google.com/">CLICK</ a></ string>
 * ```
 * Make sure your spanned text is set to the TextView before you call "handleUrlClicks"
 * textView.text = getString(R.string.link_string)
 * The pattern matches the following:
 * ```
 *
 * textView.handleUrlClicks { url ->Log.d(TAG, "click on found span: $url") }
 * ```
 *
 */
fun TextView.handleUrlClicks(onClicked: ((String) -> Unit)? = null) {
    //create span builder and replaces current text with it
    text = SpannableStringBuilder.valueOf(text).apply {
        //search for all URL spans and replace all spans with our own clickable spans
        getSpans(0, length, URLSpan::class.java).forEach {
            //add new clickable span at the same position
            setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onClicked?.invoke(it.url)
                    }
                },
                getSpanStart(it),
                getSpanEnd(it),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            //remove old URLSpan
            removeSpan(it)
        }
    }
    //make sure movement method is set
    movementMethod = LinkMovementMethod.getInstance()
}

