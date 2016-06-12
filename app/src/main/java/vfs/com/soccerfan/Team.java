package vfs.com.soccerfan;

import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;


import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Created by pg05carlos on 06/12/2015.
 */
public class Team {

    private static final String DEBUG_TAG = "Team class";

    public String name = "";
    public String shortName = "";
    public String squadMarketValue = "";
    public String fixturesURL = "";
    public String teamURL = "";
    public String playersURL = "";
    public String crestURL = "";
    public Drawable crest = null;
    public Match[] matches = null;
    public Match lastMatch = null;
    public Team(JSONObject JSONobj){
        try {

            this.fixturesURL = ((JSONObject)((JSONObject)JSONobj.get("_links")).get("fixtures")).getString("href");
            this.teamURL = ((JSONObject)((JSONObject)JSONobj.get("_links")).get("self")).getString("href");
            this.playersURL = ((JSONObject)((JSONObject)JSONobj.get("_links")).get("players")).getString("href");
            this.name = JSONobj.getString("name");
            this.shortName = JSONobj.getString("shortName");
            this.squadMarketValue = JSONobj.getString("squadMarketValue");
            this.crestURL = JSONobj.getString("crestUrl");
            //new HttpImageRequestTask().execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Match getLastGame(){
        lastMatch = matches[0];
        for (int i = matches.length-1; i >= 0 ; i--) {
            Log.d("DEBUG", matches[i].awayTeamName);
            if(matches[i].status.equals("FINISHED")){
                lastMatch = matches[i];
                return lastMatch;
            }
        }
        return lastMatch;
    }

    // Constructor with no arguments
    public Team(){}

    private class HttpImageRequestTask extends AsyncTask<Void, Void, Drawable> {
        @Override
        protected Drawable doInBackground(Void ... params) {
            String svj = "";
            Drawable img = null;
            try {
                URL url = new URL(crestURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // Starts the query
                conn.connect();
                InputStream is = conn.getInputStream();
                //if ("gzip".equals(conn.getContentEncoding())) {
                   is= decompressStream(is);
                //}
                /*
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                svj = sb.toString();
                */
                SVG svg = SVGParser.getSVGFromInputStream(is);
                img = svg.createPictureDrawable();

                return img;
                } catch (Exception e) {
                    Log.e("SVG PARSER", e.getMessage(), e);
                    Log.e("SVG PARSER URL", crestURL);
                }

            return img;
            }



        @Override
        protected void onPostExecute(Drawable drawable) {
            // Update the view
            crest = drawable;
        }
    }
    public static InputStream decompressStream(InputStream input) throws IOException{
        PushbackInputStream pb = new PushbackInputStream(input, 2); //we need a pushbackstream to look ahead
        byte[] signature = new byte[2];
        try {
            pb.read(signature); //read the signature
            pb.unread(signature); //push back the signature to the stream

        }catch (Exception e){

        }
        if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) //check if matches standard gzip magic number
            return new GZIPInputStream(pb);
        else
            return pb;
    }

}
