package vfs.com.soccerfan;

import android.provider.BaseColumns;

/**
 * Created by pg05oscar on 07/12/2015.
 */
public final class SoccerFanContract {

    private SoccerFanContract(){}

    // Table with teams
    public  static abstract class Teams implements BaseColumns {
        public static final String TABLE_NAME="Teams";
        public static final String COLUMN_NAME_TEAM_ID = "team_id";
        public static final String COLUMN_NAME_TEAM_NAME = "team_name";
        public static final String COLUMN_NAME_TEAM_URL = "team_url";
        public static final String COLUMN_NAME_CREST_URL = "crest_url";
    }

}
