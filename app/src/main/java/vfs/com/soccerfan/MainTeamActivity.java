package vfs.com.soccerfan;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pg05carlos on 06/12/2015.
 */

public class MainTeamActivity extends AppCompatActivity {
    Team mTeam = null;
    Match mLastMatch = null;
    FloatingActionButton fab = null;

    private static final String DEBUG_TAG = "Team Main";
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_team);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && getIntent().getStringExtra("URL")!="") {

            new DownloadJSONTask().execute(getIntent().getStringExtra("URL"));


        } else {
            // display error
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Saves team in the db
                SoccerFanDbHelper dbHelper = new SoccerFanDbHelper(getApplicationContext());
                dbHelper.addTeam(mTeam);
                Toast.makeText(getBaseContext(), mTeam.name.concat(" was added to favorites"), Toast.LENGTH_LONG).show();
                fab.hide();
            }
        });
        fab.hide();
    }


    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadJSONMatchTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                JSONArray fixtures = result.getJSONArray("fixtures");
                mTeam.matches = new Match[fixtures.length()];
                for(int i=0;i<mTeam.matches.length;i++){
                    mTeam.matches[i]= new Match(result.getJSONArray("fixtures").getJSONObject(i));
                    //lastMatch = new Match(result.getJSONArray("fixtures").getJSONObject(result.getInt("count")-1));
                }
                TextView tvHomeTeamName = (TextView)findViewById(R.id.homeTeamName);
                TextView tvAwayTeamName = (TextView)findViewById(R.id.awayTeamName);
                TextView tvHomeTeamScore = (TextView)findViewById(R.id.homeTeamScore);
                TextView tvAwayTeamScore = (TextView)findViewById(R.id.awayTeamScore);
                TextView tvLastMatchDate = (TextView) findViewById(R.id.lastMatchDate);
                TextView tvLargeHomeTeamName = (TextView) findViewById(R.id.largeHomeTeamName);

                mLastMatch = mTeam.getLastGame();

                tvHomeTeamName.setText(mLastMatch.homeTeamName);
                tvLargeHomeTeamName.setText(mTeam.name);
                tvAwayTeamName.setText(mLastMatch.awayTeamName);

                tvAwayTeamName.setHorizontallyScrolling(false);
                tvHomeTeamName.setHorizontallyScrolling(false);

                tvHomeTeamScore.setText(String.valueOf(mLastMatch.goalsHomeTeam));
                tvAwayTeamScore.setText(String.valueOf(mLastMatch.goalsAwayTeam));

                tvLastMatchDate.setText(mLastMatch.date);

                SoccerFanDbHelper dbHelper = new SoccerFanDbHelper(getApplicationContext());
                if( ! dbHelper.existsOnDB(mTeam.name)){
                    fab.show();
                }

            }catch (JSONException e){
                e.printStackTrace();
            }

            //ImageView ivCrest = (ImageView)findViewById(R.id.largeTeamCrest);
            //ivCrest.setImageBitmap(mTeam.crest.);
            //leagueList = fromJson(result);
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a JSON object.
        private JSONObject downloadUrl(String myurl) throws IOException {
            JSONArray jArray = null;
            JSONObject jObj = null;
            String json = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("X-Auth-Token","30aeb6530cdc4fc88f25476b1606e181");
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();


                // Convert the InputStream into a string
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    json = sb.toString();
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }

                // try parse the string to a JSON object
                try {
                    jObj = new JSONObject(json);
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }


                // return JSON String
                return jObj;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadJSONTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(JSONObject result) {
            mTeam = new Team(result);
            new DownloadJSONMatchTask().execute(mTeam.fixturesURL);
            getSupportActionBar().setTitle(mTeam.name);





            //ImageView ivCrest = (ImageView)findViewById(R.id.largeTeamCrest);
            //ivCrest.setImageBitmap(mTeam.crest.);
            //leagueList = fromJson(result);
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a JSON object.
        private JSONObject downloadUrl(String myurl) throws IOException {
            JSONArray jArray = null;
            JSONObject jObj = null;
            String json = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setRequestProperty("X-Auth-Token","30aeb6530cdc4fc88f25476b1606e181");
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();


                // Convert the InputStream into a string
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            is));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    json = sb.toString();
                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }

                // try parse the string to a JSON object
                try {
                    jObj = new JSONObject(json);
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }


                // return JSON String
                return jObj;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }
}
