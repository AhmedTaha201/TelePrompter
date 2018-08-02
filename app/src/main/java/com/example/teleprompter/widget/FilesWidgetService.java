package com.example.teleprompter.widget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.teleprompter.EditActivity;
import com.example.teleprompter.MainFragment;
import com.example.teleprompter.R;
import com.example.teleprompter.database.File;
import com.example.teleprompter.database.FileDatabase;

import java.util.List;

import timber.log.Timber;

public class FilesWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FilesWidgetRemoteViewsService(getApplicationContext());
    }

    public class FilesWidgetRemoteViewsService implements RemoteViewsService.RemoteViewsFactory {

        Context mContext;
        List<File> mFilesList;

        public FilesWidgetRemoteViewsService(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            Timber.i("DataSet changed");
            FileDatabase db = FileDatabase.getInstance(mContext);
            mFilesList = db.fileDao().loadAllFilesForWidgets();
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mFilesList != null ? mFilesList.size() : 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.files_widget_list_item);

            String fileName = mFilesList.get(position).getFileName();
            views.setTextViewText(R.id.files_widget_list_item_name, fileName);

            Intent editFillIntent = new Intent(mContext, EditActivity.class);
            editFillIntent.putExtra(MainFragment.INTENT_EXTRA_FILE_NAME, fileName);
            views.setOnClickFillInIntent(R.id.files_widget_list_item_name, editFillIntent);

            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }

}
