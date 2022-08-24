/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.deutschebahn.bahnhoflive.BaseApplication;
import de.deutschebahn.bahnhoflive.IconMapper;
import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.analytics.TrackingManager;
import de.deutschebahn.bahnhoflive.backend.db.ris.model.LocalService;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContent;
import de.deutschebahn.bahnhoflive.backend.local.model.ServiceContentType;
import de.deutschebahn.bahnhoflive.ui.station.HistoryFragment;
import de.deutschebahn.bahnhoflive.ui.station.ServiceContents;
import de.deutschebahn.bahnhoflive.ui.station.StationViewModel;
import de.deutschebahn.bahnhoflive.ui.station.info.DbActionButton;
import de.deutschebahn.bahnhoflive.ui.station.info.DbActionButtonParser;
import de.deutschebahn.bahnhoflive.ui.station.info.StaticInfoDescriptionPart;
import de.deutschebahn.bahnhoflive.util.ImageHelper;
import de.deutschebahn.bahnhoflive.util.TextUtil;
import de.deutschebahn.bahnhoflive.util.TextViewImageGetter;

public class ServiceContentFragment extends Fragment {

    public static final String ARG_TITLE = FragmentArgs.TITLE;
    public static final String ARG_CONTENT = FragmentArgs.CONTENT;
    public static final String ARG_FORCE_LIST = FragmentArgs.FORCE_LIST;
    private String actionbarTitle;
    private int imageTargetWidth;

    private View detailsContainerView;

    private ServiceContent serviceContent;

    private int serviceDetailsFontSize = R.dimen.service_textsize;

    private boolean forceList;
    private Activity activity;
    private StationViewModel stationViewModel;
    private ImageView iconView;
    private LinearLayout descriptionContainerView;
    private TextView additionalTextView;
    private LinearLayout tableView;

    public String getActionBarTitle() {
        return actionbarTitle;
    }

    public boolean isShowingActionBar() {
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stationViewModel = new ViewModelProvider(getActivity()).get(StationViewModel.class);

        stationViewModel.getSelectedServiceContentType().observe(this, s -> {
            if (s != null) {
                if (s.equals(serviceContent.getType())) {
                    stationViewModel.getSelectedServiceContentType().setValue(null);
                } else {
                    HistoryFragment.parentOf(this).pop();
                }
            }
        });

        if (ServiceContentType.DB_INFORMATION.equals(serviceContent.getType()) && serviceContent.getAdditionalText() == null) {
            stationViewModel.getRisServiceAndCategoryResource().getData().observe(this, risServicesAndCategory -> {
                if (risServicesAndCategory == null) {
                    return;
                }
                final LocalService informationCounterService = risServicesAndCategory.getLocalServices().get(LocalService.Type.INFORMATION_COUNTER);
                if (informationCounterService == null) {
                    return;
                }

                bindViews();
            });

        }
    }

    @Override
    public void onDetach() {
        this.activity = null;
        super.onDetach();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        setUIArguments(args);
    }

    public void setUIArguments(Bundle args) {
        actionbarTitle = args.getString(ARG_TITLE);

        serviceContent = args.getParcelable(ARG_CONTENT);

        forceList = args.getBoolean(ARG_FORCE_LIST);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ARG_CONTENT, serviceContent);
        outState.putString(ARG_TITLE, actionbarTitle);
        outState.putBoolean(ARG_FORCE_LIST, forceList);

        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_servicecontent, container, false);

        if (savedInstanceState != null) {
            setUIArguments(savedInstanceState);
        }

        new ToolbarViewHolder(v, getActionBarTitle());

        detailsContainerView = v.findViewById(R.id.services_details_container);

        imageTargetWidth = ImageHelper.getImagewidthTarget(getActivity());

        detailsContainerView.setVisibility(View.VISIBLE);

        iconView = v.findViewById(R.id.service_icon);
        descriptionContainerView = v.findViewById(R.id.service_description);
        additionalTextView = v.findViewById(R.id.service_additionalText);
        additionalTextView.setMovementMethod(LinkMovementMethod.getInstance());
        additionalTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(serviceDetailsFontSize));
        tableView = v.findViewById(R.id.service_table);

        bindViews();

        return v;
    }

    public void bindViews() {
        actionbarTitle = serviceContent.getTitle();

        iconView.setImageResource(IconMapper.contentIconForType(serviceContent));


        if (serviceContent.getDescriptionText() != null && serviceContent.getDescriptionText().length() > 0) {
            final List<StaticInfoDescriptionPart> staticInfoDescriptionParts = new DbActionButtonParser().parse(serviceContent.getDescriptionText());
            if (!staticInfoDescriptionParts.isEmpty()) {
                descriptionContainerView.removeAllViews();
                descriptionContainerView.setVisibility(View.VISIBLE);

                for (StaticInfoDescriptionPart staticInfoDescriptionPart : staticInfoDescriptionParts) {

                    if (staticInfoDescriptionPart.getText() != null) {
                        String shrinkingDescription = staticInfoDescriptionPart.getText();
                        boolean keepLooking = true;

                        ArrayList<String> components;
                        if (serviceContent.getType().equals("3-s-zentrale")) {
                            components = ServiceContents.parseDreiSComponents(serviceContent.getDescriptionText());

                            String firstPart = components.get(0);
                            MBTextView descriptionPartTextView = new MBTextView(activity);
                            descriptionPartTextView.setTextIsSelectable(true);
                            descriptionPartTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                    getResources().getDimensionPixelSize(serviceDetailsFontSize));
                            descriptionPartTextView.setLinkTextColor(getResources().getColor(R.color.textcolor_light));
                            descriptionPartTextView.setMovementMethod(LinkMovementMethod.getInstance());
                            descriptionPartTextView.setText(Html.fromHtml(firstPart));

                            TextUtil.linkifyHtml(descriptionPartTextView, firstPart,
                                    Linkify.WEB_URLS,
                                    new TextViewImageGetter(descriptionPartTextView, imageTargetWidth));

                            descriptionContainerView.addView(descriptionPartTextView);
                            descriptionPartTextView.getLayoutParams().height = LayoutParams.WRAP_CONTENT;

                            makePhoneButton(components.get(1), descriptionContainerView);


                        } else {
                            while (keepLooking) {
                                //this pattern does not know if we are in or outside of a tag
                                // Pattern p = Pattern.compile("[0123456789\\- ]{5,}");

                                Pattern p = Pattern.compile("(>|\\s)[\\d]{3,}/?([^\\D]|\\s)+[\\d]");
                                final Matcher m = p.matcher(shrinkingDescription);

                                MBTextView descriptionPartTextView = new MBTextView(activity);
                                descriptionPartTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                        getResources().getDimensionPixelSize(serviceDetailsFontSize));
                                descriptionPartTextView.setLinkTextColor(getResources().getColor(R.color.textcolor_light));
                                descriptionPartTextView.setMovementMethod(LinkMovementMethod.getInstance());

                                if (m.find()) {
                                    final String phoneNumber = m.group().substring(1);

                                    int phoneNumberPosition = shrinkingDescription.indexOf(phoneNumber);
                                    String firstPart = shrinkingDescription.substring(0, phoneNumberPosition) + "</p>";
                                    shrinkingDescription = "<p>" + shrinkingDescription.substring(
                                            phoneNumberPosition + phoneNumber.length());

                                    descriptionPartTextView.setText(Html.fromHtml(firstPart));

                                    descriptionContainerView.addView(descriptionPartTextView);

                                    if (serviceContent.getType().equals("wlan")) {
                                        makeSettingsButton("WLAN Einstellungen", descriptionContainerView);
                                    }
                                    makePhoneButton(phoneNumber, descriptionContainerView);
                                } else {
                                    keepLooking = false;

                                    TextUtil.linkifyHtml(descriptionPartTextView,
                                            shrinkingDescription, Linkify.WEB_URLS,
                                            new TextViewImageGetter(descriptionPartTextView, imageTargetWidth));

                                    descriptionContainerView.addView(descriptionPartTextView);
                                }
                            }
                        }
                    } else {
                        final DbActionButton button = staticInfoDescriptionPart.getButton();
                        if (button != null) {
                            final String label = button.getLabel();
                            if (label != null) {
                                final TextView buttonView = (TextView) getLayoutInflater().inflate(R.layout.include_description_button_part, descriptionContainerView, false);
                                descriptionContainerView.addView(buttonView);
                                buttonView.setText(label);
                                buttonView.setContentDescription(label);
                                buttonView.setOnClickListener(v1 -> {
                                    final String href = button.getData();
                                    if (href != null) {
                                        try {
                                            v1.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(href)));
                                        } catch (Exception e) {
                                            BaseApplication.get().getIssueTracker().dispatchThrowable(e, null);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            } else {
                descriptionContainerView.removeAllViews();
                descriptionContainerView.setVisibility(View.GONE);
            }
        }

        if (serviceContent.getAdditionalText() != null && serviceContent.getAdditionalText().length() > 0) {
            additionalTextView.setVisibility(View.VISIBLE);
            additionalTextView.setMovementMethod(LinkMovementMethod.getInstance());
            TextUtil.linkifyHtml(additionalTextView,
                    serviceContent.getAdditionalText(), Linkify.ALL,
                    new TextViewImageGetter(additionalTextView, imageTargetWidth));

            additionalTextView.setLinkTextColor(getResources().getColor(R.color.textcolor_light));
        } else {
            additionalTextView.setVisibility(View.GONE);
            additionalTextView.setText(null);
        }

        tableView.removeAllViews();
//        if (serviceContent.getTable() != null) {
//            buildTable(serviceContent, tableView);
//            tableView.setVisibility(View.VISIBLE);
//        } else {
        tableView.setVisibility(View.GONE);
//        }
    }

    private TextView makeSettingsButton(final String buttonTitle, LinearLayout container) {
        TextView phoneText = new TextView(activity);
        phoneText.setTextColor(Color.WHITE);
        phoneText.setText(buttonTitle);
        phoneText.setGravity(Gravity.CENTER);
        phoneText.setBackgroundResource(R.drawable.shape_round_button_neutral);
        phoneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(new Intent(Settings.ACTION_WIFI_SETTINGS));
                activity.startActivity(intent);
            }
        });
        phoneText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.textsize_32));

        container.addView(phoneText);
        LayoutParams params = (LayoutParams) phoneText.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.content_phonetextHeight);

        if (getResources().getBoolean(R.bool.isTablet)) {
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.width = getResources().getDimensionPixelOffset(R.dimen.content_phoneButtonWidth_Tablet);
        }
        params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.content_phontext_marginBottom));

        return phoneText;
    }

    private TextView makePhoneButton(final String phoneNumber, LinearLayout container) {

        TextView phoneText = new TextView(activity);
        phoneText.setTextColor(Color.WHITE);
        phoneText.setText(phoneNumber);
        phoneText.setGravity(Gravity.CENTER);
        phoneText.setBackgroundResource(R.drawable.shape_round_button_neutral);
        phoneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel://" + phoneNumber.trim()));
                activity.startActivity(intent);
            }
        });
        phoneText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.textsize_32));

        container.addView(phoneText);
        LayoutParams params = (LayoutParams) phoneText.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.round_button_size_large);

        if (getResources().getBoolean(R.bool.isTablet)) {
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.width = getResources().getDimensionPixelOffset(R.dimen.content_phoneButtonWidth_Tablet);
        }
        params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.content_phontext_marginBottom));

        return phoneText;
    }

    private final static String _headlines = "headlines";
    private final static String _rows = "rows";
    private final static String _rowItems = "rowItems";
    private final static String _key = "key";
    private final static String _title = "headline"; //breaking api naming convention to avoid confusion re headline/headlines
    private final static String _content = "content";

//    private void buildTablePhone(ServiceContent item, LinearLayout table) {
//        try {
//            JSONArray columns = item.getTable().getJSONArray(_headlines);
//            JSONArray rows = item.getTable().getJSONArray(_rows);
//            if (rows != null && columns != null && activity != null) {
//                //each row is a block of content
//                for (int row = 0; row < rows.length(); row++) {
//                    JSONObject rowItems = rows.getJSONObject(row);
//                    if (rowItems != null) {
//                        JSONArray rowItemArray = rowItems.getJSONArray(_rowItems);
//                        if (rowItemArray != null) {
//                            buildTableEntry(rowItemArray, columns, table, row != 0);
//                        }
//                    }
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

//    private void buildTable(ServiceContent item, LinearLayout table) {
//        if (getResources().getBoolean(R.bool.isTablet)) {
//            buildTableTablet(item, table);
//        } else {
//            buildTablePhone(item, table);
//        }
//    }

//    private void buildTableTablet(ServiceContent item, LinearLayout table) {
//        try {
//            JSONArray columns = item.getTable().getJSONArray(_headlines);
//            JSONArray rows = item.getTable().getJSONArray(_rows);
//            if (rows != null && columns != null && activity != null) {
//
//                //each row is a block of content. three blocks are grouped horizontally
//
//                for (int row = 0; row < rows.length(); row++) {
//                    JSONObject rowItems = rows.getJSONObject(row);
//                    if (rowItems != null) {
//                        if (row != 0) {
//                            drawDivider(table);
//                        }
//                        JSONArray rowItemArray = rowItems.getJSONArray(_rowItems);
//                        if (rowItemArray != null) {
//                            buildTableEntryTablet(rowItemArray, columns, table);
//                        }
//                    }
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void drawDivider(LinearLayout table) {
        ImageView divider = new ImageView(activity);
        divider.setBackgroundColor(getResources().getColor(R.color.textcolor_light));
        table.addView(divider);
        LinearLayout.LayoutParams params = (LayoutParams) divider.getLayoutParams();
        params.height = 1;
        params.topMargin = getResources().getDimensionPixelOffset(R.dimen.service_table_contentPaddingTop);
        divider.setLayoutParams(params);
    }

    private LinearLayout getItemRow(LinearLayout table) {
        return (LinearLayout) LayoutInflater.from(activity).inflate(
                R.layout.item_service_tripleitemrow, table, false);
    }

    private void buildTableEntryTablet(JSONArray rowItemArray, JSONArray columns, LinearLayout table) throws JSONException {
        int padding = getResources().getDimensionPixelSize(R.dimen.service_paddingH);

        LinearLayout triple = getItemRow(table);

        //content. columns are relevant for content order
        for (int col = 0; col < columns.length(); col++) {
            for (int item = 0; item < rowItemArray.length(); item++) {
                JSONObject rowItem = rowItemArray.getJSONObject(item);
                if (rowItem == null) {
                    continue;
                }
                if (columns.getString(col).equals(rowItem.getString(_key))) {
                    if (!rowItem.isNull(_title)) {
                        MBTextView title = new MBTextView(activity);
                        title.setText(rowItem.getString(_title));
                        title.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                getResources().getDimensionPixelSize(serviceDetailsFontSize));
                        title.setTypeface(title.getTypeface(), Typeface.BOLD);
                        title.setPadding(0,
                                getResources().getDimensionPixelOffset(R.dimen.service_table_contentPaddingTop),
                                padding, 0);
                        getCell(col, triple).addView(title);
                    }
                    if (!rowItem.isNull(_content)) {
                        MBTextView contentLine = new MBTextView(activity);
                        contentLine.setTextIsSelectable(true);
                        contentLine.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                getResources().getDimensionPixelSize(serviceDetailsFontSize));
                        contentLine.setText(Html.fromHtml(rowItem.getString(_content)));
                        TextUtil.linkifyTel(contentLine);
                        contentLine.setMovementMethod(LinkMovementMethod.getInstance());
                        contentLine.setPadding(0, 0, padding, 0);
                        contentLine.setLinkTextColor(getResources().getColor(R.color.textcolor_light));
                        getCell(col, triple).addView(contentLine);
                    }
                }
            }
            if (col % 3 == 2 || col == columns.length() - 1) {
                table.addView(triple);
                triple = getItemRow(table);
            }
        }
    }

    private LinearLayout getCell(int item, LinearLayout triple) {
        switch (item % 3) {
            case 0:
                return (LinearLayout) triple.findViewById(R.id.service_triple_1);
            case 1:
                return (LinearLayout) triple.findViewById(R.id.service_triple_2);
            default:
                return (LinearLayout) triple.findViewById(R.id.service_triple_3);
        }
    }


    private void buildTableEntry(JSONArray rowItemArray, JSONArray columns, LinearLayout table, boolean drawDivider) throws JSONException {
        //divider
        if (drawDivider) {
            ImageView divider = new ImageView(activity);
            divider.setBackgroundColor(getResources().getColor(R.color.textcolor_light));
            table.addView(divider);
            LinearLayout.LayoutParams params = (LayoutParams) divider.getLayoutParams();
            params.height = 1;
            params.topMargin = getResources().getDimensionPixelOffset(R.dimen.service_table_contentPaddingTop);
            divider.setLayoutParams(params);
        }

        int padding = getResources().getDimensionPixelSize(R.dimen.service_paddingH);

        //content. columns are relevant for content order
        for (int col = 0; col < columns.length(); col++) {
            for (int item = 0; item < rowItemArray.length(); item++) {
                JSONObject rowItem = rowItemArray.getJSONObject(item);
                if (rowItem == null) {
                    continue;
                }
                if (columns.getString(col).equals(rowItem.getString(_key))) {
                    if (!rowItem.isNull(_title)) {
                        MBTextView title = new MBTextView(activity);
                        title.setText(rowItem.getString(_title));
                        title.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                getResources().getDimensionPixelSize(serviceDetailsFontSize));
                        title.setTypeface(title.getTypeface(), Typeface.BOLD);
                        title.setPadding(padding,
                                getResources().getDimensionPixelOffset(R.dimen.service_table_contentPaddingTop),
                                padding, 0);
                        table.addView(title);
                    }
                    if (!rowItem.isNull(_content)) {
                        MBTextView contentLine = new MBTextView(activity);
                        contentLine.setTextIsSelectable(true);
                        contentLine.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                getResources().getDimensionPixelSize(serviceDetailsFontSize));
                        contentLine.setText(Html.fromHtml(rowItem.getString(_content)));
                        TextUtil.linkifyTel(contentLine);
                        contentLine.setMovementMethod(LinkMovementMethod.getInstance());
                        contentLine.setPadding(padding, 0, padding, 0);
                        contentLine.setLinkTextColor(getResources().getColor(R.color.textcolor_light));
                        table.addView(contentLine);
                    }
                }
            }
        }

    }

    public static ServiceContentFragment create(String title, ServiceContent serviceContent, String trackingTag) {
        final Bundle args = createArgs(title, serviceContent, trackingTag);

        return create(args);
    }

    @NonNull
    public static ServiceContentFragment create(Bundle args) {
        final ServiceContentFragment serviceContentFragment = new ServiceContentFragment();

        serviceContentFragment.setArguments(args);

        return serviceContentFragment;
    }

    public static Bundle createArgs(String title, ServiceContent serviceContent, String trackingTag) {
        final Bundle args = new Bundle();

        args.putString(FragmentArgs.TITLE, title);
        args.putParcelable(FragmentArgs.CONTENT, serviceContent);
        TrackingManager.putTrackingTag(args, trackingTag);

        return args;
    }

    @Override
    public void onStart() {
        super.onStart();

        TrackingManager.fromActivity(getActivity()).track(TrackingManager.TYPE_STATE, TrackingManager.Screen.D1, TrackingManager.tagFromArguments(getArguments()));
    }
}
