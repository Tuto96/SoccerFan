package vfs.com.soccerfan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by pg05oscar on 07/12/2015.
 */
public class SoccerFanDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + SoccerFanContract.Teams.TABLE_NAME + " (" +
            SoccerFanContract.Teams._ID + " INTEGER PRIMARY KEY," + SoccerFanContract.Teams.COLUMN_NAME_TEAM_ID + " TEXT, " +
            SoccerFanContract.Teams.COLUMN_NAME_TEAM_NAME + " TEXT, " + SoccerFanContract.Teams.COLUMN_NAME_TEAM_URL + " TEXT, " +
            SoccerFanContract.Teams.COLUMN_NAME_CREST_URL + " TEXT)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + SoccerFanContract.Teams.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SoccerFan.db";

    public SoccerFanDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
         // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // Adds a team to registered teams in db
    public void addTeam(Team team){
        SQLiteDatabase db = getWritableDatabase();

        // Values to be added in this row
        ContentValues values = new ContentValues();
        values.put(SoccerFanContract.Teams.COLUMN_NAME_TEAM_NAME, team.name);
        values.put(SoccerFanContract.Teams.COLUMN_NAME_TEAM_URL, team.teamURL);
        values.put(SoccerFanContract.Teams.COLUMN_NAME_CREST_URL, team.crestURL);

        // Inserting Row
        db.insert(SoccerFanContract.Teams.TABLE_NAME, null, values);
        db.close(); // Closing database connection

    }

    // Retrieves team information from db
    public Team getTeam(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(SoccerFanContract.Teams.TABLE_NAME, // name of table
                new String[] {SoccerFanContract.Teams.COLUMN_NAME_TEAM_NAME, SoccerFanContract.Teams.COLUMN_NAME_TEAM_URL, SoccerFanContract.Teams.COLUMN_NAME_CREST_URL},
                SoccerFanContract.Teams._ID + "=?", // selection (where clause)
                new String[] { String.valueOf(id) },	// selection arguments
                null, null, null);	// other unused options

        if (cursor != null) {
            cursor.moveToFirst();
        }

        Team team = new Team();
        team.name = cursor.getString(cursor.getColumnIndex(SoccerFanContract.Teams.COLUMN_NAME_TEAM_NAME));
        team.teamURL = cursor.getString(cursor.getColumnIndex(SoccerFanContract.Teams.COLUMN_NAME_TEAM_URL));
        team.crestURL = cursor.getString(cursor.getColumnIndex(SoccerFanContract.Teams.COLUMN_NAME_CREST_URL));

        return team;
    }

    // Gets all teams from db
    public ArrayList<Team> getTeams(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SoccerFanContract.Teams.TABLE_NAME, null); // Query gets all teams

        ArrayList<Team> teams = new ArrayList<>(); // Will contain all teams

        // Checks if there were results
        if (cursor != null) {
            cursor.moveToFirst();
        } else{
            return teams; // returns empty list
        }

        // Iterates through results in cursor
        do{
            // Creates a new team object and fills it with data from query
            Team team = new Team();
            team.name = cursor.getString(cursor.getColumnIndex(SoccerFanContract.Teams.COLUMN_NAME_TEAM_NAME));
            team.teamURL = cursor.getString(cursor.getColumnIndex(SoccerFanContract.Teams.COLUMN_NAME_TEAM_URL));
            team.crestURL = cursor.getString(cursor.getColumnIndex(SoccerFanContract.Teams.COLUMN_NAME_CREST_URL));

            teams.add(team); // Adds team to list

        }while(cursor.moveToNext()); // Moves to next result

        return teams; // Returns teams
    }

    // Returns true when team with this name is already on DB, false when not
    public boolean existsOnDB(String name){
        SQLiteDatabase db = this.getReadableDatabase();
        // Query to get teams with this name
        Cursor cursor = db.query(SoccerFanContract.Teams.TABLE_NAME, // name of table
                new String[]{SoccerFanContract.Teams.COLUMN_NAME_TEAM_NAME, SoccerFanContract.Teams.COLUMN_NAME_TEAM_URL, SoccerFanContract.Teams.COLUMN_NAME_CREST_URL},
                SoccerFanContract.Teams.COLUMN_NAME_TEAM_NAME + "=?", // selection (where clause)
                new String[]{name},    // selection arguments
                null, null, null);	// other unused options

        // Checks whether there was a result or not
        if(cursor.getCount() >0){
            return true;
        } else{
            return false;
        }
    }


}
