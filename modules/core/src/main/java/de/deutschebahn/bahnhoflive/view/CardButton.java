package de.deutschebahn.bahnhoflive.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import de.deutschebahn.bahnhoflive.R;

public class CardButton extends DecorationFrameLayout {
    protected ImageView imageView;
    protected TextView labelView;

    public CardButton(Context context) {
        super(context);
    }

    public CardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onInit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setViews(context);

        labelView = findViewById(R.id.label);
        imageView = findViewById(R.id.image);

        final TypedArray typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.CardButton, defStyleAttr, defStyleRes);

        final CharSequence text = typedArray.getText(R.styleable.CardButton_text);
        labelView.setText(text);

        final Drawable drawable = typedArray.getDrawable(R.styleable.CardButton_drawable);
        imageView.setImageDrawable(drawable);

        typedArray.recycle();
    }

    protected void setViews(Context context) {
        setViews(context, R.layout.decoration_card_button, R.id.content);
    }

    public void setText(@StringRes int resid) {
        labelView.setText(resid);
    }

    public void setText(CharSequence text) {
        labelView.setText(text);
    }

    public void setDrawable(@DrawableRes int resId) {
        imageView.setImageResource(resId);
    }
}
