package barqsoft.footballscores.service.events;

import barqsoft.footballscores.service.FetchStatus;

/**
 * this event gets fired whenever a fetch is done
 */
public class FetchStatusChangedEvent {

    private @FetchStatus.Values int status;

    public FetchStatusChangedEvent(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
