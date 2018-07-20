package com.example.teleprompter.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.teleprompter.database.File;
import com.example.teleprompter.database.FileDatabase;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<File>> mFileList;

    public MainViewModel(@NonNull Application application) {
        super(application);
        FileDatabase database = FileDatabase.getInstance(this.getApplication());
        mFileList = database.fileDao().loadAllFiles();
    }

    public LiveData<List<File>> getFileList() {
        return mFileList;
    }
}
