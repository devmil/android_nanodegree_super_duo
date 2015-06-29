package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import barqsoft.footballscores.service.ScoresAppWidgetService;

/**
 * Implementation of App Widget functionality.
 */
public class ScoresAppWidget extends AppWidgetProvider {

    public static void notifyDataChanged(Context context) {

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        int[] widgetIds = widgetManager.getAppWidgetIds(new ComponentName(context, ScoresAppWidget.class));
        widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.layout.scores_list_item);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }


    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Intent intent = ScoresAppWidgetService.createUpdateIntent(context, appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_app_widget);
        views.setRemoteAdapter(R.id.scores_app_widget_list, intent);
        views.setEmptyView(R.id.scores_app_widget_list, R.id.scores_app_widget_empty_text);

        Intent launchIntent = MainActivity.createLaunchIntent(context);
        PendingIntent launchPendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        views.setPendingIntentTemplate(R.id.scores_app_widget_list, launchPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

