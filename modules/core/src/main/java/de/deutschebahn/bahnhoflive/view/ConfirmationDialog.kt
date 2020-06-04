package de.deutschebahn.bahnhoflive.view

import android.view.View
import com.google.android.material.snackbar.Snackbar
import de.deutschebahn.bahnhoflive.R

class ConfirmationDialog(targetView: View, message: String, confirmedAction: View.OnClickListener) {
    init {
        val snackbar = Snackbar.make(targetView, message, Snackbar.LENGTH_SHORT)
        snackbar.setAction("Ja", confirmedAction)
        snackbar.view.setBackgroundColor(snackbar.context.resources.getColor(R.color.white))
        snackbar.show()
    }
}