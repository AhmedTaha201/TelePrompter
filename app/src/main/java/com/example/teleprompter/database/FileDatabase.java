package com.example.teleprompter.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;


@Database(entities = {File.class}, version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class FileDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "files";
    private static final Object LOCK = new Object();

    private static FileDatabase sInstance;

    public static FileDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(), FileDatabase.class, DATABASE_NAME)
                        .allowMainThreadQueries()
                        .build();
            }
        }
        return sInstance;
    }

    public abstract FileDao fileDao();
}
