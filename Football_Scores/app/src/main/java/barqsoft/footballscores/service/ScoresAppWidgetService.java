package barqsoft.footballscores.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;

public class ScoresAppWidgetService extends RemoteViewsService {

    private static final long MS_PER_DAY = 24 /* h */ * 60 /* min */ * 60 /* sec */ * 1000 /* msec */;

    public static Intent createUpdateIntent(Context context, int appWidgetId) {
        Intent result = new Intent(context, ScoresAppWidgetService.class);
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        result.setData(Uri.parse(result.toUri(Intent.URI_INTENT_SCHEME)));

        return result;
    }

    public ScoresAppWidgetService() {
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ScoresAppWidgetRemoteViewsFactory(this);
    }

    static class ScoresAppWidgetRemoteViewsFactory implements RemoteViewsFactory {

        private Context context;
        private List<FixtureItem> items = new ArrayList<>();

        public ScoresAppWidgetRemoteViewsFactory(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            //this is needed to break out of the restricted onBind call and be allowed to query the data provider
            //the identity will be restored at the end of this method call
            final long token = Binder.clearCallingIdentity();

            try {
                items.clear();

                addScoresWithDayOffset(context, -1);
                addScoresWithDayOffset(context, 0);
                addScoresWithDayOffset(context, 1);

            }
            finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        private void addScoresWithDayOffset(Context context, int offset) {
            items.add(createHeadlineItem(context, offset));
            Cursor cursor = context
                    .getContentResolver()
                    .query(
                            DatabaseContract.scores_table.buildScoreWithDate(),
                            null,
                            null,
                            getDateStringRelativeTodaySelection(offset),
                            null
                    );
            while (cursor.moveToNext()) {
                addItem(offset, cursor);
            }
            cursor.close();
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if(items.size() <= position) {
                return null;
            }
            FixtureItem item = items.get(position);

            RemoteViews result;

            if(!item.isHeadline()) {
                result = createFixtureView(context, item);
            } else {
                result = createDayHeadlineView(context, item);
            }

            return result;
        }

        private RemoteViews createDayHeadlineView(Context context, FixtureItem item) {
            RemoteViews result = new RemoteViews(context.getPackageName(), R.layout.scores_app_widget_headlines);

            result.setTextViewText(R.id.scores_app_widget_headline_text, item.getHeadline());
            result.setTextColor(R.id.scores_app_widget_headline_text, Color.BLACK);

            Intent fillIntent = MainActivity.createFillIntent(item.getDayOffset());

            result.setOnClickFillInIntent(R.id.scores_app_widget_headline_frame, fillIntent);

            return result;
        }

        private RemoteViews createFixtureView(Context context, FixtureItem item) {
            RemoteViews result = new RemoteViews(context.getPackageName(), R.layout.scores_list_item);

            result.setTextViewText(R.id.scores_list_item_home_name, item.getHomeName());
            result.setTextColor(R.id.scores_list_item_home_name, Color.BLACK);
            result.setTextViewText(R.id.scores_list_item_away_name, item.getAwayName());
            result.setTextColor(R.id.scores_list_item_away_name, Color.BLACK);
            result.setTextViewText(R.id.scores_list_item_data_textview, item.getDate());
            result.setTextColor(R.id.scores_list_item_data_textview, Color.BLACK);
            result.setTextViewText(R.id.scores_list_item_score_textview, item.getScore());
            result.setTextColor(R.id.scores_list_item_score_textview, Color.BLACK);

            result.setImageViewResource(R.id.scores_list_item_home_crest, item.getHomeCrestImageId());
            result.setImageViewResource(R.id.scores_list_item_away_crest, item.getAwayCrestImageId());

            Intent fillIntent = MainActivity.createFillIntent(item.getDayOffset());

            result.setOnClickFillInIntent(R.id.scores_list_item_frame, fillIntent);

            return result;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        private String[] getDateStringRelativeTodaySelection(int daysOffset) {
            String[] result = new String[1];
            result[0] = getDateStringRelativeToday(daysOffset);
            return result;
        }

        private String getDateStringRelativeToday(int daysOffset) {
            Date date = new Date(System.currentTimeMillis() + daysOffset * MS_PER_DAY);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return format.format(date);

        }

        private FixtureItem createHeadlineItem(Context context, int offset) {
            Date date = new Date(System.currentTimeMillis() + offset * MS_PER_DAY);

            String dayName = Utilities.getDayName(context, date.getTime());

            return new FixtureItem(offset, dayName);
        }

        private void addItem(int dayOffset, Cursor cursor) {

            String homeName = cursor.getString(DatabaseContract.Column.Home.getTableIndex());
            String awayName = cursor.getString(DatabaseContract.Column.Away.getTableIndex());
            String date = cursor.getString(DatabaseContract.Column.MatchTime.getTableIndex());
            String score = Utilities.getScores(cursor.getInt(DatabaseContract.Column.HomeGoals.getTableIndex()), cursor.getInt(DatabaseContract.Column.AwayGoals.getTableIndex()));
            int homeCrestId = Utilities.getTeamCrestByTeamName(cursor.getString(DatabaseContract.Column.Home.getTableIndex()));
            int awayCrestId = Utilities.getTeamCrestByTeamName(cursor.getString(DatabaseContract.Column.Away.getTableIndex()));

            items.add(new FixtureItem(dayOffset, homeName, awayName, date, score, homeCrestId, awayCrestId));
        }
    }

    static class FixtureItem {
        private int dayOffset;
        private String homeName;
        private String awayName;
        private String date;
        private String score;
        private int homeCrestImageId;
        private int awayCrestImageId;

        private String headline;
        private boolean isHeadline;

        public FixtureItem(int dayOffset, String homeName, String awayName, String date, String score, int homeCrestImageId, int awayCrestImageId) {
            this.dayOffset = dayOffset;
            this.homeName = homeName;
            this.awayName = awayName;
            this.date = date;
            this.score = score;
            this.homeCrestImageId = homeCrestImageId;
            this.awayCrestImageId = awayCrestImageId;
            this.headline = "";
            this.isHeadline = false;
        }

        public FixtureItem(int dayOffset, String headline) {
            this.dayOffset = dayOffset;
            this.headline = headline;
            this.isHeadline = true;
        }

        public int getDayOffset() {
            return dayOffset;
        }

        public String getHomeName() {
            return homeName;
        }

        public String getAwayName() {
            return awayName;
        }

        public String getDate() {
            return date;
        }

        public String getScore() {
            return score;
        }

        public int getHomeCrestImageId() {
            return homeCrestImageId;
        }

        public int getAwayCrestImageId() {
            return awayCrestImageId;
        }

        public boolean isHeadline() {
            return isHeadline;
        }

        public String getHeadline() {
            return headline;
        }
    }
}
