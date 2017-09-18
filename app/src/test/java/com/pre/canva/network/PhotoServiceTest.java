package com.pre.canva.network;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

import static org.junit.Assert.*;
import static org.hamcrest.core.IsEqual.equalTo;
/**
 * Created by LeonardWu on 25/11/2016.
 */
@Config(sdk = 23)
@RunWith(RobolectricTestRunner.class)
public class PhotoServiceTest {
    IPhotoService service;
    ResponseBody resultBody;
    CountDownLatch lock = new CountDownLatch(1);

    static final int NETWORK_WAITING = 2000;//milli seconds

    @Before
    public void beforeTest() throws Exception {
        service = new Retrofit.Builder()
                .baseUrl("http://localhost:8765/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(IPhotoService.class);
    }

    /**
     * connect to http://localhost:8765/32/32/abc123
     */
    @Test
    public void testGetTilePhoto() throws Exception {
        service.getTilePhoto(32, 32, "abc123")
                .observeOn(Schedulers.trampoline())
                .subscribe(
                        body -> resultBody = body,
                        error -> {
                            resultBody = null;
                            lock.countDown();
                            assertFalse(true);
                        },
                        () -> lock.countDown());

        lock.await(NETWORK_WAITING, TimeUnit.MILLISECONDS);

        byte[] expected = Files.toByteArray(new File("src/test/res/abc123.png"));

        byte[] actual = ByteStreams.toByteArray(resultBody.byteStream());

        assertThat(actual, equalTo(expected));
    }
}