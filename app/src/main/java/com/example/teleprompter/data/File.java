package com.example.teleprompter.data;

public class File {

    private String mFileName;
    private String mFileContents;

    public File(String mFileName, String mFileContents) {
        this.mFileName = mFileName;
        this.mFileContents = mFileContents;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public String getFileContents() {
        return mFileContents;
    }

    public void setFileContents(String mFileContents) {
        this.mFileContents = mFileContents;
    }
}
