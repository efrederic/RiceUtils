package com.taken.riceutils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, AdapterView.OnItemClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private static GoogleMap mMap;
    private AutoCompleteTextView textView;
    private Marker marker;
    private LocationManager locManager;
    private LocationListener locListener;
    private boolean isRunning;

    private HashMap<String, LatLng> allLocs = new HashMap<>();

    private Boolean loaded=false;

    static JSONArray routes = null;

    @SuppressLint("UseSparseArrays")
    static Map<Integer, Bitmap> busIcons = new HashMap<>();
    static ArrayList<Marker> busMarkers = new ArrayList<>();

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setMyLocationEnabled(true);
        marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        BuildingMap.buildMap();

        locListener = new MyLocationListener();
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        allLocs.putAll(BuildingMap.buildings);
        allLocs.putAll(BuildingMap.classes);
        String[] placeNames = Arrays.copyOf(allLocs.keySet().toArray(), allLocs.keySet().toArray().length, String[].class);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, placeNames);
        textView = (AutoCompleteTextView) findViewById(R.id.search);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener(this);

        AsyncTask<String, Void, JSONArray> busRoutes = new BusRoutes();
        busRoutes.execute("http://bus.rice.edu/json/routes.php");
        try {
            routes = busRoutes.get();
        } catch (Exception e) {
            Log.e("e", e.toString());
        }
        isRunning = true;
        callAsyncTask();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        this.loaded = true;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (!this.loaded){
            return;
        }
        Fragment fragment = PlaceholderFragment.newInstance(position+1);

        switch (position) {
            case 0:
                findViewById(R.id.map).setVisibility(View.VISIBLE);
                break;
            case 1:
                fragment = HappeningNow.newInstance();
                findViewById(R.id.map).setVisibility(View.GONE);
                break;
            case 2:
                findViewById(R.id.map).setVisibility(View.GONE);
                break;
        }
//         update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.map, fragment)
                .commit();
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onStop() {
        try {
            locManager.removeUpdates(locListener);
        } catch (SecurityException e) {
            Log.e("e",e.toString());
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        isRunning = false;
        super.onPause();
    }

    @Override
    public void onResume(){
        isRunning = true;
        super.onResume();
    }

    private double degreesToRadians(double deg){
        return Math.PI/180 * deg;
    }

    private String calculateWalkingTime(Location p1, LatLng p2){
        double radius = 3959; // radius of earth in miles
        double diffLat = degreesToRadians(p2.latitude - p1.getLatitude());
        double diffLong = degreesToRadians(p2.longitude - p1.getLongitude());

        double inSqrt = Math.sin(diffLat/2) * Math.sin(diffLat/2) +
                Math.cos(degreesToRadians(p1.getLatitude())) * Math.cos(degreesToRadians(p2.latitude)) * Math.sin(diffLong/2) * Math.sin(diffLong/2);
        double dist = 2 * radius * Math.asin(Math.sqrt(inSqrt));
        double minutes = dist * 20; // assume 20 mins per mile
        return "Distance: " + Math.floor(dist * 100) / 100 + " miles, Time: " + (int)minutes + " minutes";
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        String text = textView.getText().toString();

        LatLng loc = allLocs.get(text);
        if(loc != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
            marker.setPosition(loc);
            marker.setTitle(text);
            marker.showInfoWindow();

            try {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, locListener);
                Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                String toastText = calculateWalkingTime(location, loc);
                Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("e", e.toString());
            }
        } else if (BuildingMap.classes.containsKey(text)) {
            DialogFragment newFragment = new TBADialog();
            newFragment.show(getFragmentManager(), "Oops!");
        } else {
            DialogFragment newFragment = new LocNotFound();
            newFragment.show(getFragmentManager(), "Oops!");
        }
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        return false;
    }

    private void callAsyncTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsyncTask = new TimerTask() {
            @Override
            public void run() {
                if(isRunning){
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                new BusLocator().execute("http://bus.rice.edu/json/buses.php");
                            } catch (Exception e) {
                                Log.e("e",e.toString());
                            }
                        }
                    });
                }
            }
        };
        timer.schedule(doAsyncTask, 0, 2000);
    }

    @Override
    public void onMapClick(LatLng point){
        double clickX = point.longitude;
        double clickY = point.latitude;
        String shortestKey = "";
        double shortestDist = Double.MAX_VALUE;

        for (Map.Entry<String, LatLng> entry : BuildingMap.buildings.entrySet()) {
            double x = entry.getValue().longitude;
            double y = entry.getValue().latitude;

            double dist = Math.sqrt(Math.pow(clickX - x, 2) + Math.pow(clickY - y, 2));
            if (dist < shortestDist) {
                shortestKey = entry.getKey();
                shortestDist = dist;
            }
        }

        LatLng loc = BuildingMap.buildings.get(shortestKey);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
        marker.setPosition(loc);
        marker.setTitle(shortestKey);
        marker.showInfoWindow();
    }

    public static GoogleMap getMap() {
        return mMap;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            ((MainActivity) context).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }



}
