package vfs.com.soccerfan;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LeagueSelectActivity extends AppCompatActivity {
    private ListView leagueListView;
    private static final String DEBUG_TAG = "League Select";
    ArrayList<League> leagueList = new ArrayList<>();
    private LeaguesAdapter itemsAdapter;

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
        setContentView(R.layout.activity_league_select);


        getSupportActionBar().setTitle(R.string.LeagueSelectTitle);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        leagueListView = (ListView)findViewById(R.id.LeagueListView);
        itemsAdapter = new LeaguesAdapter(this, leagueList);
        leagueListView.setAdapter(itemsAdapter);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadJSONTask().execute("http://api.football-data.org/v1/soccerseasons/?season=2015");
        } else {
            // display error
        }
        leagueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                String teamsURL = leagueList.get(position).teamsURL;

                Intent selectTeamIntent = new Intent(getApplicationContext(),TeamSelectActivity.class);
                selectTeamIntent.putExtra("URL",teamsURL);
                startActivity(selectTeamIntent);

            }
        });
    }


    public class LeaguesAdapter extends ArrayAdapter<League> {
        public LeaguesAdapter(Context context, ArrayList<League> Leagues) {
            super(context, 0, Leagues);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            League League = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.league_item, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.LeagueName);
            // Populate the data into the template view using the data object
            tvName.setText(League.name);
            // Return the completed view to render on screen
            return convertView;
        }


    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadJSONTask extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(JSONArray result) {

            for(int i = 0; i < result.length();i++){
                try {
                    League newOne = new League(result.getJSONObject(i));
                    leagueList.add(newOne);
                }catch (JSONException e) {
                    Log.e("JSON Array", "Error arraying data " + e.toString());
                }
            }
            itemsAdapter.notifyDataSetChanged();
            //leagueList = fromJson(result);
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a JSON object.
        private JSONArray downloadUrl(String myurl) throws IOException {
            JSONArray jArray = null;
            JSONObject jObj;
            String json = "";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

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
                    jArray = (JSONArray) new JSONTokener(json).nextValue();
                } catch (JSONException e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                }


                // return JSON String
                return jArray;

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
