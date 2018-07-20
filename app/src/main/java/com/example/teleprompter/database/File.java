package com.example.teleprompter.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "files")
public class File {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int mId;

    @ColumnInfo(name = "file_name")
    private String mFileName;

    @ColumnInfo(name = "contents")
    private String mFileContents;

    @ColumnInfo(name = "date")
    private Date mDate;

    public File(String mFileName, String mFileContents, Date mDate) {
        this.mFileName = mFileName;
        this.mFileContents = mFileContents;
        this.mDate = mDate;
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

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public Date getDate() {
        return mDate;

    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

}
