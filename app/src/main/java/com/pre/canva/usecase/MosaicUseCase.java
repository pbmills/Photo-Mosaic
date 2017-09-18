package com.pre.canva.usecase;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.pre.canva.image.BitmapHelper;
import com.pre.canva.network.WebAgent;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by LeonardWu on 24/11/2016.
 */

public class MosaicUseCase extends AbstractUseCase {
    private Bitmap bitmap;
    private int tileWidth;
    private int tileHeight;
    private WebAgent webAgent;
    private Bitmap resultContainer;
    int[] srcPixels;

    private MosaicUseCase(Bitmap bitmap, int tileWidth, int tileHeight, WebAgent webAgent, Bitmap resultContainer) {
        this.bitmap = bitmap;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.webAgent = webAgent;
        this.resultContainer = resultContainer;
    }

    /**
     *
     * @return the caller should recylce the return bitmap
     */
    @Override
    protected Observable<Bitmap> buildObservable() {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();

        srcPixels = new int[srcWidth * srcHeight];

        bitmap.getPixels(srcPixels, 0, srcWidth, 0, 0, srcWidth, srcHeight);

        Canvas resultCanvas = new Canvas(resultContainer);

        return Observable
                .just(bitmap)
                .map(bmp -> BitmapHelper.tileSegmentationByRow(srcWidth, srcHeight, tileWidth, tileHeight))
                .concatMapIterable(rows -> rows)
                .concatMap(row -> Observable
                        .from(row)
                        .flatMap(rect -> Observable.just(rect)
                                .subscribeOn(Schedulers.computation())
                                .map(rect2 -> new BitmapHelper.TileColor(rect2,
                                        BitmapHelper.getTileAverageColor(srcPixels, srcWidth, srcHeight, rect2)))
                                .flatMap(tileColor ->
                                        webAgent.getTilePhoto(tileColor.rect.width(), tileColor.rect.height(), tileColor.color)
                                                .map(bmp -> new BitmapHelper.TileBitmap(tileColor, bmp)))

                        )
                        .toList()
                )
                .map(mosaicRow -> {
                    BitmapHelper.combineTileList(mosaicRow, resultCanvas);
                    return resultContainer;
                })
                .doOnCompleted(() -> srcPixels = null)
                .doOnError(error -> srcPixels = null)
                .subscribeOn(Schedulers.computation());
    }

    public static class Builder extends AbstractBuilder {
        private Bitmap bitmap;
        private int tileWidth;
        private int tileHeight;
        private WebAgent webAgent;
        private Bitmap resultContainer;

        /**
         *
         * @return default running on io(), mainThread() threads
         */
        @Override
        public Observable<Bitmap> createUseCase() {
            return new MosaicUseCase(bitmap, tileWidth, tileHeight, webAgent, resultContainer)
                    .buildObservable()
                    .observeOn(AndroidSchedulers.mainThread());
        }

        public Builder setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public Builder setTileWidth(int tileWidth) {
            this.tileWidth = tileWidth;
            return this;
        }

        public Builder setTileHeight(int tileHeight) {
            this.tileHeight = tileHeight;
            return this;
        }

        public Builder setWebAgent(WebAgent webAgent) {
            this.webAgent = webAgent;
            return this;
        }

        public Builder setResultContainer(Bitmap resultContainer) {
            this.resultContainer = resultContainer;
            return this;
        }
    }

}
