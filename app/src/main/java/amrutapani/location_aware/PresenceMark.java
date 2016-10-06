package amrutapani.location_aware;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PresenceMark extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    /*private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();
    private final String recordVisitURL = "http://ec2-52-32-92-198.us-west-2.compute.amazonaws.com/VV/v1/record.php";
    private final boolean LOGGING_ENABLED = false;*/

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location myLastLocation;
    // UI references.
    private AutoCompleteTextView mPersonnelNameView;
    private TextView mAddressView;
    private TextView mTimeView;
    private TextView mLatitude, mLongitude;
    private Double myLatitude, myLongitude;
    private long nwTime = 0;
    private AutoCompleteTextView mVillageView;
    private ArrayAdapter<String> adapterVillageNames, adapterCONames;
    private String villageNames[];
    private String villageName;
    private String coNames[];
    private String coName;
    private String myAddress;
    private String dateTime;

    private VisitRecordSource visitRecordSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

/*
        Only for debugging purposes

        Stetho.initializeWithDefaults(this);
        Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build();
*/

        setContentView(R.layout.activity_presence_mark);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        visitRecordSource = VisitRecordSource.getVisitRecordSourceInstance(getApplicationContext());
        visitRecordSource.open();

        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mPersonnelNameView = (AutoCompleteTextView) findViewById(R.id.name);
        mAddressView = (TextView) findViewById(R.id.address);
        mTimeView = (TextView) findViewById(R.id.time);
        mLatitude = (TextView) findViewById(R.id.latitude);
        mLongitude = (TextView) findViewById(R.id.longitude);
        mVillageView = (AutoCompleteTextView) findViewById(R.id.village);

        Typeface font = Typeface.createFromAsset(getAssets(), "century-gothic.ttf");

        mPersonnelNameView.setTypeface(font);
        mAddressView.setTypeface(font);
        mTimeView.setTypeface(font);
        mLatitude.setTypeface(font);
        mLongitude.setTypeface(font);
        mVillageView.setTypeface(font);

        villageNames = getResources().getStringArray(R.array.villages);
        adapterVillageNames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, villageNames);
        mVillageView.setAdapter(adapterVillageNames);
        mVillageView.setThreshold(1);

        coNames = getResources().getStringArray(R.array.co);
        adapterCONames = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, coNames);
        mPersonnelNameView.setAdapter(adapterCONames);
        mPersonnelNameView.setThreshold(0);

        mVillageView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                villageName = parent.getItemAtPosition(position).toString().trim();
                //Toast.makeText(getApplicationContext(), villageName + " Selected", Toast.LENGTH_LONG).show();
            }
        });

        mPersonnelNameView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                coName = parent.getItemAtPosition(position).toString().trim();
            }
        });

        mPersonnelNameView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPersonnelNameView.showDropDown();          //Show drop down without typing a character.
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
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
                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null :
                        getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                attemptRecord();
            }
        });
    }

    private void attemptRecord() {
        mPersonnelNameView.setError(null);
        mVillageView.setError(null);
        mLatitude.setError(null);

        villageName = mVillageView.getText().toString().toUpperCase();
        coName = mPersonnelNameView.getText().toString().toUpperCase();

        if (coName.equals("")) {   //Personal Name cannot be left empty
            mPersonnelNameView.setError(getResources().getString(R.string.field_required));
            mPersonnelNameView.requestFocus();
        } else if (mVillageView.getText().toString().equals("")) {  //Village name should have been selected
            mVillageView.setError(getResources().getString(R.string.field_required));
            mVillageView.requestFocus();
        } else if (!isVillageInVillageList()) {     //entered village name should be in the villages' list.
            mVillageView.setError(getResources().getString(R.string.no_village_autocomplete));
            mVillageView.requestFocus();
        } else if (!isCOInCONamesList()) {
            mPersonnelNameView.setError(getResources().getString(R.string.no_coname_autocomplete));
            mPersonnelNameView.requestFocus();
        } else if (mLatitude.getText().toString().equals("") || mLongitude.getText().toString().equals("")
                || mAddressView.getText().toString().equals("")) {
            mLatitude.setError(getResources().getString(R.string.capture_field_required));
            mLatitude.requestFocus();
        } else {
            if (isInternetConnected()) {
                recordData();
            } else {
                saveDataInDb();
                Toast.makeText(PresenceMark.this, getResources().getString(R.string.no_internet_to_push), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveDataInDb() {

        VisitRecord visitRecord = new VisitRecord(coName, villageName, myLatitude, myLongitude, myAddress, dateTime);
        long insertId = visitRecordSource.insertRecord(visitRecord);
    }

    private void showToastOnUI(final String message) {
        PresenceMark.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PresenceMark.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isInternetConnected() {
        try {
            return !InetAddress.getByName("www.google.com").equals("");
        } catch (UnknownHostException e) {
            if (Utils.LOGGING_ENABLED)
                e.printStackTrace();
            return false;
        }
    }

    private void recordData() {

        //Save personnel name, lat, long, address and village to server

        RequestBody formBody = new FormBody.Builder()
                .add("coName", coName)
                .add("villageName", villageName)
                .add("latitude", String.valueOf(myLatitude))
                .add("longitude", String.valueOf(myLongitude))
                .add("address", myAddress)
                .add("visitDate", dateTime)
                .build();

        Request request = new Request.Builder()
                .url(Utils.recordVisitURL)
                .post(formBody)
                .addHeader("Content-Type", "application/json")
                .build();

        Utils.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                saveDataInDb();
                showToastOnUI("There seems to be an issue connecting to the remote server.");
                if (Utils.LOGGING_ENABLED)
                    e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    saveDataInDb();
                    if (Utils.LOGGING_ENABLED)
                        throw new IOException("Unexpected code " + response);
                }

                if (response.code() == 200) {
                    showToastOnUI("Record Pushed to Server");
                } else {
                    saveDataInDb();
                    showToastOnUI("Unknown response from Server");
                }
            }
        });
    }

    private boolean isVillageInVillageList() {

        return Arrays.asList(villageNames).contains(villageName.trim());
//        Toast.makeText(getApplicationContext(), "" + contains, Toast.LENGTH_SHORT).show();
    }

    private boolean isCOInCONamesList() {

        return Arrays.asList(coNames).contains(coName.trim());
//        Toast.makeText(getApplicationContext(), "" + contains, Toast.LENGTH_SHORT).show();
    }

    private void setDateTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");

/*
        If network time is required enable the below code.

        if (nwTime != 0)
            cal.setTimeInMillis(nwTime);
*/

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

            myAddress = "";
            if (isInternetConnected()) {
                myAddress = Utils.getUtilsInstance(this).reverseGeoCode(myLatitude, myLongitude); //Reverse geo code will not work if device is not connected to internet.
                mAddressView.setText((myAddress.equals("")) ? getResources().getString(R.string.no_address) : myAddress);
            } else
                mAddressView.setText(getResources().getString(R.string.no_address_wo_internet));
            nwTime = 0;   //resetting the network time local copy to 0
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_gps), Toast.LENGTH_LONG).show();
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
                if (Utils.LOGGING_ENABLED)
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
}



