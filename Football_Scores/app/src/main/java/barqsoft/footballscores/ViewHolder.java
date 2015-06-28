package barqsoft.footballscores;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder
{
    public TextView home_name;
    public TextView away_name;
    public TextView score;
    public TextView date;
    public ImageView home_crest;
    public ImageView away_crest;
    public double match_id;
    public ViewGroup container;

    public ViewHolder(View view)
    {
        home_name = (TextView) view.findViewById(R.id.scores_list_item_home_name);
        away_name = (TextView) view.findViewById(R.id.scores_list_item_away_name);
        score     = (TextView) view.findViewById(R.id.scores_list_item_score_textview);
        date      = (TextView) view.findViewById(R.id.scores_list_item_data_textview);
        home_crest = (ImageView) view.findViewById(R.id.scores_list_item_home_crest);
        away_crest = (ImageView) view.findViewById(R.id.scores_list_item_away_crest);
        container = (ViewGroup) view.findViewById(R.id.scores_list_item_container);
    }
}
