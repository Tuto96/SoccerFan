package vfs.com.soccerfan;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by pg05carlos on 06/12/2015.
 */
public class League {
    public String name = "";
    public String year =  "";
    public int numberOfTeams = 18;
    public int numberOfGames= 306;
    public String teamsURL = "";

            //public Date lastUpdated = new Date() "2015-12-06T18:23:26Z";
    public League(String name, String year){
        this.name = name;
        this.year = year;
    }

    public League(JSONObject JSONobj){
        try {
            this.name = JSONobj.getString("caption");
            this.year = JSONobj.getString("year");
            this.teamsURL = ((JSONObject)((JSONObject)JSONobj.get("_links")).get("teams")).getString("href");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
