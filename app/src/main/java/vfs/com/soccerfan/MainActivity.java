package vfs.com.soccerfan;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TeamsAdapter itemsAdapter;
    private ListView teamsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent selectLeagueIntent = new Intent(getApplicationContext(), LeagueSelectActivity.class);
                startActivity(selectLeagueIntent);
            }
        });

        updateList();
    }

    public void updateList(){
        // Create dbHelper to get teams from db
        SoccerFanDbHelper dbHelper = new SoccerFanDbHelper(this);
        final ArrayList<Team> teamsList = dbHelper.getTeams();

        teamsListView = (ListView)findViewById(R.id.teamsInDB);
        itemsAdapter = new TeamsAdapter(this, teamsList);
        teamsListView.setAdapter(itemsAdapter);

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

    @Override
    public void onResume(){
        super.onResume();
        updateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

}


