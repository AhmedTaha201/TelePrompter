package com.example.teleprompter.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

//Resources
//https://developer.android.com/training/data-storage/files#java
//https://stackoverflow.com/questions/14768191/how-do-i-read-the-file-content-from-the-internal-storage-android-app

/* A non-static helper class to read and write files in the internal storage */
public class FileUtils {


    public static class WriteFileTask extends AsyncTask<String, Void, Boolean> {

        private boolean mWrittenSuccessfully = false;

        private Context mContext;

        public WriteFileTask(Context mContext) {
            this.mContext = mContext;
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            String name = strings[0];
            String contents = strings[1];

            FileOutputStream outputStream;
            try {
                outputStream = mContext.openFileOutput(name, Context.MODE_PRIVATE);
                outputStream.write(contents.getBytes());
                outputStream.close();
                mWrittenSuccessfully = true;
                Timber.d("The file %s was written successfully", name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Timber.e("File not found to write");
            } catch (IOException e) {
                Timber.e("failed to write the file");
                e.printStackTrace();
            }
            return mWrittenSuccessfully;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }

}