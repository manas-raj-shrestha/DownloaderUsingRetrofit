package com.leapfrog.downloaderusingretrofit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.okhttp.ResponseBody;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.Retrofit;

/**
 * Service to download files using retrofit
 */
public class RetroDownloadService extends Service implements ProgressListener {

    public static String KEY_DOWNLOAD_LIST = "key_download_list";

    private static int NOTIFICATION_ID = 001;
    private static int NOTIFICATION_PROGRESS_UPPER_BOUND = 100;
    private static int HEAD_POSITION = 0;
    private static int PROGRESS_UPDATE_INTERVAL = 10;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;

    private ArrayList<DownloadModel> downloadModels = new ArrayList<>();
    private ArrayList<Integer> progressList = new ArrayList<>();
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            checkDownloadQueue();
            return false;
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            handleCommand(intent);
        }

        RetrofitManager.getInstance().setProgressListener(this);

        return START_STICKY;
    }

    /**
     * Core method that initializes the download and then starts it
     *
     * @param intent
     */
    private void handleCommand(Intent intent) {

        downloadModels = intent.getParcelableArrayListExtra(KEY_DOWNLOAD_LIST);

        createNotification();
        checkDownloadQueue();
    }

    /**
     * Method to create notification
     */
    private void createNotification() {
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.btn_minus).setContentTitle("Downloading");

        // Gets an instance of the NotificationManager service
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        notificationBuilder.setProgress(NOTIFICATION_PROGRESS_UPPER_BOUND, 0, false);
        Notification notification = notificationBuilder.build();
        notification.flags = Notification.FLAG_NO_CLEAR;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Method to start download using retrofit
     *
     * @param fileName Name of file to be downloaded
     */
    public void startRetrofitDownload(final String fileName) {
        RetrofitManager.getInstance().getThumb(new Callback<ResponseBody>() {

            @Override
            public void onResponse(retrofit.Response<ResponseBody> response, Retrofit retrofit) {
                DecodeThread decodeThread = new DecodeThread(response, handler, fileName);
                decodeThread.start();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("failed req", "--" + t.getMessage());
            }
        }, "/" + fileName);
    }

    /**
     * Method to check if there is any download last left
     */
    public void checkDownloadQueue() {
        if (downloadModels.size() != 0) {
            startRetrofitDownload(downloadModels.get(HEAD_POSITION).getUrls());
            notificationBuilder.setContentText(downloadModels.get(HEAD_POSITION).getUrls());
            progressList.clear();
            Notification notification = notificationBuilder.build();
            notification.flags = Notification.FLAG_NO_CLEAR;
            notificationManager.notify(NOTIFICATION_ID, notification);
            downloadModels.remove(HEAD_POSITION);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void update(long bytesRead, long contentLength, boolean done) {
        int progress = (int) ((100 * bytesRead) / contentLength);
        if (progress % PROGRESS_UPDATE_INTERVAL == 0) {
            if (!progressList.contains(progress)) {
                progressList.add(progress);
                notificationBuilder.setProgress(NOTIFICATION_PROGRESS_UPPER_BOUND, (int) progress, false);
                Notification notification = notificationBuilder.build();
                notification.flags = Notification.FLAG_NO_CLEAR;
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
    }

}
