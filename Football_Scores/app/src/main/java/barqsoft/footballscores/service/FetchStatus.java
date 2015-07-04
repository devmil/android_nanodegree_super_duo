package barqsoft.footballscores.service;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This enumeration describes the fetch status for the football results
 */
public interface FetchStatus {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ UNKNOWN, OK, NETWORK_ERROR, PROTOCOL_ERROR })
    @interface Values {}

    int UNKNOWN = 0;
    int OK = 1;
    int NETWORK_ERROR = -1;
    int PROTOCOL_ERROR = -2;
}
