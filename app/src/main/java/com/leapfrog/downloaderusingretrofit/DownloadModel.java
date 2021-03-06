package com.leapfrog.downloaderusingretrofit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Manas on 12/18/2015.
 */
public class DownloadModel implements Parcelable {

    private String displayMessage;
    private String filename;
    private String sdCardLocation;

    protected DownloadModel(Parcel in) {
        displayMessage = in.readString();
        filename = in.readString();
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String url) {
        this.filename = url;
    }

    public String getSdCardLocation() {
        return sdCardLocation;
    }

    public void setSdCardLocation(String sdCardLocation) {
        this.sdCardLocation = sdCardLocation;
    }

    /**
     * Method to set object
     *
     * @param displayMessage Message to be displayed in notification
     * @param url            Filename
     * @param sdCardLocation Location for file storage
     */
    public DownloadModel(String displayMessage, String url, String sdCardLocation) {
        this.displayMessage = displayMessage;
        this.filename = url;
        this.sdCardLocation = sdCardLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(displayMessage);
        parcel.writeString(filename);
        parcel.writeString(sdCardLocation);
    }

}
