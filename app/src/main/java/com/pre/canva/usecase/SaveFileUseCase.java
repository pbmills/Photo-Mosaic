package com.pre.canva.usecase;

import android.graphics.Bitmap;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import rx.Observable;
import rx.exceptions.Exceptions;
import rx.schedulers.Schedulers;

/**
 * Created by LeonardWu on 9/01/2017.
 */

public class SaveFileUseCase extends AbstractUseCase {
    private Bitmap bitmap;
    private String fullFilePath;

    private SaveFileUseCase(Bitmap bitmap, String fullFilePath) {
        this.bitmap = bitmap;
        this.fullFilePath = fullFilePath;
    }

    @Override
    protected Observable<File> buildObservable() {
        return Observable
                .create(subscriber -> {
                    File file = new File(fullFilePath);
                    try {
                        Files.createParentDirs(file);
                    } catch (IOException e) {
                        Exceptions.propagate(e);
                    }

                    OutputStream stream = null;

                    try {
                        stream = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        Exceptions.propagate(e);
                    }

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                    try {
                        stream.close();
                    } catch (IOException e) {
                        Exceptions.propagate(e);
                    }

                    subscriber.onNext(file);
                    subscriber.onCompleted();
                });
    }

    public SaveFileUseCase setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        return this;
    }

    public SaveFileUseCase setFullFilePath(String fullFilePath) {
        this.fullFilePath = fullFilePath;
        return this;
    }

    public static class Builder extends AbstractBuilder {

        private Bitmap bitmap;
        private String fullFilePath;

        @Override
        public Observable<File>  createUseCase() {
            return new SaveFileUseCase(bitmap, fullFilePath)
                    .buildObservable()
                    .subscribeOn(Schedulers.computation());
        }

        public Builder setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public Builder setFullFilePath(String fullFilePath) {
            this.fullFilePath = fullFilePath;
            return this;
        }
    }
}
