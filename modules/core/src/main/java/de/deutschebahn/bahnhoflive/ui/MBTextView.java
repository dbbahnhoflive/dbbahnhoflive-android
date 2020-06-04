package de.deutschebahn.bahnhoflive.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;

import de.deutschebahn.bahnhoflive.R;

public class MBTextView extends androidx.appcompat.widget.AppCompatTextView {
	
	public static final int TEXTCOLOR_DEFAULT = R.color.textcolor_default;
	public static final int TEXTSIZE_DEFAULT = R.dimen.textsize_24;

	public MBTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	public MBTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public MBTextView(Context context) {
		super(context);
		init(null);
	}
	
	private void init( AttributeSet attrs) {

		//get some attributes from android: namespace
		int[] textviewAttrs = new int[] {
				android.R.attr.textColor
		};
		int xmlProvidedSize =0;
		if (attrs!=null) {
			xmlProvidedSize = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "textSize", 0);
		} 
		
		//set Textcolor to default if not specified in xml attribute
		Integer color = null;
		TypedArray ta = getContext().obtainStyledAttributes(attrs, textviewAttrs);
		try {
			color = ta.getResourceId(0,0);
		} finally {
			ta.recycle();
		}
		if (color==null || color==0) {
			setTextColor(getResources().getColor(TEXTCOLOR_DEFAULT));
		}

		if (xmlProvidedSize==0) {
			setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(TEXTSIZE_DEFAULT));
		}

		setLineSpacing(
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f,
						getResources().getDisplayMetrics()), 1.0f);

	}

}
