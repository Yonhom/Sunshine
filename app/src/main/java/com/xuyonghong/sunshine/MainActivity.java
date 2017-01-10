package com.xuyonghong.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.sunshine.app.R;
import com.xuyonghong.sunshine.fragment.DetailFragment;
import com.xuyonghong.sunshine.fragment.ForecastFragment;
import com.xuyonghong.sunshine.util.Utility;

/**
 * MainActivity到ForecastFragment主要声明周期调用顺序
 * MainActivity.onCreate --> ForecastFragment.onCreate -->
 * ForecastFragment.onCreateView --> ForecastFragment.onActivityCreated
 * ForecastFragment.onStart -->
 * MainActivity.onStart --> MainActivity.onResume
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();

    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = Utility.getPreferredLocation(this);

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        Log.d(DEBUG_TAG, "-------------onCreate called-----------------");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(DEBUG_TAG, "-------------onStart called-----------------");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(DEBUG_TAG, "-------------onPause called-----------------");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "-------------onResume called-----------------");
        String location = Utility.getPreferredLocation(this);

        if (location != null && !location.endsWith(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }
        }
        mLocation = location;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(DEBUG_TAG, "-------------onStop called-----------------");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(DEBUG_TAG, "-------------onDestory called-----------------");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            // start the SettingsActivity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_prefered_location) {
            // get the prefered location from sharedpreference
            String defaultGeoStr = "geo:37.786971,-122.399677;u=35";
            SharedPreferences sp = getSharedPreferences("PreferedLocation", MODE_PRIVATE);
            String geoUriStr = sp.getString("geoUriStr", defaultGeoStr);
            Uri geoUri = Uri.parse(geoUriStr);

            // show a map activity with a implicit intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Map can not be opend!", Toast.LENGTH_SHORT).show();
            }


        }

        return super.onOptionsItemSelected(item);
    }


}
