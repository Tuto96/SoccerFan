package vfs.com.soccerfan;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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
import java.util.ArrayList;

public class TeamSelectActivity extends AppCompatActivity {
    private ListView teamsListView;
    private static final String DEBUG_TAG = "Team Select";
    ArrayList<Team> teamsList = new ArrayList<>();
    private TeamsAdapter itemsAdapter;

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
        setContentView(R.layout.activity_team_select);

        getSupportActionBar().setTitle(R.string.TeamSelectTitle);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        teamsListView = (ListView)findViewById(R.id.TeamListView);
        itemsAdapter = new TeamsAdapter(this, teamsList);
        teamsListView.setAdapter(itemsAdapter);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() && getIntent().getStringExtra("URL")!="") {

            new DownloadJSONTask().execute(getIntent().getStringExtra("URL"));

        } else {
            // display error
        }
        teamsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String crestURL = teamsList.get(position).crestURL;
                String teamURL = teamsList.get(position).teamURL;

                Intent selectTeamIntent = new Intent(getApplicationContext(),MainTeamActivity.class);
                selectTeamIntent.putExtra("URL", teamURL);
                startActivity(selectTeamIntent);
            }
        });
    }


    public class TeamsAdapter extends ArrayAdapter<Team> {
        public TeamsAdapter(Context context, ArrayList<Team> teams) {
            super(context, 0, teams);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Team team = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.team_item, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.TeamName);
            ImageView ivCrest = (ImageView) convertView.findViewById(R.id.TeamCrest);

            //ivCrest.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            // Populate the data into the template view using the data object
            tvName.setText(team.name);
            //ivCrest.setImageDrawable(team.crest);
            // Return the completed view to render on screen
            return convertView;
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
            int count = 0;
            try {
                count = result.getInt("count");
            }catch (JSONException e){
                Log.e("JSON Array", "Error arraying data " + e.toString());
            }
            if(count==0){
                count=result.length();
            }
            for(int i = 0; i < count;i++){
                try {
                    Team newOne = new Team(result.getJSONArray("teams").getJSONObject(i));
                    teamsList.add(newOne);
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
        private JSONObject downloadUrl(String myurl) throws IOException {
            JSONArray jArray = null;
            JSONObject jObj = null;
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
                conn.setRequestProperty("X-Auth-Token", "30aeb6530cdc4fc88f25476b1606e181");
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