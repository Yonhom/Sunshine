package com.xuyonghong.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sunshine.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuyonghong on 2016/11/27.
 */

public class ForecastFragment extends Fragment {
    public static final String DEBUG_TAG = ForecastFragment.class.getSimpleName();

    public ArrayAdapter<String> adapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // report to the activity that this fragment like to participate in populating the options menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * refresh weather data
     */
    private void updateWeather() {
        //// use AsynTask to deal with backbround UI thread task
        // get the city id from shared preference
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String cityID = sp.getString(getString(R.string.location_key), "1796231");
        String units = sp.getString(getString(R.string.temp_list), "metric");
        new FetchWeatherTask(getContext(), adapter).execute(cityID, units);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> weekForecast = new ArrayList<>();

        // adapter view
        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);

        // set the adapter view's adapter
        adapter = new ArrayAdapter<>(
                getActivity(), // we may need the current context to find and initialize the resources we need below
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);
        forecastListView.setAdapter(adapter);

        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String weatherStr = adapter.getItem((int) id);
//                Toast.makeText(getActivity(), weatherStr, Toast.LENGTH_SHORT).show();
                //fire up detail activity
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, weatherStr);

                startActivity(intent);
            }
        });

        return rootView;
    }

}
