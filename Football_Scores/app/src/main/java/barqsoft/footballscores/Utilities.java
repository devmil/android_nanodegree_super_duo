package barqsoft.footballscores;

import android.content.Context;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utilities
{
    public static final long MS_PER_DAY = 24 /* h */ * 60 /* min */ * 60 /* sec */ * 1000 /* msec */;

    public static final int SERIE_A = 357;
    public static final int PREMIER_LEGAUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;

    public static String getLeague(Context context, int league_num)
    {
        switch (league_num)
        {
            case SERIE_A : return context.getString(R.string.league_serie_a);
            case PREMIER_LEGAUE : context.getString(R.string.league_premier_league);
            case CHAMPIONS_LEAGUE : return context.getString(R.string.league_uefa);
            case PRIMERA_DIVISION : return context.getString(R.string.league_primera_division);
            case BUNDESLIGA : return context.getString(R.string.league_Bundesliga);
            default: return context.getString(R.string.league_unknown);
        }
    }
    public static String getMatchDay(Context context, int match_day,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return context.getString(R.string.matchday_staging);
            }
            else if(match_day == 7 || match_day == 8)
            {
                return context.getString(R.string.matchday_knockout);
            }
            else if(match_day == 9 || match_day == 10)
            {
                return context.getString(R.string.matchday_quaterfinal);
            }
            else if(match_day == 11 || match_day == 12)
            {
                return context.getString(R.string.matchday_semifinal);
            }
            else
            {
                return context.getString(R.string.matchday_final);
            }
        }
        else
        {
            return context.getString(R.string.matchday_matchday) + " : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName (String teamname)
    {
        if (teamname==null){return R.drawable.no_icon;}
        switch (teamname)
        {
            case "Arsenal London FC" : return R.drawable.arsenal;
            case "Manchester United FC" : return R.drawable.manchester_united;
            case "Swansea City" : return R.drawable.swansea_city_afc;
            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC" : return R.drawable.everton_fc_logo1;
            case "West Ham United FC" : return R.drawable.west_ham;
            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC" : return R.drawable.sunderland;
            case "Stoke City FC" : return R.drawable.stoke_city;
            default: return R.drawable.no_icon;
        }
    }

    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        }
        else if ( julianDay == currentJulianDay -1)
        {
            return context.getString(R.string.yesterday);
        }
        else
        {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
            return dayFormat.format(dateInMillis);
        }
    }

    public static long getMillisecondsDayOffset(int dayOffset) {
        return System.currentTimeMillis() + dayOffset * MS_PER_DAY;
    }
}
