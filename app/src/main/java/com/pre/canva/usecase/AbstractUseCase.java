package com.pre.canva.usecase;

import java.io.IOException;

import rx.Observable;

/**
 * Created by LeonardWu on 24/11/2016.
 */

abstract class AbstractUseCase {
    protected abstract Observable buildObservable() throws IOException;
}
