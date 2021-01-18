package de.deutschebahn.bahnhoflive.ui.consent

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import de.deutschebahn.bahnhoflive.R
import de.deutschebahn.bahnhoflive.analytics.ConsentState
import de.deutschebahn.bahnhoflive.analytics.TrackingManager
import de.deutschebahn.bahnhoflive.repository.AssetDocumentBroker
import de.deutschebahn.bahnhoflive.ui.WebViewActivity
import kotlinx.android.synthetic.main.fragment_consent.view.*

class ConsentFragment : DialogFragment() {

    val trackingManager = TrackingManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_consent, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val onClickListener: (v: View) -> Unit = {
            startActivity(
                WebViewActivity.createIntent(
                    context,
                    AssetDocumentBroker.FILE_NAME_PRIVACY_POLICY,
                    "Datenschutz"
                )
            )
        }
        view.consentCopy1.setOnClickListener(onClickListener)
        view.consentCopy2.setOnClickListener(onClickListener)

        view.buttonDissent.setOnClickListener {
            trackingManager.setConsented(false)
            close()
        }

        view.buttonConsent.setOnClickListener {
            trackingManager.setConsented(true)
            close()
        }

    }

    override fun onResume() {
        super.onResume()

        if (trackingManager.consentState != ConsentState.PENDING) {
            close()
        }
    }

    private fun close() {
        activity?.setResult(Activity.RESULT_OK)
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        activity?.finish()
    }
}