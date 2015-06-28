package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.scores_table;

public class ScoresDBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 2;
    public ScoresDBHelper(Context context)
    {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        final String CreateScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + scores_table._ID                  + " INTEGER PRIMARY KEY,"
                + DatabaseContract.Column.Date      + " TEXT NOT NULL,"
                + DatabaseContract.Column.MatchTime + " INTEGER NOT NULL,"
                + DatabaseContract.Column.Home      + " TEXT NOT NULL,"
                + DatabaseContract.Column.Away      + " TEXT NOT NULL,"
                + DatabaseContract.Column.League    + " INTEGER NOT NULL,"
                + DatabaseContract.Column.HomeGoals + " TEXT NOT NULL,"
                + DatabaseContract.Column.AwayGoals + " TEXT NOT NULL,"
                + DatabaseContract.Column.Id        + " INTEGER NOT NULL,"
                + DatabaseContract.Column.MatchDay  + " INTEGER NOT NULL,"
                + " UNIQUE ("+DatabaseContract.Column.Id+") ON CONFLICT REPLACE"
                + " );";
        db.execSQL(CreateScoresTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
    }
}
