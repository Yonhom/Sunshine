package com.xuyonghong.sunshine.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.xuyonghong.sunshine.R;
import com.xuyonghong.sunshine.adapter.ForecastAdapter;
import com.xuyonghong.sunshine.data.WeatherContract;
import com.xuyonghong.sunshine.sync.SunshineSyncAdapter;
import com.xuyonghong.sunshine.util.Utility;

/**
 * Created by xuyonghong on 2016/11/27.
 */

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String DEBUG_TAG = ForecastFragment.class.getSimpleName();

    public ForecastAdapter adapter;
    private static final int FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            //In this case the id needs to be fully qualified with a table name, since
            //the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On one hand, that's annoying.  On the other, You can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.
    // If FORECAST_COLUMNS changes, these must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_COORD_LAT = 7;
    public static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "------onCreate------");
        super.onCreate(savedInstanceState);
        // report to the activity that this fragment like to participate in populating the options menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(DEBUG_TAG, "------onCreateOptionsMenu------");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(DEBUG_TAG, "------onOptionsItemSelected------");
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

//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA,
//                Utility.getPreferredLocation(getContext()));
//
//        PendingIntent pi = PendingIntent.getBroadcast(
//                getContext(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager am = (AlarmManager) getActivity().getSystemService(Service.ALARM_SERVICE);
//        // schedule a alarm, this alarm is a intent broadcast that goes to a receiver registered
//        // in the manifest, so the onRecieve method of the registered receiver will be called
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        // when is method is called, onSyncPerformed of SunshineSyncAdapter is called
        SunshineSyncAdapter.syncImmediately(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "------onCreateView------");

        adapter = new ForecastAdapter(getContext(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(adapter);
        View emptyView = rootView.findViewById(R.id.forecast_empty_view);
        // if the adapter is empty, show this view
        // when the app is back online, weather data is returned, and view will be refreshed,
        // thanks to AsyncAdapter
        listView.setEmptyView(emptyView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(),
                // or null if it cannot seek to that position.
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getContext());
                    ((Callback)getActivity()).onItemSelected(
                            WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "------onActivityCreated------");
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        return new CursorLoader(getContext(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dataUri);
    }

    private void updateEmptyView() {
        if (adapter.getCount() == 0) {
            TextView ev = (TextView) getView().findViewById(R.id.forecast_empty_view);
            if (null != ev) {
                int message = R.string.empty_forecast_list;
                if (!Utility.isNetworkAvailable(getActivity())) {
                    message = R.string.empty_forecast_no_network;
                }
                ev.setText(message);
            }
        }
    }
}
