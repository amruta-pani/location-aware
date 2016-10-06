package amrutapani.location_aware;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Amruta Pani on 04-10-2016.
 */

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    VisitRecordSource visitRecordSource;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Stetho.initializeWithDefaults(context);
        ConnectivityManager connectivityManager = null;
        NetworkInfo activeNetwork = null;

        visitRecordSource = VisitRecordSource.getVisitRecordSourceInstance(context);
        visitRecordSource.open();

        connectivityManager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();


        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_LONG).show();
            visitRecordSource = VisitRecordSource.getVisitRecordSourceInstance(context);

            if (visitRecordSource.getCachedRecordCount() > 0) {
                List<VisitRecord> visitRecords = visitRecordSource.getAllVisitRecords();
                for (int i = 0; i < visitRecords.size(); i++) {
                    uploadData(visitRecords.get(i), context);
                }
            }
        }
    }

    private void uploadData(final VisitRecord visitRecord, final Context context) {

        if (!isInternetConnected()) {
            return;
        }

        //Check if address is retrieved or not. Address could not retrieved without internet

        if (visitRecord.getAddress().equals("")) {
            visitRecord.setAddress(Utils.getUtilsInstance(context)
                    .reverseGeoCode(visitRecord.getLatitude(), visitRecord.getLongitude()));
        }

        //Save personnel name, lat, long, address and village to server

        RequestBody formBody = new FormBody.Builder()
                .add("coName", visitRecord.getCoName())
                .add("villageName", visitRecord.getVillageName())
                .add("latitude", String.valueOf(visitRecord.getLatitude()))
                .add("longitude", String.valueOf(visitRecord.getLongitude()))
                .add("address", visitRecord.getAddress())
                .add("visitDate", visitRecord.getVisitDate())
                .build();

        Request request = new Request.Builder()
                .url(Utils.recordVisitURL)
                .post(formBody)
                .addHeader("Content-Type", "application/json")
                .build();

        Utils.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                showToastOnUI("There seems to be an issue connecting to the remote server.", context);
                if (Utils.LOGGING_ENABLED)
                    e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    if (Utils.LOGGING_ENABLED)
                        throw new IOException("Unexpected code " + response);
                }

                if (response.code() == 200) {
                    visitRecordSource.deleteVisitRecord(visitRecord);
//                    showToastOnUI("Record Pushed to Server", context);
                }
            }
        });
    }

    private boolean isInternetConnected() {
        try {
            return !InetAddress.getByName("www.google.com").equals("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showToastOnUI(final String message, final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
