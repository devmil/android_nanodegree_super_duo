package barqsoft.footballscores;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import barqsoft.footballscores.service.FetchStatus;
import barqsoft.footballscores.service.MyFetchService;
import barqsoft.footballscores.service.events.FetchStatusChangedEvent;
import de.greenrobot.event.EventBus;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String KEY_DETAIL_MATCH_ID = "DETAIL_MATCH_ID";

    private ScoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentDate = new String[1];
    private Settings settings;
    private LinearLayout llNoItems;
    private TextView txtNoItems;

    public MainScreenFragment()
    {
    }

    private void update_scores()
    {
        getActivity().startService(MyFetchService.createCheckUpdateIntent(getActivity()));
    }
    public void setFragmentDate(String date)
    {
        fragmentDate[0] = date;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        update_scores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView score_list = (ListView) rootView.findViewById(R.id.fragment_main_scores_list);
        mAdapter = new ScoresAdapter(getActivity(),null,0);
        score_list.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER,null,this);
        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(KEY_DETAIL_MATCH_ID)) {
                mAdapter.setDetailMatchId(savedInstanceState.getDouble(KEY_DETAIL_MATCH_ID));
            }
        }

        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.setDetailMatchId(selected.match_id);
            }
        });
        llNoItems = (LinearLayout)rootView.findViewById(R.id.fragment_main_scores_no_items);
        txtNoItems = (TextView)rootView.findViewById(R.id.fragment_main_scores_no_items_text);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        settings = new Settings(activity);
        EventBus.getDefault().register(this);
        handleNoItemsVisibilityAndContent();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        settings = null;
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null) {
            outState.putDouble(KEY_DETAIL_MATCH_ID, mAdapter.getDetailMatchId());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(),DatabaseContract.scores_table.buildScoreWithDate(),
                null,null, fragmentDate,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            cursor.moveToNext();
        }
        mAdapter.swapCursor(cursor);
        handleNoItemsVisibilityAndContent();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }

    public void onEventMainThread(FetchStatusChangedEvent e) {
        handleNoItemsVisibilityAndContent();
    }

    private void handleNoItemsVisibilityAndContent() {
        boolean hasData = mAdapter != null && mAdapter.getCount() > 0;
        @FetchStatus.Values
        int fetchStatus = FetchStatus.UNKNOWN;

        Settings s = settings;
        if(s != null) {
            fetchStatus = settings.getLastFetchStatus();
        }

        if(llNoItems != null) {
            llNoItems.setVisibility(hasData ? View.GONE :  View.VISIBLE);
        }
        if(txtNoItems != null) {
            txtNoItems.setText(getStatusText(fetchStatus));
        }
    }

    private CharSequence getStatusText(@FetchStatus.Values int fetchStatus) {
        Context context =  getActivity();
        if(context == null) {
            return "";
        }

        switch(fetchStatus) {
            case FetchStatus.UNKNOWN:
                return context.getResources().getText(R.string.no_items_fetch_status_unknown);
            case FetchStatus.OK:
                return context.getResources().getText(R.string.no_items_fetch_status_ok);
            case FetchStatus.NETWORK_ERROR:
                return context.getResources().getText(R.string.no_items_fetch_status_network_error);
            case FetchStatus.PROTOCOL_ERROR:
                return context.getResources().getText(R.string.no_items_fetch_status_protocol_error);

        }
        return "";
    }
}
