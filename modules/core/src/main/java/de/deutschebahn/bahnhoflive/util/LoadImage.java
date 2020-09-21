/*
 * SPDX-FileCopyrightText: 2020 DB Station&Service AG <bahnhoflive-opensource@deutschebahn.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package de.deutschebahn.bahnhoflive.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class LoadImage extends AsyncTask<Object, Void, Bitmap> {

    private LevelListDrawable mDrawable;
    private TextView textView;
    private int width;


    @Override
    protected Bitmap doInBackground(Object... params) {
        String source = (String) params[0];
        mDrawable = (LevelListDrawable) params[1];
        textView = (TextView) params[2];
        width = (Integer) params[3];

        try {
            InputStream is = new URL(source).openStream();
            Bitmap b = BitmapFactory.decodeStream(is);
            return b;//Bitmap.createScaledBitmap(b, mDrawable.getBounds().width(), mDrawable.getBounds().height(), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            float ascpect = (float)bitmap.getWidth()/(float)bitmap.getHeight();

            Log.e("KK","w " + width + "  asp " + ascpect);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, (int)(width/ascpect), true);

            BitmapDrawable d = new BitmapDrawable(textView.getContext().getResources(),scaledBitmap);
            mDrawable.addLevel(1, 1, d);
            mDrawable.setBounds(0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());

            mDrawable.setLevel(1);
            // i don't know yet a better way to refresh TextView
            // mTv.invalidate() doesn't work as expected
            CharSequence t = textView.getText();
            textView.setText(t);
        }
    }
}