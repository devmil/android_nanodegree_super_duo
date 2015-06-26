package it.jaschke.alexandria.events;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BookFetchingProgressEvent {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ACTION_STARTED, ACTION_FINISHED})
    public @interface Actions {}

    public static final int ACTION_STARTED = 1;
    public static final int ACTION_FINISHED = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ RESULT_OK, RESULT_NOT_FOUND, RESULT_ERROR_CONNECTION, RESULT_ERROR_BAD_FORMAT })
    public @interface Results {}

    public static final int RESULT_OK = 0;
    public static final int RESULT_NOT_FOUND = 1;
    public static final int RESULT_ERROR_CONNECTION = -11;
    public static final int RESULT_ERROR_BAD_FORMAT = -12;

    public BookFetchingProgressEvent(String ean, @Actions int action, @Results int result) {
        this.ean = ean;
        this.action = action;
        this.result = result;
    }

    private String ean;
    @Actions
    private int action;
    @Results
    private int result;

    public String getEan() {
        return ean;
    }

    public int getAction() {
        return action;
    }

    public @Results int getResult() {
        return result;
    }
}
