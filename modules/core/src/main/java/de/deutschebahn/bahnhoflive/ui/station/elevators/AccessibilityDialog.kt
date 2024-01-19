package de.deutschebahn.bahnhoflive.ui.station.elevators

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.deutschebahn.bahnhoflive.R


class AccessibilityDialog {

    companion object {

        fun execDialog(
            context: Context,
            titleText:String,
            mainText:String,
            buttonOption1Text:String="",
            buttonOption2Text:String="",
            buttonOption1Clicked: (() -> Unit)? = null,
            buttonOption2Clicked: (() -> Unit)? = null,
            buttonPositiveText:String="",
            buttonPositiveClicked: (() -> Unit)? = null

        ) {

            val builder: AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(context, R.style.App_Dialog_Theme)


            val layout: View = View.inflate(context, R.layout.dialog_accessibility_elevator, null)
            builder.setView(layout)

            val title : TextView = layout.findViewById(R.id.title)
            val mainTextView : TextView = layout.findViewById(R.id.main_text)
            val buttonOption1 : Button = layout.findViewById(R.id.buttonOption1)
            val buttonOption2 : Button = layout.findViewById(R.id.buttonOption2)

            title.text = titleText

            mainTextView.text = mainText



            builder.setNeutralButton(context.getText(R.string.dlg_cancel)) { _, _ ->
            }

            if (buttonPositiveText.isNotEmpty()) {
                builder.setPositiveButton(buttonPositiveText) { dialog, _ ->
                    buttonPositiveClicked?.invoke()
                    dialog.dismiss()
                }
            }

            val dialog: AlertDialog = builder.create()

            if(buttonOption1Text.isNotEmpty()) {
                buttonOption1.text = buttonOption1Text
                buttonOption1.setOnClickListener {
                    buttonOption1Clicked?.invoke()
                    dialog.dismiss()
                }
            }
            else
                buttonOption1.visibility = View.GONE

            if(buttonOption2Text.isNotEmpty()) {
                buttonOption2.text = buttonOption2Text
                buttonOption2.setOnClickListener {
                    buttonOption2Clicked?.invoke()
                    dialog.dismiss()
                }

            }
            else
                buttonOption2.visibility = View.GONE

            dialog.show()

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)?.isAllCaps=false
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)?.setTypeface(null, Typeface.BOLD)
//            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)?.importantForAccessibility=IMPORTANT_FOR_ACCESSIBILITY_NO

            title.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            title.requestFocus()
        }

    }
}