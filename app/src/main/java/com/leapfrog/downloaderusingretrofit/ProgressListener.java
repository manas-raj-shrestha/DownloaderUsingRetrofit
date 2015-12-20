package com.leapfrog.downloaderusingretrofit;

interface ProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}
