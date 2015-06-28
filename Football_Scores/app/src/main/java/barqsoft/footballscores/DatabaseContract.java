package barqsoft.footballscores;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseContract
{
    public static final String SCORES_TABLE = "scores_table";
    public static final class scores_table implements BaseColumns
    {

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        public static Uri buildScoreWithLeague()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
        }
        public static Uri buildScoreWithId()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("id").build();
        }
        public static Uri buildScoreWithDate()
        {
            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
        }
    }
    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    public static final String PATH = "scores";
    public static Uri BASE_CONTENT_URI = Uri.parse("content://"+CONTENT_AUTHORITY);

    public enum Column {

        Date        (1, "date"      ),
        MatchTime   (2, "time"      ),
        Home        (3, "home"      ),
        Away        (4, "away"      ),
        League      (5, "league"    ),
        HomeGoals   (6, "home_goals"),
        AwayGoals   (7, "away_goals"),
        Id          (8, "match_id"  ),
        MatchDay    (9, "match_day" );

        private int indexDefaultProjection;
        private String name;

        Column(int indexDefaultProjection, String name) {
            this.indexDefaultProjection = indexDefaultProjection;
            this.name = name;
        }

        public int getTableIndex() {
            return indexDefaultProjection;
        }

        public String getColumnName() {
            return name;
        }

        @Override
        public String toString() {
            return getColumnName();
        }
    }
}
