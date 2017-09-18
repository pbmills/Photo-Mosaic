package com.pre.canva.usecase;

import rx.Observable;

/**
 * Created by LeonardWu on 24/11/2016.
 */

abstract class AbstractBuilder {
    public abstract Observable createUseCase();
}
