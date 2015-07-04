package barqsoft.footballscores;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import barqsoft.footballscores.service.FetchStatus;

public class Settings {

    private static final String PREFERENCES_NAME = "settings";

    private static final String PREF_INITIAL_LOADING_DONE = "initialLoadingDone";
    private static final String PREF_LAST_UPDATE = "lastUpdate";

    private static final String PREF_LAST_FETCH_STATUS = "lastFetchStatus";

    private WeakReference<Context> mContext;

    public Settings(Context context) {
        mContext = new WeakReference<>(context);
    }

    public boolean hasInitialLoadingDone() {
        SharedPreferences p = getSharedPreferences();
        return p != null && p.getBoolean(PREF_INITIAL_LOADING_DONE, false);
    }

    public void setInitialLoadingDone(boolean initialLoadingDone) {
        SharedPreferences p = getSharedPreferences();
        if(p != null) {
            p.edit()
                    .putBoolean(PREF_INITIAL_LOADING_DONE, initialLoadingDone)
                    .apply();
        }
    }

    public Date getLastUpdate() {
        SharedPreferences p = getSharedPreferences();
        if(p != null) {
            return new Date(p.getLong(PREF_LAST_UPDATE, 0));
        }
        return new Date(0);
    }

    public void notifyLastUpdateNow() {
        setInitialLoadingDone(true);
        setLastUpdate(Calendar.getInstance().getTime());
    }

    public void setLastUpdate(Date date) {
        SharedPreferences p = getSharedPreferences();
        if(p != null) {
            p.edit()
                    .putLong(PREF_LAST_UPDATE, date.getTime())
                    .apply();
        }
    }

    public @FetchStatus.Values int getLastFetchStatus() {
        SharedPreferences p = getSharedPreferences();
        if(p != null) {
            //noinspection ResourceType
            return p.getInt(PREF_LAST_FETCH_STATUS, FetchStatus.UNKNOWN);
        }
        return FetchStatus.UNKNOWN;
    }

    public void setLastFetchStatus(@FetchStatus.Values int fetchStatus) {
        SharedPreferences p = getSharedPreferences();
        if(p != null) {
            p.edit()
                    .putInt(PREF_LAST_FETCH_STATUS, fetchStatus)
                    .apply();
        }
    }

    @Nullable
    private SharedPreferences getSharedPreferences() {
        Context tmpContext = mContext.get();
        if(tmpContext != null) {
            return tmpContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        return null;
    }
}
