package de.deutschebahn.bahnhoflive.util

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment

fun Activity.hideKeyboard() {

    val currentFocusedView = currentFocus

    if(currentFocusedView!=null) {
        (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    }
}

fun Fragment.hideKeyboard() {

    val currentFocusedView = requireActivity().currentFocus

    if(currentFocusedView!=null) {
        (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow((activity?.currentFocus ?: View(context)).windowToken, 0)
    }
}

fun EditText.hideKeyboard() {
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(windowToken, 0)
}

fun EditText.showKeyboard() {
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
        .showSoftInput(this, 0)
}
