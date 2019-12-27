package com.example.videoplayer;

import android.net.Uri;

/**
 * @author Abhishek Saxena
 * @since 27/12/19 5:40 PM
 */

public class MediaFile {

    private String fileName;
    private Uri fileUri;

    public MediaFile(String fileName, Uri fileUri) {
        this.fileName = fileName;
        this.fileUri = fileUri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }
}
