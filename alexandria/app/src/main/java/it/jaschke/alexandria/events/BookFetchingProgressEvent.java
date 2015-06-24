package it.jaschke.alexandria.events;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BookFetchingProgressEvent {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_STARTED, ACTION_FINISHED})
    @interface Actions {}

    public static final int ACTION_STARTED = 1;
    public static final int ACTION_FINISHED = 2;

    public BookFetchingProgressEvent(String ean, @Actions int action, boolean success) {
        this.ean = ean;
        this.action = action;
        this.success = success;
    }

    private String ean;
    @Actions
    private int action;
    private boolean success;

    public String getEan() {
        return ean;
    }

    public int getAction() {
        return action;
    }

    public boolean isSuccess() {
        return success;
    }
}
