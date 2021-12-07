package de.deutschebahn.bahnhoflive.ui.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openAppInPlayStore(context: Context) {
    //        TrackingManager.trackActions(trackingManager, new String[]{TrackingManager.TRACK_KEY_FEEDBACK, "rating"});
    context.startActivity(context.createPlaystoreIntent())
}

fun Context.createPlaystoreIntent() = Intent(
    Intent.ACTION_VIEW, Uri.parse(
        "market://details?id=" + packageName.replace(".debug", "")
    )
)
