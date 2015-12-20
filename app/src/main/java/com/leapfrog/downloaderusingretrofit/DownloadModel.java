package com.leapfrog.downloaderusingretrofit;

import java.util.ArrayList;

/**
 * Created by Manas on 12/18/2015.
 */
public class DownloadModel {

    private String displayMessage;
    private ArrayList<String> urls;
    private String sdCardLocation;

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }

    public String getSdCardLocation() {
        return sdCardLocation;
    }

    public void setSdCardLocation(String sdCardLocation) {
        this.sdCardLocation = sdCardLocation;
    }
}
