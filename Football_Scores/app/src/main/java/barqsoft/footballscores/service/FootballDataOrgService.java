package barqsoft.footballscores.service;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

public interface FootballDataOrgService {

    class TimeFrame {
        private boolean future;
        private int days;

        public TimeFrame(boolean future, int days) {
            this.future = future;
            this.days = days;
        }

        @Override
        public String toString() {
            return String.format("%s%d", future ? "n" : "p", days);
        }
    }

    @GET("/alpha/fixtures")
    TimeFrameResult queryTimeFrame(@Query("timeFrame") TimeFrame timeFrame, @Header("X-Auth-Token") String authToken);

    class TimeFrameResult {
        public String timeFrameStart;
        public String timeFrameEnd;
        public long count;
        public List<Fixture> fixtures;
    }

    class Fixture {
        public Links _links;
        public String date;
        public String status;
        public int matchday;
        public String homeTeamName;
        public String awayTeamName;
        public Result result;
    }

    class Links {
        public Link self;
        public Link soccerseason;
        public Link homeTeam;
        public Link awayTeam;
    }

    class Link {
        String href;
    }

    class Result {
        public int goalsHomeTeam;
        public int goalsAwayTeam;
    }
}
