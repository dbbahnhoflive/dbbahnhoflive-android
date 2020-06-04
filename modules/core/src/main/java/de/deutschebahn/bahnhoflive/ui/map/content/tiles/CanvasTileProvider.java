package de.deutschebahn.bahnhoflive.ui.map.content.tiles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

public class CanvasTileProvider implements TileProvider {

    static final int TILE_SIZE = 512;
    private TileProvider mTileProvider;

    private BitMapThreadLocal tlBitmap;

    public CanvasTileProvider(TileProvider tileProvider) {
        mTileProvider = tileProvider;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        byte[] data;
        Bitmap image = getNewBitmap();
        Canvas canvas = new Canvas(image);
        onDraw(canvas, zoom, x,y);
        data = bitmapToByteArray(image);
        Tile tile = new Tile(TILE_SIZE, TILE_SIZE, data);

        return tile;
    }

    Paint paint = new Paint();

    private void onDraw(Canvas canvas, int zoom, int x, int y) {
        x = x*2;
        y = y*2;

        Tile leftTop = mTileProvider.getTile(x, y, zoom + 1);
        if(leftTop != NO_TILE) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(leftTop.data, 0, leftTop.data.length);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            bitmap.recycle();
            leftTop = null;
        }

        Tile leftBottom = mTileProvider.getTile(x, y + 1, zoom + 1);
        if(leftBottom != NO_TILE) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(leftBottom.data, 0, leftBottom.data.length);
            canvas.drawBitmap(bitmap, 0, 256, paint);
            bitmap.recycle();
            leftBottom = null;
        }

        Tile rightTop = mTileProvider.getTile(x + 1, y, zoom + 1);
        if(rightTop != NO_TILE) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(rightTop.data, 0, rightTop.data.length);
            canvas.drawBitmap(bitmap, 256, 0, paint);
            bitmap.recycle();
            rightTop = null;
        }

        Tile rightBottom = mTileProvider.getTile(x + 1, y + 1, zoom + 1);
        if(rightBottom != NO_TILE) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(rightBottom.data, 0, rightBottom.data.length);
            canvas.drawBitmap(bitmap, 256, 256, paint);
            bitmap.recycle();
            rightBottom = null;
        }
    }

    private Bitmap getNewBitmap() {
        if(tlBitmap != null) {
            Bitmap bitmap = tlBitmap.get();
            // Clear the previous bitmap
            bitmap.eraseColor(Color.TRANSPARENT);
            return bitmap;
        } else {
            return Bitmap.createBitmap(TILE_SIZE, TILE_SIZE, Bitmap.Config.RGB_565);
        }
    }

    private static byte[] bitmapToByteArray(Bitmap bm) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] data = bos.toByteArray();
        return data;
    }

    class BitMapThreadLocal extends ThreadLocal<Bitmap> {
        @Override
        protected Bitmap initialValue() {
            Bitmap image = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE,
                    Bitmap.Config.RGB_565);
            return image;
        }
    }

    public void recycle() {
        if(tlBitmap != null) {
            tlBitmap.remove();
            tlBitmap = null;
        }
        mTileProvider = null;
        paint.reset();
        paint = null;
        System.gc();
    }
}
