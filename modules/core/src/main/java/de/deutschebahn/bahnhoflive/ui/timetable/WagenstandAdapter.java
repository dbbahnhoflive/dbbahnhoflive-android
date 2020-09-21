/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.ui.timetable;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.FeatureStatus;
import de.deutschebahn.bahnhoflive.backend.wagenstand.models.Status;
import de.deutschebahn.bahnhoflive.repository.trainformation.LegacyFeature;
import de.deutschebahn.bahnhoflive.repository.trainformation.Train;
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation;
import de.deutschebahn.bahnhoflive.repository.trainformation.Waggon;

public class WagenstandAdapter extends BaseAdapter {

    public static final String TAG = WagenstandAdapter.class.getSimpleName();
    private Context context;

    private final TrainFormation trainFormation;

    public WagenstandAdapter(Context context, TrainFormation trainFormation) {
        this.trainFormation = trainFormation;
        this.context = context;
    }

    @Override
    public int getCount() {
        return trainFormation.getWaggonCount() + 1;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public Waggon getItem(int position) {
        if (position < trainFormation.getWaggonCount()) {
            return trainFormation.getWaggons().get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Resources r = context.getResources();

        final int waggonCount = trainFormation.getWaggonCount();
        if (position >= waggonCount) {

            Waggon lastWaggon = getItem(waggonCount - 1);

            int heightOfLastWaggon = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, r.getDisplayMetrics());
            if (lastWaggon.isWaggon() && lastWaggon.getLength() > 1.0) {
                heightOfLastWaggon = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, r.getDisplayMetrics());
            }

            View spacer = new View(context);
            int heightOfListView = parent.getLayoutParams().height;
            spacer.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, heightOfListView - heightOfLastWaggon));
            return spacer;
        }

        Waggon waggon = getItem(position);

        if (waggon == null) {
            return new View(context);
        }

        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        holder = new ViewHolder();

        if (waggon.isHead()) {
            convertView = layoutInflater.inflate(trainFormation.isReversed() ? R.layout.item_train_tail : R.layout.item_train_head, parent, false);
            holder.trainHeadIcon = convertView.findViewById(R.id.train_head_icon);
            holder.trainHeadDestinationLabel = convertView.findViewById(R.id.head_different_destination_label);
            holder.trainHeadTypeLabel = convertView.findViewById(R.id.head_train_type_label);
            holder.trainHeadLabelContainer = convertView.findViewById(R.id.head_label_container);
            convertView.setTag(holder);
        } else if (waggon.isTail()) {
            convertView = layoutInflater.inflate(trainFormation.isReversed() ? R.layout.item_train_head : R.layout.item_train_tail, parent, false);
            convertView.setTag(holder);
        } else if (waggon.isTrainHeadBothWays()) {
            convertView = layoutInflater.inflate(R.layout.item_train_both, parent, false);
            holder.trainHeadIcon = convertView.findViewById(R.id.train_head_icon);
            holder.trainHeadDestinationLabel = convertView.findViewById(R.id.head_different_destination_label);
            holder.trainHeadTypeLabel = convertView.findViewById(R.id.head_train_type_label);
            holder.trainHeadLabelContainer = convertView.findViewById(R.id.head_label_container);
        } else {

            if (waggon.getLength() == 1) {
                convertView = layoutInflater.inflate(R.layout.item_train_waggon_half, parent, false);
            } else {
                convertView = layoutInflater.inflate(R.layout.item_train_waggon, parent, false);
            }
            holder.secondWaggonPart = convertView.findViewById(R.id.second_waggon_part);
            holder.classColorContainer = convertView.findViewById(R.id.waggon_class_color_view);
            holder.waggonNumberLabel = convertView.findViewById(R.id.waggon_number_label);
            holder.waggonClassLabel = convertView.findViewById(R.id.waggon_class_label);
            holder.symbolContainer = convertView.findViewById(R.id.waggon_symbol_container);
            holder.additionalInformationLabel = convertView.findViewById(R.id.waggon_additional_information_label);

            convertView.setTag(holder);
        }

        int fiveDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
        int twentyDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());


        final Train train = waggon.getTrain();
        boolean lastPosition = position == trainFormation.getWaggonCount() - 1;
        boolean firstPosition = position == 0;

        if (train != null && holder.trainHeadTypeLabel != null && holder.trainHeadDestinationLabel != null) {
            holder.trainHeadTypeLabel.setText("");
            holder.trainHeadDestinationLabel.setText("");

            if (firstPosition || lastPosition) {
                if (train.getDestinationStation().length() > 0 && (waggon.isHead() || waggon.isTrainHeadBothWays())) {
                    holder.trainHeadTypeLabel.setText(String.format("%s %s nach ", train.getType(), train.getNumber()));
                    holder.trainHeadDestinationLabel.setText(train.getDestinationStation());
                } else {
                    holder.trainHeadTypeLabel.setText(String.format("%s %s", train.getType(), train.getNumber()));
                }

                if (lastPosition && holder.trainHeadIcon != null) {
                    RelativeLayout.LayoutParams topLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    topLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    topLayoutParams.setMargins(twentyDP, fiveDP, 0, 0);
                    topLayoutParams.addRule(RelativeLayout.RIGHT_OF, holder.trainHeadIcon.getId());
                    if (holder.trainHeadLabelContainer != null) {
                        holder.trainHeadLabelContainer.setLayoutParams(topLayoutParams);
                    }
                    convertView.requestLayout();
                }
            }
        }

        if (holder.classColorContainer != null) {
            holder.classColorContainer.setBackgroundColor(waggon.getPrimaryColor());
            holder.classColorContainer.setContentDescription(context.getString(R.string.sr_template_waggon, waggon.getDisplayNumber(), renderClass(waggon.getClass()), renderSectionsString(waggon.getSections())));
            holder.waggonNumberLabel.setText(waggon.getDisplayNumber());
            holder.waggonNumberLabel.setContentDescription(" ");
            holder.waggonNumberLabel.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

            if (waggon.isMultiClass()) {
                holder.secondWaggonPart.setBackgroundColor(waggon.getSecondaryColor());
                holder.waggonClassLabel.setText("");
            } else {
                holder.waggonClassLabel.setText(waggon.getClass());
                holder.waggonClassLabel.setContentDescription(" ");
                holder.waggonClassLabel.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }

            final String differentDestination = waggon.getDifferentDestination();
            holder.additionalInformationLabel.setText(differentDestination);
            int visibility = differentDestination.length() > 0 ? View.VISIBLE : View.GONE;
            holder.additionalInformationLabel.setVisibility(visibility);

            holder.symbolContainer.removeAllViews();

            for (LegacyFeature legacyFeature : waggon.getLegacyFeatures()) {
                View symbolTagView = layoutInflater.inflate(R.layout.wagenstand_symbol_tag, holder.symbolContainer, false);
                TextView symbolLabel = symbolTagView.findViewById(R.id.symbol_label);
                TextView symbolDescription = symbolTagView.findViewById(R.id.symbol_description);

                symbolLabel.setText(legacyFeature.getSymbol());
                symbolDescription.setText(legacyFeature.getDescription());

                holder.symbolContainer.addView(symbolTagView);
            }

            final ArrayList<View> iconlessViews = new ArrayList<>();

            for (FeatureStatus feature : waggon.getFeatures()) {

                final int icon = feature.waggonFeature.icon;
                final Status status = feature.status;

                final CharSequence label = feature.waggonFeature.labelTemplate.composeLabel(context, feature.waggonFeature, status);

                if (label != null || icon != 0) {
                    final View symbolTagView = layoutInflater.inflate(R.layout.wagenstand_symbol_image, holder.symbolContainer, false);

                    final ImageView imageView = symbolTagView.findViewById(R.id.symbol_icon);
                    final TextView textView = symbolTagView.findViewById(R.id.symbol_description);

                    textView.setText(label);

                    if (icon == 0) {
                        imageView.setVisibility(View.GONE);

                        iconlessViews.add(symbolTagView);
                    } else {
                        imageView.setImageResource(icon);
                        imageView.setSelected(status.available);

                        holder.symbolContainer.addView(symbolTagView);
                    }

                }

            }

            for (View iconlessView : iconlessViews) {
                holder.symbolContainer.addView(iconlessView);
            }
        }

        return convertView;
    }

    private String renderClass(String classOfWaggon) {
        if ("2".equals(classOfWaggon)) {
            return context.getString(R.string.sr_class_2);
        }
        if ("1".equals(classOfWaggon)) {
            return context.getString(R.string.sr_class_1);
        }
        return context.getString(R.string.sr_template_class_other, classOfWaggon);
    }

    private String renderSectionsString(List<String> sections) {
        if (sections == null || sections.isEmpty()) {
            return "";
        }

        if (sections.size() == 1) {
            return context.getString(R.string.sr_template_single_section, sections.get(0));
        }

        return context.getString(R.string.sr_template_multi_section, sections.get(0), sections.get(sections.size() - 1));

    }

    private class ViewHolder {
        View classColorContainer;
        View secondWaggonPart;
        TextView waggonNumberLabel;
        TextView waggonClassLabel;
        LinearLayout symbolContainer;

        ImageView trainHeadIcon;
        RelativeLayout trainHeadLabelContainer;
        TextView trainHeadDestinationLabel;
        TextView trainHeadTypeLabel;
        TextView additionalInformationLabel;
    }
}
