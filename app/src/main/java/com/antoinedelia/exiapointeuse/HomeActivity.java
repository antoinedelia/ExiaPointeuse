package com.antoinedelia.exiapointeuse;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class HomeActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private NavigationView navigationView;
    private GoogleMap mMap;
    private Location location;
    private final double LATCESI = 43.548330;
    private final double LONCESI = 1.502874;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            logout();
            backToLoginScreen();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displayUserInfo(user);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  }, 1 );
        ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  }, 1 );

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED){return;}
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED){return;}
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        Log.d("LAT", String.valueOf(latitude));
        Log.d("LON", String.valueOf(longitude));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(distance(location.getLatitude(), LATCESI, location.getLongitude(), LONCESI) > 1000) {
                    Snackbar.make(view, getString(R.string.too_far_away), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else
                {
                    Snackbar.make(view, getString(R.string.congratulations), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });


    }

    private void backToLoginScreen() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void logout() {
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.really_exit)
                    .setMessage(R.string.exit_confirmation)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            HomeActivity.this.finishAffinity();
                        }
                    }).create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_manage) {

        } else if (id == R.id.logout) {
            Log.d("ERROR", "ERROR");
            logout();
            backToLoginScreen();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void displayUserInfo(FirebaseUser user){
        if(user != null)
        {
            View headerLayout = navigationView.getHeaderView(0);
            TextView textViewFacebookName = (TextView)headerLayout.findViewById(R.id.facebook_name);
            textViewFacebookName.setText(user.getDisplayName());

            ImageView imageViewFacebook = (ImageView)headerLayout.findViewById(R.id.imageViewFacebook);
            Picasso.with(HomeActivity.this).load(user.getPhotoUrl()).resize(300, 300).centerInside().into(imageViewFacebook);

//            ImageView imageView = (ImageView) findViewById(R.id.facebook_photo);
//            Picasso.with(HomeActivity.this).load(user.getPhotoUrl()).resize(200, 200).centerInside().into(imageView);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
    }

    private void setUpMap() {
        LatLng cesi = new LatLng(LATCESI, LONCESI);
        mMap.addMarker(new MarkerOptions().position(cesi).title("CESI Toulouse")).showInfoWindow();

        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentLocation).title(getString(R.string.your_position))).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17.0f));
        TextView textView = (TextView)findViewById(R.id.distance);
        textView.setText(String.valueOf(distance(location.getLatitude(), LATCESI, location.getLongitude(), LONCESI)));
        //this.map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(43.548323,1.502874) , 14.0f) );
    }

    /**
     * Calculate distance between two points in latitude and longitude
     *
     * lat1, lon1 Start point lat2, lon2 End point
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);
        return Math.round(Math.sqrt(distance)*100.0)/100.0;
    }
}