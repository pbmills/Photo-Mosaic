package com.pre.canva.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Scheduler;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

/**
 * Created by Pre on 15/09/2017.
 */

public class WebAgent {
    private final int MAX_CONCURRENT_RETROFIT_THREADS = 64;
    protected String baseUrl = "http://localhost:8765/";
    protected IPhotoService service;

    Scheduler scheduler;

    public WebAgent(String baseUrl) {
        this.baseUrl = baseUrl;

        //max concurrent getTilePhoto request is 64
        final int threads = Math.min(Runtime.getRuntime().availableProcessors() * 8, MAX_CONCURRENT_RETROFIT_THREADS);
        scheduler = Schedulers.from(Executors.newFixedThreadPool(threads));

        service = buildPhotoService(baseUrl);
    }

    private IPhotoService buildPhotoService(String baseUrl) {
        return new Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IPhotoService.class);
    }

    /**
     * only invoke this method when there is no other network request in process
     * @param url, full path url including host address and port, example "http://10.4.35.34:8765/"
     */
    public void setWebServer(String url) {
        if (baseUrl.equals(url)) {
            return;
        }

        baseUrl = url;

        service = buildPhotoService(baseUrl);
    }

    /**
     * @param color ARGB8888
     * @return caller should recycle the returned bitmap
     */
    public Observable<Bitmap> getTilePhoto(int tileWidth, int tileHeight, int color) {
        return service.getTilePhoto(tileWidth, tileHeight, String.format("%06x", color & 0xFFFFFF))
                .map(body -> {
                    InputStream in = body.byteStream();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
                    try {
                        in.close();
                    } catch (IOException e) {
                        Exceptions.propagate(e);
                    }
                    return bitmap;
                })
                .subscribeOn(scheduler);
    }
}
