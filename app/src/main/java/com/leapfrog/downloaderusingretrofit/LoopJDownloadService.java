package com.leapfrog.downloaderusingretrofit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StatFs;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Service to download files using retrofit
 */
public class LoopJDownloadService extends Service {

    public static final String KEY_DOWNLOAD_LIST = "key_download_list";

    private static final String MSG_DISK_FULL = "Disk full";
    private static final String MSG_RETRY = "Retry";
    private static final String MSG_DOWNLOAD_FAILED = "Download Failed";
    private static final String MSG_DOWNLOAD_SUCCESSFUL = "Download Successful";
    private static final String MSG_DOWNLOADING = "Downloading";

    private static final int NOTIFICATION_ID = 001;
    private static final int NOTIFICATION_PROGRESS_UPPER_BOUND = 100;
    private static final int HEAD_POSITION = 0;
    private static final int PROGRESS_UPDATE_INTERVAL = 10;
    private static final int MINIMUM_STORAGE_REQUIREMENT = 100;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private Intent intent;
    private boolean connectionTimeOut = false;
    private int currentDownload = 0;
    private int totalQueueItems;

    private ArrayList<DownloadModel> downloadQueue = new ArrayList<>();
    private ArrayList<Integer> progressList = new ArrayList<>();
    AsyncHttpClient client = new AsyncHttpClient();

    /**
     * Handler to know when a file is finished decoding
     * Removes the top most item from the queue once its download is successful
     */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (!connectionTimeOut) {
                downloadQueue.remove(HEAD_POSITION);
                checkDownloadQueue();
            }

            return false;
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        if (intent != null) {
            connectionTimeOut = false;
            handleCommand(intent);
        }

        return START_STICKY;
    }

    /**
     * Core method that initializes the download and then starts it
     *
     * @param intent {@link Intent}
     */
    private void handleCommand(final Intent intent) {

//        client.cancelRequests(LoopJDownloadService.this, true);
        if (client != null) {
            client.getHttpClient().getConnectionManager().shutdown();
            client = new AsyncHttpClient();
        }
        currentDownload = 0;
        downloadQueue = intent.getParcelableArrayListExtra(KEY_DOWNLOAD_LIST);
        totalQueueItems = downloadQueue.size();

        createNotification();
        if (checkDiskSize() < MINIMUM_STORAGE_REQUIREMENT) {
            PendingIntent pIntent = PendingIntent.getService(LoopJDownloadService.this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
            notificationBuilder.addAction(android.R.drawable.sym_action_chat, MSG_RETRY, pIntent);
            notificationBuilder.setContentIntent(pIntent);

            updateNotification(MSG_DISK_FULL, 0, false, 0);
        } else {
            checkDownloadQueue();
        }
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//
//            }
//        }, 800);

    }

    /**
     * Method to create notification
     */
    private void createNotification() {
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(android.R.drawable.btn_minus).setContentTitle(MSG_DOWNLOADING);

        // Gets an instance of the NotificationManager service
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        updateNotification("", Notification.FLAG_NO_CLEAR, false, 0);
    }

    DecodeThread decodeThread;

    /**
     * Method to start download using retrofit
     *
     * @param fileName Name of file to be downloaded
     */
    public void startRetrofitDownload(final String fileName, final String sdCardLocation) {

        client.get("http://10.10.11.112:8000/" + fileName, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                decodeThread = new DecodeThread(response, handler, fileName, sdCardLocation);
                decodeThread.start();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.e("status code", String.valueOf(statusCode));
                PendingIntent pIntent = PendingIntent.getService(LoopJDownloadService.this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
                String message;
                if (statusCode == 404) {
                    message = "File Not Found";
                } else {
                    message = MSG_DOWNLOAD_FAILED;
                }

                notificationBuilder.addAction(android.R.drawable.sym_action_chat, MSG_RETRY, pIntent);
                notificationBuilder.setContentIntent(pIntent);

                updateNotification(message, 0, false, 0);

                if (decodeThread != null) {
                    decodeThread.downloadCompete = false;
                }
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }

            @Override
            public void onCancel() {
                super.onCancel();
                Log.e("canceled", fileName);
            }


            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int progress = (int) ((100 * bytesWritten) / totalSize);
                if (progress % PROGRESS_UPDATE_INTERVAL == 0) {
                    if (!progressList.contains(progress)) {
                        progressList.add(progress);

                        updateNotification(MSG_DOWNLOADING + " " + currentDownload + " of " + totalQueueItems, Notification.FLAG_NO_CLEAR, true, progress);
                    }
                }
            }
        });

    }

    /**
     * Method to check if there is any download last left
     */
    public void checkDownloadQueue() {
        if (downloadQueue.size() != 0) {
            startRetrofitDownload(downloadQueue.get(HEAD_POSITION).getFilename(), downloadQueue.get(HEAD_POSITION).getSdCardLocation());
            currentDownload++;
            progressList.clear();

            updateNotification(MSG_DOWNLOADING + " " + currentDownload + " of " + totalQueueItems, Notification.FLAG_NO_CLEAR, true, 0);
        } else {
            updateNotification(MSG_DOWNLOAD_SUCCESSFUL, 0, false, 0);
        }
    }

    /**
     * Method to check for available disk space in MB
     *
     * @return available disk size
     */
    private long checkDiskSize() {
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        return (availableSpace / SIZE_MB);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    @Override
//    public void update(long bytesRead, long contentLength, boolean done, boolean connectionTimeOut) {
//        if (!connectionTimeOut) {
//            Log.e("content length", String.valueOf(contentLength));
//            int progress = (int) ((100 * bytesRead) / contentLength);
//            if (progress % PROGRESS_UPDATE_INTERVAL == 0) {
//                if (!progressList.contains(progress)) {
//                    progressList.add(progress);
//
//                    updateNotification(MSG_DOWNLOADING + " " + currentDownload + " of " + totalQueueItems, Notification.FLAG_NO_CLEAR, true, progress);
//                }
//            }
//        } else {
//            this.connectionTimeOut = connectionTimeOut;
//            PendingIntent pIntent = PendingIntent.getService(LoopJDownloadService.this, 0, intent, 0);
//            notificationBuilder.addAction(android.R.drawable.sym_action_chat, MSG_RETRY, pIntent);
//            notificationBuilder.setContentIntent(pIntent);
//
//            updateNotification(MSG_DOWNLOAD_FAILED, 0, false, 0);
//        }
//    }

    /**
     * Method to update notification
     *
     * @param contentText
     * @param notificationFlag
     * @param showProgress
     * @param progress
     */
    public void updateNotification(String contentText, int notificationFlag, boolean showProgress, int progress) {
        notificationBuilder.setContentText(contentText);

        if (showProgress) {
            notificationBuilder.setProgress(NOTIFICATION_PROGRESS_UPPER_BOUND, progress, false);
        } else {
            notificationBuilder.setProgress(0, 0, false);
        }

        Notification notification = notificationBuilder.build();
        if (notificationFlag != 0) {
            notification.flags = notificationFlag;
        }

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
