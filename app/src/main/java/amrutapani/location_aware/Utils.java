package amrutapani.location_aware;

import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Amruta Pani on 05-10-2016.
 */

public final class Utils {

    private Utils(){
    }

    public static Utils getUtilsInstance(Context context) {
        if(utilsInstance == null) {
            utilsInstance = new Utils();
            utilsContext = context;
        }
        return utilsInstance;
    }

    private static Context utilsContext;
    private static Utils utilsInstance = null;
    private Geocoder geocoder;
    private List<Address> addresses = null;

    public static final OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(new StethoInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();
    public static final String recordVisitURL = "http://ec2-52-32-92-198.us-west-2.compute.amazonaws.com/VV/v1/record.php";
    public static final boolean LOGGING_ENABLED = false;

    public String reverseGeoCode(Double latitude, Double longitude) {
        geocoder = new Geocoder(utilsContext, Locale.getDefault());
        String address = "";
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            if (Utils.LOGGING_ENABLED)
                e.printStackTrace();
        }

        if (addresses != null) {
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName();

            address = (!city.equals("")) ? address + ", " + city : address;
            address = (!state.equals("")) ? address + ", " + state : address;
            address = (!postalCode.equals("")) ? address + ", " + postalCode : address;
        }

        return address;
    }

}
