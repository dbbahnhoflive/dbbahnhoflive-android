package de.deutschebahn.bahnhoflive.view;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

public class FullBottomSheetDialogFragment extends AppCompatDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new FullBottomSheetDialog(getContext(), getTheme());
    }
}
