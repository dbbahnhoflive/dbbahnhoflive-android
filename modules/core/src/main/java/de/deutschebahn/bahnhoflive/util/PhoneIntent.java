/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import android.content.Intent;
import android.net.Uri;

public class PhoneIntent extends Intent {
    public PhoneIntent(String phoneNumber) {
        super(Intent.ACTION_DIAL, Uri.parse("tel://" + phoneNumber.trim()));
    }
}
