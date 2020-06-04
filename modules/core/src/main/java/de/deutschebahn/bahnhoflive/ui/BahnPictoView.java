package de.deutschebahn.bahnhoflive.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import de.deutschebahn.bahnhoflive.R;
import de.deutschebahn.bahnhoflive.util.font.FontUtil;

public class BahnPictoView extends androidx.appcompat.widget.AppCompatTextView {

    public static final int TEXTCOLOR_DEFAULT = R.color.textcolor_default;
    public static final int TEXTSIZE_DEFAULT = R.dimen.textsize_24;

    public BahnPictoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public BahnPictoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BahnPictoView(Context context) {
        super(context);
        init(null);
    }

    private void init( AttributeSet attrs) {

        setTypeface(FontUtil.getDbPicto());

        //get some attributes from android: namespace
        int[] textviewAttrs = new int[] {
                android.R.attr.textColor
        };

        //set Textcolor to default if not specified in xml attribute
        Integer color = null;
        TypedArray ta = getContext().obtainStyledAttributes(attrs, textviewAttrs);
        try {
            color = ta.getResourceId(0,0);
        } finally {
            ta.recycle();
        }
        if (color == 0) {
            setTextColor(getResources().getColor(TEXTCOLOR_DEFAULT));
        }
    }

}
