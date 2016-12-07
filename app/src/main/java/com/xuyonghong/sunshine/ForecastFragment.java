package com.xuyonghong.sunshine;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.R;import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xuyonghong on 2016/11/27.
 */

public class ForecastFragment extends Fragment {
    public static final String DEBUG_TAG = FetchForecastDataTask.class.getSimpleName();

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
        new FetchForecastDataTask().execute(cityID, units);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    /**
     * this AsyncTask inner class deals with data fetching and pupolation
     */
    private class FetchForecastDataTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {

            ///// add network function
            HttpURLConnection conn = null;
            BufferedReader reader = null; //enable us to read line of text efficiently from a inputstream

            String format = "json";
            int numDays = 7;
            String appid = "160918fb72a9a78a48532aa1dbb580d6";

            String jsonStr = null; // returned json resposne from server

            try{
//                URL url = new URL(strings[0]); //strings[0] is first parameter you passed to execute method
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String CITY_ID_PARAM = "id";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAMS = "APPID";


                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(CITY_ID_PARAM, strings[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, strings[1])
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAMS, appid)
                        .build();

                URL url = new URL(builtUri.toString());


                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                // get the input stream from the connection
                InputStream is = conn.getInputStream();
                if (is == null) {
                    return null;
                }

                // this is where we store the read text
                StringBuffer buffer = new StringBuffer(); // just like a string, but can be modified
                reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) { // read a line of text
                    buffer.append(line + "\n"); // store the read line into the string buffer
                }

                if (buffer.length() == 0) {
                    return null;
                }

                jsonStr = buffer.toString();
                Log.d(DEBUG_TAG, jsonStr);

            }catch (IOException exception) {
//                Log.e(DEBUG_TAG, "Error", exception);

                return null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // json to weather data array
            String[] formatedWeatherList = null;

            try {
                formatedWeatherList = getWeatherDataFromJson(jsonStr, 7);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return formatedWeatherList;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (strings != null && strings.length > 0) {
                List<String> weatherDataList = Arrays.asList(strings);

                adapter.clear();
                for(String weatherData : weatherDataList) {
                    adapter.add(weatherData);
                }
            } else {
                Log.d(DEBUG_TAG, "city id is wrong");
                Toast.makeText(
                        getActivity(),
                        "No weather data returned, please enter the right city ID in Settings" +
                                " or check your internet connection.",
                        Toast.LENGTH_SHORT)
                        .show();
            }



        }

        //// json parsing related

        /* The date/time conversion code is going to be moved outside the asynctask later,
             * so for convenience we're breaking it out into its own method now.
             */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            final String OWM_COORDINATE = "coord";
            final String OWM_LONGITUDE = "lon";
            final String OWM_LATITUDE = "lat";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);

            // get the coordinate object
            JSONObject coordObject = forecastJson.getJSONObject("city").getJSONObject(OWM_COORDINATE);
            double lon = coordObject.getDouble(OWM_LONGITUDE);
            double lat = coordObject.getDouble(OWM_LATITUDE);
            String geoUriStr = "geo:" + lon + "," + lat + ";" + "u=35";
            // save it in the preference
            SharedPreferences sp =
                    getActivity().getSharedPreferences("PreferedLocation", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("geoUriStr", geoUriStr);
            editor.commit();

            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
//                Log.v(DEBUG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

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
