package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ScoresAdapter extends CursorAdapter
{

    private double detail_match_id = 0;

    @SuppressWarnings("FieldCanBeLocal")
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    public ScoresAdapter(Context context, Cursor cursor, int flags)
    {
        super(context,cursor,flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    public void setDetailMatchId(double detailMatchId) {
        if(detailMatchId != detail_match_id) {
            detail_match_id = detailMatchId;
            notifyDataSetChanged();
        }
    }

    public double getDetailMatchId() {
        return detail_match_id;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        mHolder.home_name.setText(cursor.getString(DatabaseContract.Column.Home.getTableIndex()));
        mHolder.away_name.setText(cursor.getString(DatabaseContract.Column.Away.getTableIndex()));
        mHolder.date.setText(cursor.getString(DatabaseContract.Column.MatchTime.getTableIndex()));
        mHolder.score.setText(Utilities.getScores(cursor.getInt(DatabaseContract.Column.HomeGoals.getTableIndex()), cursor.getInt(DatabaseContract.Column.AwayGoals.getTableIndex())));
        mHolder.match_id = cursor.getDouble(DatabaseContract.Column.Id.getTableIndex());
        mHolder.home_crest.setImageResource(Utilities.getTeamCrestByTeamName(
                cursor.getString(DatabaseContract.Column.Home.getTableIndex())));
        mHolder.away_crest.setImageResource(Utilities.getTeamCrestByTeamName(
                cursor.getString(DatabaseContract.Column.Away.getTableIndex())
        ));
        //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(mHolder.match_id == detail_match_id)
        {
            View v = vi.inflate(R.layout.detail_fragment, mHolder.container, false);
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

            mHolder.container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.detail_fragment_matchday_textview);
            match_day.setText(Utilities.getMatchDay(context, cursor.getInt(DatabaseContract.Column.MatchDay.getTableIndex()),
                    cursor.getInt(DatabaseContract.Column.League.getTableIndex())));
            TextView league = (TextView) v.findViewById(R.id.detail_fragment_league_textview);
            league.setText(Utilities.getLeague(context, cursor.getInt(DatabaseContract.Column.League.getTableIndex())));
            Button share_button = (Button) v.findViewById(R.id.detail_fragment_share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.home_name.getText()+" "
                    +mHolder.score.getText()+" "+mHolder.away_name.getText() + " "));
                }
            });
        }
        else
        {
            mHolder.container.removeAllViews();
        }

    }

    @SuppressWarnings("deprecation")
    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
