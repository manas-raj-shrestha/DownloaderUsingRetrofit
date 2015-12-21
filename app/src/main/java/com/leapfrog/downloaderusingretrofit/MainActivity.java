package com.leapfrog.downloaderusingretrofit;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.squareup.okhttp.ResponseBody;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements ProgressListener {

    Button btnGetFile;
    String[] downloadFilesQueue = new String[]{"sample1.mp4", "sample2.mp4", "sample3.mp4", "sample4.mp4"};
    ArrayList<String> downloadFilesArryList = new ArrayList<>();
    ArrayList<Integer> progressList = new ArrayList<>();
    ArrayList<DownloadModel> downloadModels = new ArrayList<>();

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetFile = (Button) findViewById(R.id.btn_get_file);

        downloadFilesArryList.add("sample1.mp4");
        downloadFilesArryList.add("sample2.mp4");
        downloadFilesArryList.add("sample3.mp4");
        downloadFilesArryList.add("sample4.mp4");
        RetrofitManager.getInstance().setProgressListener(this);
        btnGetFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                createDummyList();
                Intent intent = new Intent(MainActivity.this, RetroDownloadService.class);
                intent.putParcelableArrayListExtra(RetroDownloadService.KEY_DOWNLOAD_LIST, downloadModels);
                startService(intent);

            }
        });
    }

    public void createDummyList() {
        downloadModels.clear();
        downloadModels.add(new DownloadModel("short clip", "sample1.mp4", "/Android/data/" + getPackageName() + "/appdata"));
        downloadModels.add(new DownloadModel("rajesh", "sample2.jpg", "/Android/data/" + getPackageName() + "/appdata"));
        downloadModels.add(new DownloadModel("surhid", "sample3.jpg", "/Android/data/" + getPackageName() + "/appdata"));
        downloadModels.add(new DownloadModel("shilu", "sample4.jpg", "/Android/data/" + getPackageName() + "/appdata"));
        downloadModels.add(new DownloadModel("lego house", "sample5.mp4", "/Android/data/" + getPackageName() + "/appdata"));
        Log.e("===", "" + getPackageName());
    }

    public void checkDownloadQueue() {
        if (downloadFilesArryList.size() != 0) {
            Log.e("queue not empty", "--");
            startRetrofitDownload(downloadFilesArryList.get(0));
            mBuilder.setContentText(downloadFilesArryList.get(0));
            progressList.clear();
            mNotifyMgr.notify(001, mBuilder.build());
            downloadFilesArryList.remove(0);

        } else {
            Log.e("queue is empty", "--");
        }
    }

    public void startRetrofitDownload(final String fileName) {
        RetrofitManager.getInstance().getThumb(new Callback<ResponseBody>() {

            @Override
            public void onResponse(retrofit.Response<ResponseBody> response, Retrofit retrofit) {
//                        InputStream response1 =response.raw();

                //Response is = response.body();

                //response.raw().
                //System.out.println("Response instance of byte =="+ )
                //response.body().
//                        Log.e("success req", "--" + is.toString());


//                DecodeThread decodeThread = new DecodeThread(response, fileName);
//                Log.e("now decoding", "--");
//                decodeThread.start();


            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("failed req", "--" + t.getMessage());
            }
        }, "/" + fileName);
    }

//    @Override
//    public void update(long bytesRead, long contentLength, boolean done) {
////        int progress = (int) ((100 * bytesRead) / contentLength);
////        if (progress % 10 == 0) {
////            if (!progressList.contains(progress)) {
////                progressList.add(progress);
////                mBuilder.setProgress(100, (int) progress, false);
////                mNotifyMgr.notify(001, mBuilder.build());
////            }
////            Log.e("progress ", "" + ((100 * bytesRead) / contentLength));
////        }
//    }


    @Override
    public void update(long bytesRead, long contentLength, boolean done, boolean connectionTimeOut) {

    }
}
