package com.xuyonghong.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu); // this method called only once, the first time the option menu is displayed

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

        return super.onOptionsItemSelected(item);

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * intent from MainActivity
         */
        Intent intent;

        TextView weatherDetailView;

        public PlaceholderFragment() {
            setHasOptionsMenu(true); // for the onCreateOptionsMenu method in fragment to be called, this method has to be called first
        }

        private Intent shareIntent() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/txt");
            intent.putExtra(Intent.EXTRA_TEXT, weatherDetailView.getText());

            return intent;

        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detail_fragment, menu);

            //get menu item for share
            MenuItem item = menu.findItem(R.id.action_share_detail);
            ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

            //set intent for the action provider
            actionProvider.setShareIntent(shareIntent());

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            weatherDetailView = (TextView) rootView.findViewById(R.id.weather_detail);
            intent = getActivity().getIntent();

            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String weatherDetail = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
                weatherDetailView.setText(weatherDetail);
            }

            return rootView;
        }

    }

}
