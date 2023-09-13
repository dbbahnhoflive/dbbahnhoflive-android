/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.tutorial;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.deutschebahn.bahnhoflive.R;

public class TutorialView extends FrameLayout {

    private Button mCloseButton;
    private Tutorial mTutorial;

    private TextView mHeadlineLabel;
    private TextView mDescriptionLabel;

    public TutorialViewDelegate mDelegate;
    public boolean mIsVisible = false;

    public interface TutorialViewDelegate {
        void didCloseTutorialView(TutorialView view, Tutorial tutorial);
    }

    public TutorialView(Context context) {
        super(context);
        init();
    }

    public TutorialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Tutorial getCurrentlyVisibleTutorial() {
        return mTutorial;
    }

    public void show(TutorialViewDelegate delegate, Tutorial tutorial) {
        mDelegate = delegate;
        mTutorial = tutorial;

        mHeadlineLabel.setText(mTutorial.title);
        mDescriptionLabel.setText(mTutorial.descriptionText);

        setVisibility(View.VISIBLE);
        mIsVisible = true;
    }

    public void hide() {
        setVisibility(View.GONE);
        mIsVisible = false;
        mTutorial = null;
    }

    protected void init() {
        inflate(getContext(), R.layout.tutorial_view, this);

        mCloseButton = findViewById(R.id.close_button);
        mHeadlineLabel = findViewById(R.id.headline_label);
        mDescriptionLabel = findViewById(R.id.description_label);

        mCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(View.GONE);

                if (mTutorial != null)
                    mTutorial.closedByUser = true;
                mIsVisible = false;

                if (mDelegate != null) {
                    mDelegate.didCloseTutorialView(TutorialView.this, mTutorial);
                }
            }
        });
    }

}
