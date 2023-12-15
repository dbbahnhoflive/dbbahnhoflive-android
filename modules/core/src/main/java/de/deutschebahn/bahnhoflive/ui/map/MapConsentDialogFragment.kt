package de.deutschebahn.bahnhoflive.ui.map

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import de.deutschebahn.bahnhoflive.databinding.DialogMapConsentBinding
import de.deutschebahn.bahnhoflive.repository.AssetDocumentBroker

interface OnMapConsentDialogListener {
    fun onConsentAccepted()
}

class MapConsentDialogFragment : DialogFragment() {

    private val mapViewModel: MapViewModel by activityViewModels()

    private var listener: OnMapConsentDialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogMapConsentBinding.inflate(inflater, container, false).apply {

        providerPrivacyPolicyLink.setOnClickListener("https://policies.google.com/privacy?hl=de".createOnClickListener())
        privacyPolicyLink.setOnClickListener(activity?.let { activity ->
            AssetDocumentBroker(activity).takeIf { it.hasLegalNotice }?.run {
                View.OnClickListener {
                    showDocument(getCurrentPrivacyPolicy())
                }
            }
        }
            ?: "https://www.bahnhof.de/bahnhof-de/datenschutzhinweis_db_bahnhof_live-2887724".createOnClickListener())

        acceptButton.setOnClickListener {
            onAccept()
        }

        cancelButton.setOnClickListener {
            dialog?.cancel()
        }

    }.root

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
//        AlertDialog.Builder(requireContext(), theme)
//            .setPositiveButton(R.string.map_accept) { dialog, which ->
//                onAccept()
//            }.setNegativeButton(R.string.dlg_cancel) { dialog, which ->
//                dialog.cancel()
//            }
//            .create()

    private fun String.createOnClickListener(): View.OnClickListener = View.OnClickListener {
        startActivity(Intent(Intent.ACTION_VIEW, toUri()))
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

//        activity?.finish()
    }

    private fun onAccept() {
        mapViewModel.mapConsentedLiveData.value = true

        listener?.onConsentAccepted()

        dismiss()
    }

    override fun onPause() {

        dismiss()
        super.onPause()
    }

    fun setOnMapConsentDialogListener(_listener: OnMapConsentDialogListener) : MapConsentDialogFragment {
        listener = _listener
        return this
    }

}