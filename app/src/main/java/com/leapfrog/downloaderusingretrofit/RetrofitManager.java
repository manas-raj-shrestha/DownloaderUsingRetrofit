package com.leapfrog.downloaderusingretrofit;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by Manas on 12/18/2015.
 */
public class RetrofitManager {

    public static Retrofit retrofit = null;
    public static RetrofitApi retrofitApi = null;
    public static RetrofitManager retrofitManager = null;
    private ProgressListener progressListener;

    public void setProgressListener(ProgressListener progressListener){
        this.progressListener = progressListener;
    }

    private RetrofitManager() {

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.10.11.112:9090/")
                .addConverterFactory(GsonConverterFactory.create()).client(buildOkHttpClient())
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(60, TimeUnit.SECONDS);
        okHttpClient.setRetryOnConnectionFailure(true);

        okHttpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return null;
            }
        });

        retrofitApi = retrofit.create(RetrofitApi.class);

    }

    public static RetrofitManager getInstance() {
        if (retrofitManager == null) {
            retrofitManager = new RetrofitManager();
        }
        return retrofitManager;
    }

    public void getThumb(Callback<ResponseBody> callback, String fileName) {
        Call<ResponseBody> memberCredential = retrofitApi.getThumbs(fileName);
        memberCredential.enqueue(callback);
    }

    private OkHttpClient buildOkHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        });
        return okHttpClient;
    }

    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            try {
                return responseBody.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                try {
                    bufferedSource = Okio.buffer(source(responseBody.source()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) {
                    long bytesRead = 0;
                    try {
                        bytesRead = super.read(sink, byteCount);
                        // read() returns the number of bytes read, or -1 if this source is exhausted.
                        totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                        progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                        return bytesRead;
                    } catch (IOException e) {
                        Log.e("out","---");
                        e.printStackTrace();
                        return totalBytesRead;
                    }
                }
            };
        }
    }
}
