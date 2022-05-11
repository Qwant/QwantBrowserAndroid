package org.mozilla.reference.browser.assist;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.app.PendingIntent;
import android.widget.RemoteViews;

import org.mozilla.reference.browser.R;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.assist_initial_layout);

        final Intent intent = new Intent(context, Assist.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.custom_notification_widget_layout, pendingIntent);

        ComponentName watchWidget = new ComponentName(context, WidgetProvider.class);
        appWidgetManager.updateAppWidget(watchWidget, views);
    }
}