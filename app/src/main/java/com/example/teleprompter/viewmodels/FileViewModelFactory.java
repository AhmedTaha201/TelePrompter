package com.example.teleprompter.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.teleprompter.database.FileDatabase;

public class FileViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private FileDatabase mDb;
    private String mName;

    public FileViewModelFactory(FileDatabase mDb, String mName) {
        this.mDb = mDb;
        this.mName = mName;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new FileViewModel(mDb, mName);
    }
}
