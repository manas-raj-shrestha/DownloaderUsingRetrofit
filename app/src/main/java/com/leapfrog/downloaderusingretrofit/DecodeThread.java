package com.leapfrog.downloaderusingretrofit;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit.Response;

public class DecodeThread extends Thread{
    Response<ResponseBody> response;
    String fileName;
    Handler handler;
    String sdCardLocation;

    public DecodeThread(Response<ResponseBody> response, Handler handler, String filename, String storageLocation) {
        this.fileName = filename;
        this.response = response;
        this.handler = handler;
        this.sdCardLocation = storageLocation;
    }

    @Override
    public void run() {
        super.run();

        InputStream input = null;
        try {
            input = response.body().byteStream();
            File directory = new File(Environment.getExternalStorageDirectory() + sdCardLocation);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(Environment.getExternalStorageDirectory() + sdCardLocation, fileName);

            OutputStream output = new FileOutputStream(file);
            try {
                try {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                    handler.sendEmptyMessage(0);

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