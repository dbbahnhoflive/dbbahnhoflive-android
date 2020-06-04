package de.deutschebahn.bahnhoflive.util;

import android.content.Intent;
import android.net.Uri;

public class PhoneIntent extends Intent {
    public PhoneIntent(String phoneNumber) {
        super(Intent.ACTION_DIAL, Uri.parse("tel://" + phoneNumber.trim()));
    }
}
