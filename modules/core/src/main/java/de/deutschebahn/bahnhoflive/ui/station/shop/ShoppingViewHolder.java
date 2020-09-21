/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.station.shop;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.regex.Pattern;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.ui.station.CommonDetailsCardViewHolder;
import de.deutschebahn.bahnhoflive.ui.station.info.ThreeButtonsViewHolder;
import de.deutschebahn.bahnhoflive.view.SingleSelectionManager;

public class ShoppingViewHolder<T> extends CommonDetailsCardViewHolder<T> {
    public static final Pattern PHONE_PATTERN = Patterns.PHONE;

    protected final ThreeButtonsViewHolder threeButtonsViewHolder;
    protected final TextView locationView;
    protected final TextView hoursView;
    protected final ViewGroup paymentContainer;
    protected final TextView hoursTitleView;
    protected final TextView paymentTextView;

    protected String phoneString;
    protected String webString;
    protected String emailString;

    public ShoppingViewHolder(ViewGroup parent, int layout, SingleSelectionManager singleSelectionManager) {
        super(parent, layout, singleSelectionManager);
        paymentContainer = itemView.findViewById(R.id.shopschlemmDetails_paymentIcons);
        paymentTextView = itemView.findViewById(R.id.shopschlemmDetails_paymentTextlist);
        hoursTitleView = findTextView(R.id.hours_title);
        hoursView = findTextView(R.id.hours);
        locationView = findTextView(R.id.location);

        threeButtonsViewHolder = new ThreeButtonsViewHolder(itemView, R.id.buttons_container, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.button_left) {
                    if (!webString.startsWith("http")) {
                        webString = "http://" + webString;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webString));
                    getContext().startActivity(intent);
                } else if (id == R.id.button_middle) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", emailString, null));
                    getContext().startActivity(Intent.createChooser(emailIntent, "Email schreiben:"));
                } else if (id == R.id.button_right) {
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                    phoneIntent.setData(Uri.fromParts("tel", phoneString, null));
                    getContext().startActivity(phoneIntent);
                }
            }
        });

    }

    public Context getContext() {
        return itemView.getContext();
    }
}
