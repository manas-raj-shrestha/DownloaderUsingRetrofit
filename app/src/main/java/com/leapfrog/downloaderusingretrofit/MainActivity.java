package com.leapfrog.downloaderusingretrofit;

import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    Button btnGetFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetFile = (Button) findViewById(R.id.btn_get_file);


        btnGetFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainActivity.this)
                                .setSmallIcon(android.R.drawable.btn_minus)
                                .setContentTitle("My notification")
                                .setContentText("Hello World!");

                int mNotificationId = 001;
// Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
                mBuilder.setProgress(100,20,false);
                mNotifyMgr.notify(mNotificationId, mBuilder.build());

                RetrofitManager.getInstance().getThumb(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(retrofit.Response<ResponseBody> response, Retrofit retrofit) {
//                        InputStream response1 =response.raw();

                        //Response is = response.body();

                        //response.raw().
                        //System.out.println("Response instance of byte =="+ )
                        //response.body().
//                        Log.e("success req", "--" + is.toString());


                        DecodeThread decodeThread = new DecodeThread(response);

                        decodeThread.start();


                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("failed req", "--" + t.getMessage());
                    }
                });
            }
        });
    }

    public class DecodeThread extends Thread {
        Response<ResponseBody> response;

        public DecodeThread(Response<ResponseBody> response) {
            this.response = response;
        }

        @Override
        public void run() {
            super.run();

            InputStream input = null;
            try {
                input = response.body().byteStream();
                File file = new File(Environment.getExternalStorageDirectory(), "sample.mp4");
                OutputStream output = new FileOutputStream(file);
                try {
                    try {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;

                        while ((read = input.read(buffer)) != -1) {
                            output.write(buffer, 0, read);
                        }
                        output.flush();
                        Log.e("success req", "--");
                    } finally {
                        output.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // handle exception, define IOException and others
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
