package de.deutschebahn.bahnhoflive.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

import de.deutschebahn.bahnhoflive.R;

public class TextViewImageGetter implements ImageGetter {
		TextView tv;
		private int width;
		
		public TextViewImageGetter (TextView v, int width) {
			tv = v;
			this.width = width;
		}
		
	    @Override
	    public Drawable getDrawable(String source){
	    	LevelListDrawable d = new LevelListDrawable();
	    	Drawable empty = tv.getResources().getDrawable(R.drawable.placeholder);
	        d.addLevel(0, 0, empty);
	        d.setBounds(0, 0, empty.getIntrinsicWidth(), empty.getIntrinsicHeight());
	        
	        new LoadImage().execute(source, d, tv, width);
	        
	        return d;
	    }
	}