package de.deutschebahn.bahnhoflive.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.repository.trainformation.TrainFormation;
import de.deutschebahn.bahnhoflive.repository.trainformation.Waggon;

public class WagenstandSectionIndicator extends LinearLayout implements View.OnClickListener{

    public interface WagenstandSectionIndicatorListener {
        void didSelectSection(String section);
        void didSelectTypeOfWagon(String section);
    }

    private WagenstandSectionIndicatorListener delegate;
    private Context mContext;

    private LinearLayout mSectionLabelContainer;
    private LinearLayout mTrainSymbolContainer;
    private LinearLayout mViewContainer;
    private String activeSection = "";

    private final HorizontalScrollView mScrollView;

    private ArrayList<TextView> sectionLabels = new ArrayList<>();

    public WagenstandSectionIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        View mSectionIndicatorContainer = LayoutInflater.from(context).inflate(R.layout.wagenstand_section_indicator, this, true);

        mViewContainer = mSectionIndicatorContainer.findViewById(R.id.section_label_container);
        mSectionLabelContainer = mViewContainer.findViewById(R.id.train_section_container);
        mTrainSymbolContainer = mViewContainer.findViewById(R.id.train_symbol_container);
        mScrollView = mSectionIndicatorContainer.findViewById(R.id.horizontal_scrollview);
    }

    public void centerContent() {
        int trainlength = mSectionLabelContainer.getWidth();
        int screenWidth = (getResources().getDisplayMetrics().widthPixels-trainlength) / 2;
        int defaultPadding = getResources().getDimensionPixelOffset(R.dimen.wagenstand_sectionlabel_padding);
        int padding = Math.max(screenWidth,defaultPadding);

        mViewContainer.setPadding(padding, 0, 0, 0);
    }


    public void setWagenstand(final TrainFormation trainFormation) {
        clearView();

        final List<Waggon> waggons = trainFormation.getWaggons();

        String currentSection = "";

        for (Waggon waggon : waggons) {

            final String lastSectionOfWaggon = waggon.getSections().get(waggon.getSections().size()-1);

            LayoutInflater inflater = LayoutInflater.from(mContext);

            final ViewGroup sectionView = (ViewGroup)inflater.inflate(R.layout.item_wagenstand_section, mSectionLabelContainer, false);

            TextView waggonNumberLabel = sectionView.findViewById(R.id.waggon_number_label);
            final ImageView waggonView = sectionView.findViewById(R.id.waggon_view);
            final ImageView secondWaggonHalf = sectionView.findViewById(R.id.second_waggon_part_image);

            final int sectionLabelDimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30 , getResources().getDisplayMetrics());
            final int underlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2 , getResources().getDisplayMetrics());

            final TextView sectionLabel = new TextView(mContext);
            sectionLabel.setTextColor(getResources().getColor(R.color.inactive_gray));
            sectionLabel.setGravity(Gravity.CENTER);
            sectionLabel.setTypeface(Typeface.DEFAULT_BOLD);

            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(sectionLabelDimension,sectionLabelDimension);
            sectionLabel.setLayoutParams(labelParams);

            // check if we are in a new section and display the label
            if (!lastSectionOfWaggon.equals(currentSection)) {
                sectionLabel.setText(lastSectionOfWaggon);
                currentSection = lastSectionOfWaggon;

                sectionLabel.setOnClickListener(this);
                sectionLabels.add(sectionLabel);

                mSectionLabelContainer.addView(sectionLabel);
            }


            // configure Train Heads
            if (waggon.isHead()) {
                waggonView.setBackgroundResource(trainFormation.isReversed() ? R.drawable.app_fahrtrichtung_rechts : R.drawable.app_fahrtrichtung_links);
            } else if (waggon.isTail()) {
                waggonView.setBackgroundResource(trainFormation.isReversed() ? R.drawable.small_train_head : R.drawable.small_train_back);
            } else if (waggon.isTrainHeadBothWays()) {
                final int waggonIndex = waggons.indexOf(waggon);
                if (trainFormation.isReversed() && waggonIndex == waggons.size() - 1) {
                    waggonView.setBackgroundResource(R.drawable.app_regionalzug_fahrtrichtung_rechts);
                } else if (!trainFormation.isReversed() && waggonIndex == 0) {
                    waggonView.setBackgroundResource(R.drawable.app_regionalzug_fahrtrichtung_links);
                } else {
                    waggonView.setBackgroundResource(R.drawable.app_regionalzug);
                }
            } else {
                GradientDrawable shape = (GradientDrawable) getResources().getDrawable(R.drawable.legacy_waggon_shape);
                shape.setColor(waggon.getPrimaryColor());
                waggonView.setBackgroundDrawable(shape);

                if (waggon.isMultiClass()) {
                    GradientDrawable secondHalfShape = (GradientDrawable) getResources().getDrawable(R.drawable.legacy_second_half_wagon_shape);
                    secondHalfShape.setColor(waggon.getSecondaryColor());
                    secondWaggonHalf.setBackgroundDrawable(secondHalfShape);
                }
            }

            // adjust the length of a waggon according to the data
            // and add neccessary margins

            final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)waggonView.getLayoutParams();
            if (waggon.getLength() > 1.0) {
                layoutParams.width = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
            } else {
                layoutParams.width = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            }
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getBoolean(R.bool.isTablet)?20:15, getResources().getDisplayMetrics());


            waggonView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    LayoutParams params = (LinearLayout.LayoutParams)sectionView.getLayoutParams();
                    int marginEnd = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
                    params.setMargins(0, 0, marginEnd, 0);
                    sectionView.setLayoutParams(params);
                    sectionLabel.setX(sectionView.getX()-(sectionView.getWidth()-waggonView.getWidth()));
                }
            });

            waggonView.setLayoutParams(layoutParams);

            if (waggon.isMultiClass()) {
                RelativeLayout.LayoutParams secondaryLayoutParams = (RelativeLayout.LayoutParams)secondWaggonHalf.getLayoutParams();
                secondaryLayoutParams.width = layoutParams.width/2;
                secondaryLayoutParams.height = layoutParams.height;
                waggonNumberLabel.setText("");

            } else {
                if (waggon.isRestaurant()) {
                    waggonView.setImageResource(R.drawable.legacy_bistro_icon_small);
                } else {
                    waggonNumberLabel.setText(waggon.getClass());
                }
            }

            mTrainSymbolContainer.addView(sectionView);
        }

        if (sectionLabels.size() > 0) {
            String firstSection = sectionLabels.get(0).getText().toString();
            setActiveSection(firstSection);
        }
    }

    public TextView setActiveSection(String section) {
        TextView activeTextView = null;
        for (TextView sectionLabel : sectionLabels) {
            if (sectionLabel.getText().equals(section)) {
                sectionLabel.setTextColor(getResources().getColor(R.color.text_color_secondary));
                activeTextView = sectionLabel;
            } else {
                sectionLabel.setTextColor(getResources().getColor(R.color.inactive_gray));
            }
        }

        return activeTextView;
    }

    private void setUnderline(int highlightItem) {
        for (int i = 0; i < mTrainSymbolContainer.getChildCount(); i++) {
            final View trainView = mTrainSymbolContainer.getChildAt(i);
            if (trainView != null) {
                final View underlineView = trainView.findViewById(R.id.underline);
                if (underlineView != null) {
                    underlineView.setVisibility(i == highlightItem ? VISIBLE : INVISIBLE);
                }
            }
        }
    }

    public void setActiveSection(String section, boolean animateTo) {
        final TextView activeTextView = setActiveSection(section);

        if (animateTo && activeTextView != null && section != activeSection) {
            activeSection = section;
            int offset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40 , getResources().getDisplayMetrics());

            int scrollX = 0;

            if (activeTextView.getLeft()/2 < mScrollView.getWidth()/2) {
                scrollX = activeTextView.getLeft()-offset;
            } else if (activeTextView.getLeft()/2 > mScrollView.getWidth()/2) {
                scrollX = activeTextView.getLeft()+offset;
            }

            if (sectionLabels.indexOf(activeTextView) == 0) {
                scrollX = 0;
            } else if (sectionLabels.indexOf(activeTextView) == sectionLabels.size()-1) {
                scrollX = mScrollView.getWidth();
            }

            mScrollView.smoothScrollTo(scrollX, 0);
        }
    }

    public void setDelegate(WagenstandSectionIndicatorListener delegate) {
        this.delegate = delegate;
    }

    /**
     * Clear the view
     * clear the views mTrainSymbolContainer, mSectionLabelContainer, mWaggonHighlightContainer
     * clear data sectionLabels
     */
    public void clearView() {
        mTrainSymbolContainer.removeAllViews();
        mSectionLabelContainer.removeAllViews();
        sectionLabels.clear();
    }

    @Override
    public void onClick(View v) {
        TextView sectionLabel = (TextView)v;
        setActiveSection(sectionLabel.getText().toString());

        this.delegate.didSelectSection(sectionLabel.getText().toString());
    }

    public void setActiveWaggon(int focusedItem, String section) {
        setActiveSection(section, true);
        setUnderline(focusedItem);
    }

}
