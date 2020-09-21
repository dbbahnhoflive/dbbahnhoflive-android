/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class TextUtil {
    public static Spannable linkifyHtml(TextView textView, String html, int linkifyMask, ImageGetter imageGetter) {

        Spanned text = Html.fromHtml(html, imageGetter, null);

        URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);

        SpannableString buffer = new SpannableString(text);

        Linkify.addLinks( textView, linkifyMask);

        for (URLSpan span : currentSpans) {
            int end = text.getSpanEnd(span);
            int start = text.getSpanStart(span);
            buffer.setSpan(span, start, end, 0);
        }
        textView.setText(buffer);
        return buffer;
    }

    public static void linkifyTel(TextView textView) {
        Pattern p = Pattern.compile("[0123456789 ]{6,}");
        Linkify.addLinks(textView, p, "tel://", Linkify.sPhoneNumberMatchFilter, Linkify.sPhoneNumberTransformFilter);
    }

    public static String fromStream(InputStream stream) throws IOException {
        InputStreamReader sr = new InputStreamReader(stream);
        BufferedReader br = new BufferedReader(sr);
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

}
