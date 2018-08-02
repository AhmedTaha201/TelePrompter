package com.example.teleprompter.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.teleprompter.EditActivity;
import com.example.teleprompter.R;

public class FilesWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(Context context, AppWidgetManager mgr, int[] widgetIds) {
        for (int id : widgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.files_widget_layout);

            //Populate the list view
            Intent filesListIntent = new Intent(context, FilesWidgetService.class);
            views.setRemoteAdapter(R.id.widget_file_names_list, filesListIntent);
            views.setEmptyView(R.id.widget_file_names_list, R.id.widget_list_empty_view);

            //The PendingIntentTemplate for the listView
            Intent editIntent = new Intent(context, EditActivity.class);
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context).addParentStack(EditActivity.class);
            taskStackBuilder.addNextIntent(editIntent);
            PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_file_names_list, pendingIntent);

            //The pendingIntent for the emptyView to open edit activity
            views.setOnClickPendingIntent(R.id.widget_list_empty_view, pendingIntent);

            //The pendingIntent for the addNew button to open edit activity
            views.setOnClickPendingIntent(R.id.files_widget_add_file, pendingIntent);


            mgr.updateAppWidget(id, views);
        }
    }
}
