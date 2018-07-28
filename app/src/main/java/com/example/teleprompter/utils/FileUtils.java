package com.example.teleprompter.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.teleprompter.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static String createVideoFile(Context context) throws IOException {
        //Create video directory
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File videoDir = new File(dcimDir, context.getString(R.string.app_name));
        if (!videoDir.exists()) {
            if (!videoDir.mkdirs()) {
                throw new IOException("Failed to make the videos folder");
            }
        }

        //Create a new video file
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prefix = "Video_" + timeStamp + "_";
        try {
            return File.createTempFile(prefix, ".mp4", videoDir).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Timber.e("Failed to create a new video file");
        }
        return null;
    }

}