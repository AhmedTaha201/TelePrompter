package com.example.teleprompter.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.teleprompter.database.File;
import com.example.teleprompter.database.FileDatabase;

public class FileViewModel extends ViewModel {

    private LiveData<File> mFile;

    public FileViewModel(FileDatabase mDb, int mId) {
        mFile = mDb.fileDao().loadFileById(mId);
    }

    public LiveData<File> getmFile() {
        return mFile;
    }
}
