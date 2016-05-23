package com.leapfrog.downloaderusingretrofit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnGetFile;
    ArrayList<DownloadModel> downloadModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetFile = (Button) findViewById(R.id.btn_get_file);

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

    /**
     * Method to add dummy data
     */
    public void createDummyList() {
        downloadModels.clear();
        downloadModels.add(new DownloadModel("short clip", "java1.mp4", "/appdata"));
        downloadModels.add(new DownloadModel("short clip", "java2.mp4", "/appdata"));
    }

}
