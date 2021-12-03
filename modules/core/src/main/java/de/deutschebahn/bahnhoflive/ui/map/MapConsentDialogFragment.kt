package de.deutschebahn.bahnhoflive.ui.map

import android.content.DialogInterface
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.deutschebahn.bahnhoflive.databinding.DialogMapConsentBinding

class MapConsentDialogFragment : BottomSheetDialogFragment() {

    val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogMapConsentBinding.inflate(inflater, container, false).apply {

        providerPrivacyPolicyLink.movementMethod = LinkMovementMethod.getInstance()
        privacyPolicyLink.movementMethod = LinkMovementMethod.getInstance()

        btnAccept.setOnClickListener {
            mapViewModel.mapConsentedLiveData.value = true
            dismiss()
        }
    }.root

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        activity?.finish()
    }
}