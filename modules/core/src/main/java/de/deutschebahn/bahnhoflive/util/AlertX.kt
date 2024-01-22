package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.graphics.Typeface
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import de.deutschebahn.bahnhoflive.R


class AlertX {

    companion object {

        enum class AlertDefaultButton {
            BUTTON_NODEFAULT, BUTTON_POSITIVE, BUTTON_NEGATIVE, BUTTON_NEUTRAL
        }


        // for java
        fun buttonNegative() : AlertDefaultButton = AlertDefaultButton.BUTTON_NEGATIVE
        @SuppressWarnings("unused")
        fun buttonPositive() : AlertDefaultButton = AlertDefaultButton.BUTTON_POSITIVE

//        @SuppressWarnings("unused")
        fun buttonNeutral() : AlertDefaultButton = AlertDefaultButton.BUTTON_NEUTRAL

        fun execAlert(
            context: Context,
            titleText:String,
            mainText:String,
            defaultButton : AlertDefaultButton=AlertDefaultButton.BUTTON_POSITIVE,
            buttonPositiveText:String="", // right
            buttonPositiveClicked: (() -> Unit)? = null,
            buttonNegativeText:String="", // left
            buttonNegativeClicked: (() -> Unit)? = null,
            buttonNeutralText:String="", // middle
            buttonNeutralClicked: (() -> Unit)? = null,
            checkboxText:String="",
            checkboxClicked: ((Boolean)->Unit)? = null
        ) {

            val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.App_Dialog_Theme)

            if(titleText.isNotEmpty())
                builder.setTitle(titleText)

            if(mainText.isNotEmpty())
                builder.setMessage(mainText)

            if (buttonPositiveText.isNotEmpty()) {
                builder.setPositiveButton(buttonPositiveText) { dialog, _ ->
                    buttonPositiveClicked?.invoke()
                    dialog.dismiss()
                }
            }

            if (buttonNegativeText.isNotEmpty()) {
                builder.setNeutralButton(buttonNegativeText) { dialog, _ ->
                    buttonNegativeClicked?.invoke()
                    dialog.dismiss()
                }
            }

            if (buttonNeutralText.isNotEmpty()) {
                builder.setNegativeButton(buttonNegativeText) { dialog, _ ->
                    buttonNeutralClicked?.invoke()
                    dialog.dismiss()
                }
            }


//            val checkBoxView = View.inflate(context, R.layout.test_alert_checkbox, null)
//            val checkBox = checkBoxView.findViewById<View>(R.id.checkbox) as CheckBox
//            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
//                // Save to shared preferences
//            }
//            checkBox.text = "Text to the right of the check box."

            if(checkboxText.isNotBlank() && checkboxClicked!=null) {
                val checkBoxView = CheckBox(context)
                checkBoxView.text = checkboxText
                checkBoxView.setOnCheckedChangeListener { _, isChecked ->
                    checkboxClicked(isChecked)
                }
                builder.setView(checkBoxView)
            }

            builder.setCancelable(false)

            val dialog: AlertDialog = builder.create()

            dialog.show()


            // alert_dialog.xml

//            val parentPanel = dialog.findViewById<LinearLayout>(androidx.appcompat.R.id.parentPanel)

//            dialog.findViewById<DialogTitle>(androidx.appcompat.R.id.alertTitle)?.gravity = Gravity.CENTER
//            dialog.findViewById<DialogTitle>(androidx.appcompat.R.id.alertTitle)?.setTypeface(null, Typeface.BOLD)

//            dialog.findViewById<TextView>(android.R.id.message)?.gravity = Gravity.CENTER

//            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isAllCaps=false
            try {
                when (defaultButton) {
                    AlertDefaultButton.BUTTON_POSITIVE ->
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            ?.setTypeface(null, Typeface.BOLD)

                    AlertDefaultButton.BUTTON_NEGATIVE -> dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        ?.setTypeface(null, Typeface.BOLD)

                    AlertDefaultButton.BUTTON_NEUTRAL -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                        ?.setTypeface(null, Typeface.BOLD)

                    else -> {}
                }
            }
            catch(_:Exception) {

            }
//            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)?.isAllCaps=false
//            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.isAllCaps=false

        }

    }
}

