package com.pre.canva.image;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pre on 15/09/2017.
 */

public class BitmapHelper {
    public static Bitmap decodeBitmap(Context context, Uri uri) throws Exception{
        final ContentResolver contentResolver = context.getContentResolver();
        InputStream in =  contentResolver.openInputStream(uri);

        //get size
        BitmapFactory.Options optionSize = new BitmapFactory.Options();
        optionSize.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, optionSize);

        in.close();

        in =  contentResolver.openInputStream(uri);

        //get content
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = getInSampleSize(optionSize.outWidth, optionSize.outHeight);
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);

        in.close();

        //Todo: rotate bitmap according to exif orientation
        return bitmap;
    }

    public static int getInSampleSize(int srcWidth, int srcHeight) {
        int maxTextureSize = getMaxTextureSize();
        if (srcWidth <= maxTextureSize && srcHeight <= maxTextureSize) {
            return 1; //no need to scale down
        }

        //factor = source / destination
        float scaleFactor;

        float srcAspect = ((float) srcWidth) / srcHeight;
        float dstAspect = 1.0f;

        if (srcAspect >= dstAspect) {
            scaleFactor = (float)srcWidth/maxTextureSize;
        } else {
            scaleFactor = (float)srcHeight/maxTextureSize;
        }

        return (int)Math.ceil(scaleFactor);
    }

    public static int getMaxTextureSize() {
        //Todo: detect max texture size on the fly
        return 2048;
    }

    /**
     *
     * @return a 1D ArrayList<Rect> contains all the segmented tiles
     */
    public static ArrayList<Rect> tileSegmentation(final int srcWidth, final int srcHeight, final int tileWidth, final int tileHeight) {
        ArrayList<Rect> tileList = new ArrayList<>();

        for(int currentTop = 0 ; currentTop < srcHeight ; currentTop += tileHeight)
            for(int currentLeft = 0; currentLeft < srcWidth ; currentLeft += tileWidth){
                final int tileRight = currentLeft + tileWidth - 1;
                final int tileBottom = currentTop + tileHeight - 1;
                final Rect tile = new Rect(currentLeft, currentTop, tileRight, tileBottom);
                tileList.add(tile);
            }

        return tileList;
    }

    public static ArrayList<ArrayList<Rect>> tileSegmentationByRow(final int srcWidth, final int srcHeight, final int tileWidth, final int tileHeight) {
        ArrayList<ArrayList<Rect>> tileList = new ArrayList<>();

        for(int currentTop = 0 ; currentTop < srcHeight ; currentTop += tileHeight) {
            ArrayList<Rect> rowTileList = new ArrayList<>();
            for (int currentLeft = 0; currentLeft < srcWidth; currentLeft += tileWidth) {
                final int tileRight = currentLeft + tileWidth - 1;
                final int tileBottom = currentTop + tileHeight - 1;
                final Rect tile = new Rect(currentLeft, currentTop, tileRight, tileBottom);
                rowTileList.add(tile);
            }

            tileList.add(rowTileList);
        }

        return tileList;
    }

    public static class TileColor {
        public Rect rect;
        public int color;

        public TileColor(Rect rect, int color) {
            this.color = color;
            this.rect = rect;
        }
    }

    public static class TileBitmap extends TileColor {
        public Bitmap bitmap;

        public TileBitmap(TileColor tileColor, Bitmap bitmap) {
            super(tileColor.rect, tileColor.color);
            this.bitmap = bitmap;
        }
    }

    public static int getTileAverageColor(int srcPixels[], int srcWidth, int srcHeight, Rect tileRect) {
        final Rect from = new Rect(tileRect.left, tileRect.top, Math.min(tileRect.right, srcWidth - 1), Math.min(tileRect.bottom, srcHeight -1));
        final int leftTop = srcWidth * tileRect.top + tileRect.left;

        final int tileWidth = from.width();
        final int tileHeight = from.height();

        float rowAverageRed;
        float rowAverageGreen;
        float rowAverageBlue;

        float rowAverageRedSum = 0f;
        float rowAverageGreenSum = 0f;
        float rowAverageBlueSum = 0f;

        for(int i = 0 ; i < tileHeight ; i++){
            long rowSumRed = 0;
            long rowSumBlue = 0;
            long rowSumGreen = 0;
            for( int j = 0 ; j < tileWidth; j++){
                final int index = leftTop + srcWidth * i + j;
                final int color = srcPixels[index];

                rowSumRed   += Color.red(color);
                rowSumGreen += Color.green(color);
                rowSumBlue  += Color.blue(color);
            }
            rowAverageRed = (float)rowSumRed/tileWidth;
            rowAverageGreen = (float)rowSumGreen/tileWidth;
            rowAverageBlue = (float)rowSumBlue/tileWidth;

            rowAverageRedSum += rowAverageRed;
            rowAverageGreenSum += rowAverageGreen;
            rowAverageBlueSum += rowAverageBlue;
        }

        int averageRed = (int)(rowAverageRedSum/tileHeight);
        int averageGreen = (int)(rowAverageGreenSum/tileHeight);
        int averageBlue = (int)(rowAverageBlueSum/tileHeight);

        return Color.rgb(averageRed,averageGreen,averageBlue);
    }

    public static void combineTileList(List<TileBitmap> list, Canvas canvas) {
        for (TileBitmap tileBitmap : list) {
            Bitmap tile = tileBitmap.bitmap;
            final Rect rect = tileBitmap.rect;
            canvas.drawBitmap(tile, rect.left, rect.top, null);
            tile.recycle();
        }
    }
}
