package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.DialogTitle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import de.deutschebahn.bahnhoflive.R


class AlertX {

    companion object {

        fun execAlert(
            context: Context,
            titleText:String,
            mainText:String,
            buttonPositiveText:String="",
            buttonPositiveClicked: (() -> Unit)? = null,
            buttonNegativText:String="",
            buttonNegativeClicked: (() -> Unit)? = null,
            buttonNeutralText:String="",
            buttonNeutralClicked: (() -> Unit)? = null

        ) {

            val builder: AlertDialog.Builder = androidx.appcompat.app.AlertDialog.Builder(context)
//
//            val layout: View = View.inflate(context, androidx.appcompat.R.id.alertTitle, null)
//            builder.setView(layout)



            if(titleText.isNotEmpty())
                builder.setTitle(titleText)

            if(mainText.isNotEmpty())
                builder.setMessage(mainText)

            if (buttonPositiveText.isNotEmpty()) {
                builder.setPositiveButton(buttonPositiveText) { dialog, which ->
                    buttonPositiveClicked?.invoke()
                    dialog.dismiss()
                }
            }

            if (buttonNegativText.isNotEmpty()) {
                builder.setNegativeButton(buttonNegativText) { dialog, which ->
                    buttonNegativeClicked?.invoke()
                    dialog.dismiss()
                }
            }

            if (buttonNeutralText.isNotEmpty()) {
                builder.setNeutralButton(buttonNegativText) { dialog, which ->
                    buttonNeutralClicked?.invoke()
                    dialog.dismiss()
                }
            }


            builder.setCancelable(false)

            val dialog: AlertDialog = builder.create()

            dialog.show()


            // alert_dialog.xml



//            val parentPanel = dialog.findViewById<LinearLayout>(androidx.appcompat.R.id.parentPanel)

            dialog.findViewById<DialogTitle>(androidx.appcompat.R.id.alertTitle)?.gravity = Gravity.CENTER
            dialog.findViewById<DialogTitle>(androidx.appcompat.R.id.alertTitle)?.setTypeface(null, Typeface.BOLD)

            dialog.findViewById<TextView>(android.R.id.message)?.gravity = Gravity.CENTER

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isAllCaps=false
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.setTypeface(null, Typeface.BOLD)
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)?.isAllCaps=false
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)?.isAllCaps=false

        }

    }
}

