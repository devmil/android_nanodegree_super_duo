package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import retrofit.RestAdapter;

/**
 * Created by yehya khaled on 3/2/2015.
 */
public class myFetchService extends IntentService
{
    public static final String LOG_TAG = "myFetchService";
    public myFetchService()
    {
        super("myFetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        getData(new FootballDataOrgService.TimeFrame(true, 2));
        getData(new FootballDataOrgService.TimeFrame(false, 2));

        return;
    }

    private void getData (FootballDataOrgService.TimeFrame timeFrame)
    {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("http://api.football-data.org")
                .build();

        FootballDataOrgService fbService = adapter.create(FootballDataOrgService.class);

        FootballDataOrgService.TimeFrameResult fbResult = null;

        try {
            fbResult = fbService.queryTimeFrame(timeFrame, "e136b7858d424b9da07c88f28b61989a");
        }
        catch (Exception e) {
            Log.e(LOG_TAG,"Exception here" + e.getMessage());
        }

        boolean isFake = false;

        if(fbResult == null
                || fbResult.count <= 0) {
            fbResult = getFakeResult();
            isFake = true;
        }

        processFootballData(fbResult, !isFake);
    }

    private FootballDataOrgService.TimeFrameResult getFakeResult() {

        try {

            FootballDataOrgService.TimeFrameResult result = new FootballDataOrgService.TimeFrameResult();
            result.timeFrameStart = "2015-06-18";
            result.timeFrameEnd = "2015-06-19";
            result.count = 1;
            result.fixtures = new ArrayList<>();

            FootballDataOrgService.Fixture f = new FootballDataOrgService.Fixture();
            FootballDataOrgService.Links ls = new FootballDataOrgService.Links();
            ls.self = new FootballDataOrgService.Link();
            ls.self.href = "http://api.football-data.org/alpha/fixtures/140514";
            ls.soccerseason = new FootballDataOrgService.Link();
            ls.soccerseason.href = "http://api.football-data.org/alpha/soccerseasons/357";
            ls.homeTeam = new FootballDataOrgService.Link();
            ls.homeTeam.href = "http://api.football-data.org/alpha/teams/275";
            ls.awayTeam = new FootballDataOrgService.Link();
            ls.awayTeam.href = "http://api.football-data.org/alpha/teams/263";
            f._links = ls;
            f.date = "2015-06-19T22:00:00Z";
            f.status = "Finished";
            f.matchday = 42;
            f.homeTeamName = "TESTING1";
            f.awayTeamName = "TESTING2";
            f.result = new FootballDataOrgService.Result();
            f.result.goalsHomeTeam = 2;
            f.result.goalsAwayTeam = 4;

            //add 5 copies
            for(int i=0; i<5; i++) {
                result.fixtures.add(f);
            }

            return result;
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Error generating test data", e);
        }
        return null;
    }

    private void processFootballData (FootballDataOrgService.TimeFrameResult data, boolean isReal)
    {
        //JSON data
        final String SERIE_A = "357";
        final String PREMIER_LEGAUE = "354";
        final String CHAMPIONS_LEAGUE = "362";
        final String PRIMERA_DIVISION = "358";
        final String BUNDESLIGA = "351";
        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";

        //Match data
        String league = null;
        String mDate = null;
        String mTime = null;
        String Home = null;
        String Away = null;
        String Home_goals = null;
        String Away_goals = null;
        String match_id = null;
        String match_day = null;

        SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
        match_date.setTimeZone(TimeZone.getTimeZone("UTC"));


        List<FootballDataOrgService.Fixture> fixtures = data.fixtures;
        if(fixtures == null) {
            fixtures = new ArrayList<>();
        }

        //ContentValues to be inserted
        Vector<ContentValues> values = new Vector <ContentValues> (fixtures.size());
        for(int i = 0;i < fixtures.size();i++)
        {
            FootballDataOrgService.Fixture fixture = fixtures.get(i);

            if(fixture._links == null
                    || fixture._links.soccerseason == null) {
                continue;
            }

            league = fixture._links.soccerseason.href;

            league = league.replace(SEASON_LINK,"");
            if(     league.equals(PREMIER_LEGAUE)      ||
                    league.equals(SERIE_A)             ||
                    league.equals(CHAMPIONS_LEAGUE)    ||
                    league.equals(BUNDESLIGA)          ||
                    league.equals(PRIMERA_DIVISION)     )
            {
                if(fixture._links.self == null) {
                    continue;
                }

                match_id = fixture._links.self.href;
                match_id = match_id.replace(MATCH_LINK, "");
                if(!isReal){
                    //This if statement changes the match ID of the dummy data so that it all goes into the database
                    match_id=match_id+Integer.toString(i);
                }

                mDate = fixture.date;
                mTime = mDate.substring(mDate.indexOf("T") + 1, mDate.indexOf("Z"));
                mDate = mDate.substring(0,mDate.indexOf("T"));

                try {
                    Date parseddate = match_date.parse(mDate+mTime);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                    new_date.setTimeZone(TimeZone.getDefault());
                    mDate = new_date.format(parseddate);
                    mTime = mDate.substring(mDate.indexOf(":") + 1);
                    mDate = mDate.substring(0,mDate.indexOf(":"));

                    if(!isReal){
                        //This if statement changes the dummy data's date to match our current date range.
                        Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                        mDate=mformat.format(fragmentdate);
                    }
                }
                catch (Exception e)
                {
                    Log.d(LOG_TAG, "error here!");
                    Log.e(LOG_TAG,e.getMessage());
                }

                Home = fixture.homeTeamName;
                Away = fixture.awayTeamName;

                if(fixture.result == null) {
                    continue;
                }

                Home_goals = Integer.toString(fixture.result.goalsHomeTeam);
                Away_goals = Integer.toString(fixture.result.goalsAwayTeam);
                match_day = Integer.toString(fixture.matchday);

                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.scores_table.MATCH_ID,match_id);
                match_values.put(DatabaseContract.scores_table.DATE_COL,mDate);
                match_values.put(DatabaseContract.scores_table.TIME_COL,mTime);
                match_values.put(DatabaseContract.scores_table.HOME_COL,Home);
                match_values.put(DatabaseContract.scores_table.AWAY_COL,Away);
                match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL,Home_goals);
                match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL,Away_goals);
                match_values.put(DatabaseContract.scores_table.LEAGUE_COL,league);
                match_values.put(DatabaseContract.scores_table.MATCH_DAY,match_day);

                values.add(match_values);
            }
        }
        int inserted_data = 0;
        ContentValues[] insert_data = new ContentValues[values.size()];
        values.toArray(insert_data);
        inserted_data = getContentResolver().bulkInsert(
                DatabaseContract.BASE_CONTENT_URI,insert_data);
    }
}

