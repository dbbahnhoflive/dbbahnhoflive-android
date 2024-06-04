package de.deutschebahn.bahnhoflive.util

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import de.deutschebahn.bahnhoflive.R


enum class MessageBoxButton(val value: Int) {
    BUTTON_POSITIVE(AlertDialog.BUTTON_POSITIVE),
    BUTTON_NEGATIVE(AlertDialog.BUTTON_NEGATIVE),
    BUTTON_NEUTRAL(AlertDialog.BUTTON_NEUTRAL)
}

enum class MessageBoxAlignment() {
    ALIGN_LEFT,
    ALIGN_CENTER,
    ALIGN_RIGHT
}

class MessageBox(
    private val context: Context, titleText: String, mainText: String
) {
    private var buttons = setOf<MessageBoxButton>()
    private var defaultButton : MessageBoxButton = MessageBoxButton.BUTTON_POSITIVE
    private var builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.App_Dialog_Theme)

    private var messageAlignment : MessageBoxAlignment = MessageBoxAlignment.ALIGN_CENTER
    private var titleAlignment : MessageBoxAlignment = MessageBoxAlignment.ALIGN_CENTER
    init {

        if(titleText.isNotEmpty())
            builder.setTitle(titleText)

        if(mainText.isNotEmpty())
            builder.setMessage(mainText)
    }

    //
    // Usage: findFirstEncounteredType(getDialog().getWindow().getDecorView(), TextView.class)
    //
    private fun findFirstEncounteredType(view: View?, klass: Class<*>): View? {
        if (klass.isInstance(view)) {
            return view
        } else {
            if (view !is ViewGroup) {
                return null
            }
        }

        val viewGroup = view

        var i = 0
        val ei = viewGroup.childCount
        while (i < ei) {
            val child = viewGroup.getChildAt(i)
            val result = findFirstEncounteredType(child, klass)
            if (result != null) {
                return result
            }
            i++
        }

        return null
    }

    fun show() {

        builder.setCancelable(false)

        if(buttons.isEmpty())
            addPositiveButton("OK", {})

        val dialog: AlertDialog = builder.create()

        dialog.setOnShowListener {
            try {
                when (defaultButton) {
                    MessageBoxButton.BUTTON_POSITIVE ->
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            ?.setTypeface(null, Typeface.BOLD)

                    MessageBoxButton.BUTTON_NEGATIVE -> dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        ?.setTypeface(null, Typeface.BOLD)

                    MessageBoxButton.BUTTON_NEUTRAL -> dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                        ?.setTypeface(null, Typeface.BOLD)

                    else -> {}
                }
            }
            catch(_:Exception) {

            }
        }

        dialog.show()

        // title (not working
        try {
            val dialogTitle: View? =
                findFirstEncounteredType(dialog.window?.decorView, TextView::class.java)

            dialogTitle?.let {
                val lp: LinearLayout.LayoutParams = it.layoutParams as LinearLayout.LayoutParams
                lp.gravity =  Gravity.CENTER_HORIZONTAL

//                it.textAlignment= when (titleAlignment) {
//                    MessageBoxAlignment.ALIGN_LEFT -> View.TEXT_ALIGNMENT_TEXT_START
//                    MessageBoxAlignment.ALIGN_CENTER -> View.TEXT_ALIGNMENT_CENTER
//                    MessageBoxAlignment.ALIGN_RIGHT -> View.TEXT_ALIGNMENT_TEXT_END
//                }

                lp.width = 0
                lp.weight = 1.0f
                it.layoutParams = lp
            }
        }
        catch(_:Exception) {

        }

        // message
        try {
            val messageText = dialog.findViewById<View>(android.R.id.message) as TextView?
            messageText?.gravity = when (messageAlignment) {
                MessageBoxAlignment.ALIGN_LEFT -> Gravity.START
                MessageBoxAlignment.ALIGN_CENTER -> Gravity.CENTER
                MessageBoxAlignment.ALIGN_RIGHT -> Gravity.END
            }
        }
        catch(_:Exception) {

        }


        // buttons
        try {
            if (buttons.size == 1) {

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.weight = 1.0f
                layoutParams.gravity = Gravity.CENTER // this is layout_gravity
                dialog.getButton(buttons.first().value).layoutParams = layoutParams
            }
        }
        catch(_:Exception) {

        }


    }

    private fun setDefaultButton(button:MessageBoxButton) : MessageBox {
        defaultButton = button
        return this
    }

    fun addPositiveButton(text: String, onClick: () -> Unit, isDefault:Boolean=true) : MessageBox {
        buttons+=MessageBoxButton.BUTTON_POSITIVE
        if(isDefault)
            setDefaultButton(MessageBoxButton.BUTTON_POSITIVE)
        builder.setPositiveButton(text) { dialog, _ ->
            onClick()
            dialog.dismiss()
        }
        return this
    }

    fun addNegativeButton(text: String, onClick: () -> Unit, isDefault:Boolean=false) : MessageBox {
        buttons+=MessageBoxButton.BUTTON_NEGATIVE
        if(isDefault)
            setDefaultButton(MessageBoxButton.BUTTON_NEGATIVE)
        builder.setNegativeButton(text) { dialog, _ ->
            onClick()
            dialog.dismiss()
        }
        return this
    }

    fun addNeutralButton(text: String, onClick: () -> Unit, isDefault:Boolean=false) : MessageBox {
        buttons+=MessageBoxButton.BUTTON_NEUTRAL
        if(isDefault)
            setDefaultButton(MessageBoxButton.BUTTON_NEUTRAL)
        builder.setNeutralButton(text) { dialog, _ ->
            onClick()
            dialog.dismiss()
        }
        return this
    }

    fun setMessageAlignment(alignment: MessageBoxAlignment) : MessageBox {
        messageAlignment = alignment
        return this
    }

    fun setTitleAlignment(alignment: MessageBoxAlignment) : MessageBox {
        titleAlignment = alignment
        return this
    }
}