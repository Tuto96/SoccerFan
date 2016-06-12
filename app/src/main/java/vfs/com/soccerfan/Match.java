package vfs.com.soccerfan;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;

/**
 * Created by pg05carlos on 07/12/2015.
 */
public class Match {
    public String date = "";
    public String status = "";
    public String homeTeamName = "";
    public String awayTeamName = "";
    public int goalsHomeTeam;
    public int goalsAwayTeam;
    public Match(JSONObject JSONobj){
        try{
            this.date = JSONobj.getString("date");
            this.status = JSONobj.getString("status");
            this.homeTeamName = JSONobj.getString("homeTeamName");
            this.awayTeamName = JSONobj.getString("awayTeamName");
            if(status.equals("FINISHED")){
                JSONObject result = (JSONObject)JSONobj.get("result");
                this.goalsHomeTeam =  result.getInt("goalsHomeTeam");
                this.goalsAwayTeam =  result.getInt("goalsAwayTeam");
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
