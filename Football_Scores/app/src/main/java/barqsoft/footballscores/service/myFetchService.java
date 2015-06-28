package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import retrofit.RestAdapter;

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
        String league;
        String date;
        String time;
        String home;
        String away;
        String homeGoals;
        String awayGoals;
        String matchId;
        String matchDay;

        SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss", Locale.US);
        match_date.setTimeZone(TimeZone.getTimeZone("UTC"));


        List<FootballDataOrgService.Fixture> fixtures = data.fixtures;
        if(fixtures == null) {
            fixtures = new ArrayList<>();
        }

        //ContentValues to be inserted
        Vector<ContentValues> values = new Vector<>(fixtures.size());
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

                matchId = fixture._links.self.href;
                matchId = matchId.replace(MATCH_LINK, "");
                if(!isReal){
                    //This if statement changes the match ID of the dummy data so that it all goes into the database
                    matchId=matchId+Integer.toString(i);
                }

                date = fixture.date;
                time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                date = date.substring(0,date.indexOf("T"));

                try {
                    Date parseddate = match_date.parse(date+time);
                    SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm", Locale.US);
                    new_date.setTimeZone(TimeZone.getDefault());
                    date = new_date.format(parseddate);
                    time = date.substring(date.indexOf(":") + 1);
                    date = date.substring(0,date.indexOf(":"));

                    if(!isReal){
                        //This if statement changes the dummy data's date to match our current date range.
                        Date fragmentdate = new Date(System.currentTimeMillis()+((i-2)*86400000));
                        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                        date=mformat.format(fragmentdate);
                    }
                }
                catch (Exception e)
                {
                    Log.d(LOG_TAG, "error here!");
                    Log.e(LOG_TAG,e.getMessage());
                }

                home = fixture.homeTeamName;
                away = fixture.awayTeamName;

                if(fixture.result == null) {
                    continue;
                }

                homeGoals = Integer.toString(fixture.result.goalsHomeTeam);
                awayGoals = Integer.toString(fixture.result.goalsAwayTeam);
                matchDay = Integer.toString(fixture.matchday);

                ContentValues match_values = new ContentValues();
                match_values.put(DatabaseContract.Column.Id.getColumnName() ,matchId);
                match_values.put(DatabaseContract.Column.Date.getColumnName(), date);
                match_values.put(DatabaseContract.Column.MatchTime.getColumnName(), time);
                match_values.put(DatabaseContract.Column.Home.getColumnName(), home);
                match_values.put(DatabaseContract.Column.Away.getColumnName(), away);
                match_values.put(DatabaseContract.Column.HomeGoals.getColumnName(), homeGoals);
                match_values.put(DatabaseContract.Column.AwayGoals.getColumnName(), awayGoals);
                match_values.put(DatabaseContract.Column.League.getColumnName(), league);
                match_values.put(DatabaseContract.Column.MatchDay.getColumnName(), matchDay);

                values.add(match_values);
            }
        }
        ContentValues[] insert_data = new ContentValues[values.size()];
        values.toArray(insert_data);
        getContentResolver().bulkInsert(
                DatabaseContract.BASE_CONTENT_URI, insert_data);
    }
}

