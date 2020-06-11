package de.deutschebahn.bahnhoflive.ui.station.parking;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.model.parking.ParkingFacility;

public class BahnparkSiteDetailsFragment extends BottomSheetDialogFragment {


    public enum Action {
        INFO(
                R.string.parking_details_title_info,
                R.string.parking_details_button_reservation,
                context -> new DescriptionRenderer.Companion.DetailedDescriptionRenderer(context),
                new ButtonClickListener() {
                    @Override
                    public void onButtonClick(Context context, ParkingFacility parkingFacility) {
                        ExternalLinks.openReservation(context);
                    }
                }),
        PRICE(
                R.string.parking_details_title_price,
                R.string.parking_details_button_subscribe,
                context -> new DescriptionRenderer.Companion.PriceDescriptionRenderer(context),
                new ButtonClickListener() {
                    @Override
                    public void onButtonClick(Context context, ParkingFacility parkingFacility) {
                        ExternalLinks.openMonatskartekMail(context, parkingFacility);
                    }
                });

        @StringRes
        final int title;

        @StringRes
        final int buttonLabel;
        final DescriptionRendererFactory descriptionRendererFactory;
        final ButtonClickListener buttonClickListener;

        Action(int title, int buttonLabel, DescriptionRendererFactory descriptionRendererFactory, ButtonClickListener buttonClickListener) {
            this.title = title;
            this.buttonLabel = buttonLabel;
            this.descriptionRendererFactory = descriptionRendererFactory;
            this.buttonClickListener = buttonClickListener;
        }

        interface DescriptionRendererFactory {
            DescriptionRenderer create(Context context);
        }
    }

    public static final String ARG_PARKING_FACILITY = "parkingFacility";
    private static final String ARG_ACTION = "action";

    private Action action;

    private ParkingFacility parkingFacility;

    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);

        if (args == null) {
            dismissAllowingStateLoss();
            return;
        }

        parkingFacility = args.getParcelable(ARG_PARKING_FACILITY);
        action = Action.values()[args.getInt(ARG_ACTION)];
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_bahnpark_site_details, container, false);

        final TextView titleView = view.findViewById(R.id.title);
        titleView.setText(action.title);

        view.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final View footerView = view.findViewById(R.id.footer);

        if (true /*FIXME determine reservation availability */) {
            footerView.setVisibility(View.GONE);
        } else {
            footerView.setVisibility(View.VISIBLE);

            final View buttonView = footerView.findViewById(R.id.button);
            buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    action.buttonClickListener.onButtonClick(v.getContext(), parkingFacility);
                }
            });

            final TextView buttonLabel = buttonView.findViewById(R.id.button_label);
            buttonLabel.setText(action.buttonLabel);
        }

        final TextView contentTextView = view.findViewById(R.id.content_text);
        contentTextView.setText(action.descriptionRendererFactory.create(getContext()).render(parkingFacility));

        return view;
    }

    public static BahnparkSiteDetailsFragment create(Action action, ParkingFacility parkingFacility) {
        final BahnparkSiteDetailsFragment bahnparkSiteDetailsFragment = new BahnparkSiteDetailsFragment();

        final Bundle args = new Bundle();
        args.putParcelable(ARG_PARKING_FACILITY, parkingFacility);
        args.putInt(ARG_ACTION, action.ordinal());

        bahnparkSiteDetailsFragment.setArguments(args);

        return bahnparkSiteDetailsFragment;
    }
}
