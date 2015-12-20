package com.leapfrog.downloaderusingretrofit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Manas on 12/18/2015.
 */
public class DownloadModel implements Parcelable {

    private String displayMessage;
    private String url;
    private String sdCardLocation;

    protected DownloadModel(Parcel in) {
        displayMessage = in.readString();
        url = in.readString();
        sdCardLocation = in.readString();
    }

    public static final Creator<DownloadModel> CREATOR = new Creator<DownloadModel>() {
        @Override
        public DownloadModel createFromParcel(Parcel in) {
            return new DownloadModel(in);
        }

        @Override
        public DownloadModel[] newArray(int size) {
            return new DownloadModel[size];
        }
    };

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getUrls() {
        return url;
    }

    public void setUrls(String url) {
        this.url = url;
    }

    public String getSdCardLocation() {
        return sdCardLocation;
    }

    public void setSdCardLocation(String sdCardLocation) {
        this.sdCardLocation = sdCardLocation;
    }

    public DownloadModel(String displayMessage,String url,String sdCardLocation){
        this.displayMessage = displayMessage;
        this.url = url;
        this.sdCardLocation = sdCardLocation;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(displayMessage);
        parcel.writeString(url);
        parcel.writeString(sdCardLocation);
    }
}
