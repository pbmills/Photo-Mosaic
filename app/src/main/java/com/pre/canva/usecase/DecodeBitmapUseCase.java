package com.pre.canva.usecase;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.pre.canva.image.BitmapHelper;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by LeonardWu on 24/11/2016.
 */

public class DecodeBitmapUseCase extends AbstractUseCase {
    private Uri uri;
    private Context context;

    private DecodeBitmapUseCase(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    @Override
    protected Observable<Bitmap> buildObservable() {
        return Observable.<Bitmap>create(s -> {
            try {
                Bitmap bitmap = BitmapHelper.decodeBitmap(context, uri);
                s.onNext(bitmap);
            } catch (Exception e) {
                s.onError(e);
            }
        })
                .subscribeOn(Schedulers.io());
    }

    public static class Builder extends AbstractBuilder {
        private Context context;
        private Uri uri;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setUri(Uri uri) {
            this.uri = uri;
            return this;
        }

        /**
         *
         * @return default running on io(), mainThread() threads
         */
        @Override
        public Observable<Bitmap> createUseCase() {
            return new DecodeBitmapUseCase(context, uri)
                    .buildObservable()
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

}
