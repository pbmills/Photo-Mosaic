package com.pre.canva.network;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Pre on 15/09/2017.
 */

public interface IPhotoService {
    @GET("color/{tileWidth}/{tileHeight}/{color}")
    Observable<ResponseBody> getTilePhoto(@Path("tileWidth")int tileWidth, @Path("tileHeight")int tileHeight, @Path("color") String color);
}
