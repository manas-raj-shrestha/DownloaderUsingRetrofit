package com.leapfrog.downloaderusingretrofit;


import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Streaming;
import retrofit.http.Url;

/**
 * Created by Manas on 12/18/2015.
 */
public interface RetrofitApi {
    @Headers("Content-Type: charset=binary")
    @GET
    @Streaming
    //Call<Response> getThumbs();
    Call<ResponseBody> getThumbs(@Url String url);
}
