package com.taken.riceutils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
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
    private String lastBusName;
    private FragmentManager fragmentManager;
    private boolean hasLocationPermissions = false;

    private HashMap<String, LatLng> allLocs = new HashMap<>();

    private Boolean loaded = false;

    static JSONArray routes = null;

    @SuppressLint("UseSparseArrays")
    static Map<Integer, Bitmap> busIcons = new HashMap<>();
    static ArrayList<Marker> busMarkers = new ArrayList<>();
    static HashMap<String, ArrayList<LatLng>> busRouteMarkerArrays = null;
    public static ArrayList<Marker> shoutoutMarkers = new ArrayList<>();

    final static int MY_PERMISSIONS_REQUEST_LOCATION = 100;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT < 23) {
            hasLocationPermissions = true;
        }

        fragmentManager = getFragmentManager();
        ((MapFragment) fragmentManager.findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                try {
                    mMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Failed to set user location")
                            .setMessage("Please make sure your location services are enabled. Some app functionality will not work correctly without location services.")
                            .setPositiveButton(R.string.ok, null)
                            .show();
                }
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
                mMap.setOnMarkerClickListener(MainActivity.this);
                mMap.setOnMapClickListener(MainActivity.this);
            }
        });
        BuildingMap.buildMap();

        locListener = new MyLocationListener();
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        allLocs.putAll(BuildingMap.buildings);
        allLocs.putAll(BuildingMap.classes);
        allLocs.putAll(BuildingMap.busStops);
        allLocs.putAll(BuildingMap.pointsOfInterest);
        String[] placeNames = Arrays.copyOf(allLocs.keySet().toArray(), allLocs.keySet().toArray().length, String[].class);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, placeNames);

        AsyncTask<String, Void, JSONArray> busRoutes = new BusRoutes();
        busRoutes.execute("http://bus.rice.edu/json/routes.php");
        try {
            routes = busRoutes.get();
        } catch (Exception e) {
            Log.e("e", e.toString());
        }
        isRunning = true;
        callAsyncTask();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.search_layout, null);
        actionBar.setCustomView(view);
        textView = (AutoCompleteTextView) view.findViewById(R.id.search);
        textView.setAdapter(adapter);
        textView.setOnItemClickListener(this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getString(R.string.title_section1);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
        findViewById(R.id.create_shoutout_button).setVisibility(View.GONE);
        this.loaded = true;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (!this.loaded){
            return;
        }

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        if (position > 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }

        ActionBar actionBar = getSupportActionBar();
        switch (position) {
            case 0: // map
                mTitle = getString(R.string.title_section1);
                actionBar.setDisplayShowTitleEnabled(false);
                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
                findViewById(R.id.map).setVisibility(View.VISIBLE);
                findViewById(R.id.create_shoutout_button).setVisibility(View.GONE);
                clearShoutoutMarkers();
                break;
            case 1: // happening today
                mTitle = getString(R.string.title_section2);
                actionBar.setDisplayShowTitleEnabled(true);

                View pickerView = getLayoutInflater().inflate(R.layout.happening_number_picker_dialog, null);
                final NumberPicker numberPicker = (NumberPicker) pickerView.findViewById(R.id.numberOfDaysPicker);
                numberPicker.setMaxValue(7);
                numberPicker.setMinValue(1);
                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setClickable(false);
                new AlertDialog.Builder(this)
                        .setTitle("View events for the following number of days:")
                        .setView(pickerView)
                        .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                fragmentManager.beginTransaction()
                                        .addToBackStack(null)
                                        .replace(R.id.container, HappeningNow.newInstance(numberPicker.getValue()))
                                        .commit();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                findViewById(R.id.map).setVisibility(View.GONE);
                findViewById(R.id.create_shoutout_button).setVisibility(View.GONE);
                break;
            case 2: //Bus Notifications
                clearShoutoutMarkers();
                mTitle = getString(R.string.title_section3);
                actionBar.setDisplayShowTitleEnabled(true);
                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
                final String[] busNames = new String[]{
                        "Inner Loop",
                        "Graduate Apartments",
                        "Rice Village Apartments/Greenbriar",
                        "Friday Night Rice Village",
                        "Saturday Night Rice Village",
                        "Graduate Apartment Shopping",
                        "Undergraduate Shopping",
                        //"Night Escort Service",
                        "Greater Loop",
                        "BRC Express"
                        //"Texas Medical Center/BRC"
                };
                if (busRouteMarkerArrays == null) {
                    initBusRouteMarkerArrays();
                }
                new AlertDialog.Builder(this)
                        .setTitle(Html.fromHtml("<font color='#03AD97'>Select Bus Type</font>"))
                        .setItems(busNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                lastBusName = busNames[which];
                                ArrayList<LatLng> markerLocs = busRouteMarkerArrays.get(lastBusName);
                                for (LatLng location : markerLocs) {
                                    mMap.addMarker(new MarkerOptions().position(location));
                                }
                                Toast.makeText(MainActivity.this, "Select desired bus stop", Toast.LENGTH_LONG).show();
                            }
                        })
                        .show();

                findViewById(R.id.map).setVisibility(View.VISIBLE);
                break;
            case 3: // shoutout
                mTitle = getString(R.string.title_section4);
                actionBar.setDisplayShowTitleEnabled(true);
                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
                findViewById(R.id.map).setVisibility(View.VISIBLE);
                findViewById(R.id.create_shoutout_button).setVisibility(View.VISIBLE);
                updateShoutoutMap();
                break;
            case 4: // servery menu
                mTitle = getString(R.string.title_section5);
                actionBar.setDisplayShowTitleEnabled(true);
                Intent webViewIntent = new Intent(this, WebViews.class);
                webViewIntent.putExtra("url", getString(R.string.dining_link));
                startActivity(webViewIntent);
                break;
            case 5: // other links
                mTitle = getString(R.string.title_section6);
                actionBar.setDisplayShowTitleEnabled(true);
                final ArrayList<String> sites = new ArrayList<>();
                sites.add(getString(R.string.athletics_link));
                sites.add(getString(R.string.career_link));
                sites.add(getString(R.string.courses_link));
                sites.add(getString(R.string.esther_link));
                sites.add(getString(R.string.event_link));
                sites.add(getString(R.string.helpdesk_link));
                sites.add(getString(R.string.library_link));
                sites.add(getString(R.string.owlspace_link));
                sites.add(getString(R.string.recreation_link));
                sites.add(getString(R.string.news_link));
                sites.add(getString(R.string.rpc_link));
                sites.add(getString(R.string.schedule_link));
                new AlertDialog.Builder(this)
                        .setTitle(Html.fromHtml("<font color='#03AD97'>Other Links</font>"))
                        .setItems(new CharSequence[]{
                                        "Athletics",
                                        "Careers",
                                        "Courses",
                                        "Esther",
                                        "Events",
                                        "HelpDesk",
                                        "Library",
                                        "OwlSpace",
                                        "Recreation",
                                        "RiceNews",
                                        "RPC",
                                        "Schedule Planner"},
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent webViewIntent = new Intent(MainActivity.this, WebViews.class);
                                        webViewIntent.putExtra("url", sites.get(which));
                                        startActivity(webViewIntent);
                                    }
                                })
                        .show();
                break;
            default:
                break;
        }
    }

    public void updateShoutoutMap() {
        // Create a GetShoutoutsTask to retrieve new pins, clear pins, and set new pins
        AsyncTask<String, Void, String> getShoutoutsTask = new GetShoutoutsTask(mMap, this);
        getShoutoutsTask.execute("http://rice-utilities.appspot.com/getposts");
    }

    public void giveShoutout(View view) {
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_LOCATION);
//        }
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Give a shoutout!");
        LayoutInflater inflater = this.getLayoutInflater();
        final View v = inflater.inflate(R.layout.shoutout_layout, null);
        builder.setView(v)
               .setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String text = ((EditText) v.findViewById(R.id.shoutoutText)).getText().toString();
                        String lat = "";
                        String lng = "";
                        try {
                            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locListener);
                            Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                lat += location.getLatitude();
                                lng += location.getLongitude();
                            }
//                            Location location = locManager.getLastKnownLocation(locManager.getBestProvider(new Criteria(), true));
//                            if (location != null) {
//                                lat += location.getLatitude();
//                                lng += location.getLongitude();
//                            }
                        } catch (SecurityException e) {
                            Log.e("e", e.toString());
                        }
                        // Create a PostShoutoutTask to send the new shoutout to the server
                        AsyncTask<String, Void, Void> postShoutoutTask = new PostShoutoutTask(text, lat, lng, MainActivity.this);
                        postShoutoutTask.execute("http://rice-utilities.appspot.com/addpost");
                    }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                hasLocationPermissions = grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    private void clearShoutoutMarkers() {
        for (Marker marker : shoutoutMarkers) {
            marker.setVisible(false);
        }
    }

    public void showShoutoutMarkers() {
        for (Marker marker : shoutoutMarkers) {
            marker.showInfoWindow();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    private void initBusRouteMarkerArrays() {
        busRouteMarkerArrays = new HashMap<>();

        for (String route : BuildingMap.busRoutes.keySet()) {
            ArrayList<LatLng> busStopLocs = new ArrayList<>();
            for (String stopOnRoute : BuildingMap.busRoutes.get(route).keySet()) {
                busStopLocs.add(BuildingMap.busStops.get(stopOnRoute));
            }
            busRouteMarkerArrays.put(route, busStopLocs);
        }
    }


//    @Override
//    public void setTitle(CharSequence title) {
//        mTitle = title;
//        getActionBar().setTitle(mTitle);
//    }


//    public void onSectionAttached(int number) {
//        switch (number) {
//            case 1:
//                mTitle = getString(R.string.title_section1);
//                break;
//            case 2:
//                mTitle = getString(R.string.title_section2);
//                break;
//            case 3:
//                mTitle = getString(R.string.title_section3);
//                break;
//            case 4:
//                mTitle = getString(R.string.title_section4);
//                break;
//            case 5:
//                mTitle = getString(R.string.title_section5);
//                break;
//            case 6:
//                mTitle = getString(R.string.title_section6);
//                break;
//            case 7:
//                mTitle = getString(R.string.title_section7);
//                break;
//        }
//    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        if (mTitle.equals(getString(R.string.title_section1))) {
            actionBar.setDisplayShowTitleEnabled(false);
        } else {
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setVisible(false);
        return true;
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
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locListener);
                Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(location != null) {
                    String toastText = calculateWalkingTime(location, loc);
                    Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
                }
            } catch (SecurityException e) {
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
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStack();
            findViewById(R.id.map).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mTitle.equals("Bus Notifications") && lastBusName != null) {
            String busStop = "";
            for (Map.Entry<String, LatLng> entry : BuildingMap.busStops.entrySet()) {
                if (entry.getValue().equals(marker.getPosition())) {
                    busStop = entry.getKey();
                }
            }

            if (busStop.equals("")) {
                return false;
            }

            Intent serviceIntent = new Intent(this, BusNotificationService.class);
            serviceIntent.putExtra("BusType", lastBusName).putExtra("BusStop", busStop);
            startService(serviceIntent);
            new AlertDialog.Builder(this)
                    .setTitle("Bus notification pending")
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }
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
    public void onMapClick(LatLng point) {
        if (mTitle.equals("Bus Notifications")) {
            return;
        }
        double clickX = point.longitude;
        double clickY = point.latitude;
        String shortestKey = "";
        double shortestDist = Double.MAX_VALUE;

        HashMap<String, LatLng> selectableLocs = new HashMap<>();
        selectableLocs.putAll(BuildingMap.buildings);

        for (Map.Entry<String, LatLng> entry : selectableLocs.entrySet()) {
            double x = entry.getValue().longitude;
            double y = entry.getValue().latitude;

            double dist = Math.sqrt(Math.pow(clickX - x, 2) + Math.pow(clickY - y, 2));
            if (dist < shortestDist) {
                shortestKey = entry.getKey();
                shortestDist = dist;
            }
        }

        LatLng loc = selectableLocs.get(shortestKey);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
        marker.setPosition(loc);
        marker.setTitle(shortestKey);
        marker.showInfoWindow();
    }

    public static GoogleMap getMap() {
        return mMap;
    }

}
