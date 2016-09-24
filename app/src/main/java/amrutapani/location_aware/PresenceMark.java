package amrutapani.location_aware;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A login screen that offers login via email/password.
 */
public class PresenceMark extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    Geocoder geocoder;
    List<Address> addresses = null;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location myLastLocation;
    // UI references.
    private EditText mPersonnelNameView;
    private TextView mAddressView;
    private TextView mTimeView;
    private TextView mLatitude, mLongitude;
    private Double myLatitude, myLongitude;
    private long nwTime = 0;
    private AutoCompleteTextView mVillageView;
    private ArrayAdapter<String> adapter;

    private String villageNames[];
    private String villageName;
    private String myAddress;
    private String dateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presence_mark);

        // Set up the login form.
        mPersonnelNameView = (EditText) findViewById(R.id.name);
        mAddressView = (TextView) findViewById(R.id.address);
        mTimeView = (TextView) findViewById(R.id.time);
        mLatitude = (TextView) findViewById(R.id.latitude);
        mLongitude = (TextView) findViewById(R.id.longitude);
        mVillageView = (AutoCompleteTextView) findViewById(R.id.village);

        //this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        villageNames = getResources().getStringArray(R.array.villages);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, villageNames);

        mVillageView.setAdapter(adapter);
        mVillageView.setThreshold(1);


        mVillageView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                villageName = parent.getItemAtPosition(position).toString().trim();
                //Toast.makeText(getApplicationContext(), villageName + " Selected", Toast.LENGTH_LONG).show();
            }
        });

        mPersonnelNameView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                attemptRecord();
                return true;
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    //.addApi(Drive.API)
                    .build();
        }

        Button mRecordButton = (Button) findViewById(R.id.record_visit);
        Button mCaptureLocation = (Button) findViewById(R.id.capture);

        mCaptureLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient.connect();
            }
        });

        mRecordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRecord();
            }
        });
    }

    private void attemptRecord() {
        mPersonnelNameView.setError(null);
        mVillageView.setError(null);
        mLatitude.setError(null);

        villageName = mVillageView.getText().toString().toUpperCase();

        if (mPersonnelNameView.getText().toString().equals("")) {   //Personal Name cannot be left empty
            mPersonnelNameView.setError("This field is required");
            mPersonnelNameView.requestFocus();
        } else if (mVillageView.getText().toString().equals("")) {  //Village name should have been selected
            mVillageView.setError("This field is required");
            mVillageView.requestFocus();
        } else if (!isVillageInVillageList()) {     //entered village name should be in the villages' list.
            mVillageView.setError("Village does not exist in the list. Type a character and select the village from the drop down.");
            mVillageView.requestFocus();
        } /*else if (mLatitude.getText().toString().equals("") || mLongitude.getText().toString().equals("")
                || mAddressView.getText().toString().equals("")) {
            mLatitude.setError("These fields are required. Press the Capture Location button to fetch these values.");
            mLatitude.requestFocus();
        }*/ else {
            Toast.makeText(getApplicationContext(), "Name, Address and Time will be recorded on Server", Toast.LENGTH_SHORT).show();
            /*
            * TODO: record data here
             */
        }
    }

    private boolean isVillageInVillageList() {

        return Arrays.asList(villageNames).contains(villageName.trim());
//        Toast.makeText(getApplicationContext(), "" + contains, Toast.LENGTH_SHORT).show();
    }

    private void setDateTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss a");

        if (nwTime != 0)
            cal.setTimeInMillis(nwTime);

        Date currentTime = cal.getTime();

        dateTime = dateFormat.format(currentTime);
        mTimeView.setText(dateTime);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        requestLocation();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        myLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if (myLastLocation != null) {
            nwTime = myLastLocation.getTime();
            setDateTime();
            myLatitude = myLastLocation.getLatitude();
            myLongitude = myLastLocation.getLongitude();

            mLatitude.setText("Latitude = " + String.valueOf(myLatitude));
            mLongitude.setText("Longitude = " + String.valueOf(myLongitude));

            reverseGeoCode(myLatitude, myLongitude);
        } else
            Toast.makeText(getApplicationContext(), "Unable to retrieve your location co-ordinates right now. Leave your GPS ON " +
                    "for a few minutes before you try again or move to a more open-air location.", Toast.LENGTH_LONG).show();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Conn", "Suspended");
        Toast.makeText(getApplicationContext(), "Conn Suspended", Toast.LENGTH_SHORT).show();
        // mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, ConnectionResult.RESOLUTION_REQUIRED);
                Toast.makeText(getApplicationContext(), "Conn Failed", Toast.LENGTH_SHORT).show();
                mGoogleApiClient.connect();

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }

        Log.d("Conn", "Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Conn", "Location Changed");

        //Toast.makeText(getApplicationContext(), "Location Changed", Toast.LENGTH_SHORT).show();
        //mGoogleApiClient.connect();
    }

    private void requestLocation() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(PresenceMark.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    private void reverseGeoCode(Double latitude, Double longitude) {
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null) {
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            mAddressView.setText(address + "; " + city + "; " + state + "; " + "; " + postalCode);

           /* Toast.makeText(getApplicationContext(), address + "; " + city + "; " + state + "; " + country + "; " +
                    postalCode + "; " + knownName, Toast.LENGTH_LONG).show();*/
        } else {
            mLatitude.setText("NIL");
            mLongitude.setText("NIL");
            mAddressView.setText("Latitude: " + latitude + "\n" + "Longitude: " + longitude +
                    "\n" + "Address could not be retrieved");
        }
        mGoogleApiClient.disconnect();
        nwTime = 0;   //resetting the network time local copy to 0
    }
}



