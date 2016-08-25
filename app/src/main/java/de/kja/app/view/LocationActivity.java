package de.kja.app.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;
import org.androidannotations.rest.spring.api.RestErrorHandler;
import org.springframework.core.NestedRuntimeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.kja.app.R;
import de.kja.app.client.DistrictClient;
import de.kja.app.model.District;

@EActivity
public class LocationActivity extends AppCompatActivity implements RestErrorHandler {

    private static final int PERMISSION_REQUEST_LOCATION = 42;
    private static final String TAG = "LocationActivity";

    @ViewById(R.id.districtTextView)
    protected AutoCompleteTextView districtTextView;

    @RestService
    protected DistrictClient districtClient;

    private List<District> districts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        districtClient.setRestErrorHandler(this);

        SharedPreferences preferences = getSharedPreferences(MainActivity.PREFERENCE_FILE_KEY, MODE_PRIVATE);
        if(preferences.contains(MainActivity.PREFERENCE_DISTRICT_KEY)) {
            districtTextView.setText(preferences.getString(MainActivity.PREFERENCE_DISTRICT_KEY, ""));
        }

        fillAutoComplete();
    }

    @Background
    protected void fillAutoComplete() {
        districts = districtClient.getValidDistricts();
        if(districts != null) {
            fillAutoCompleteFinish();
        }
    }

    @UiThread
    protected void fillAutoCompleteFinish() {
        ArrayList<String> options = new ArrayList<String>();
        for(District district : districts) {
            options.add(district.getName());
            options.addAll(district.getSubDistrictsList());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, options);
        districtTextView.setAdapter(adapter);
    }

    @Click(R.id.buttonOk)
    public void onOkClicked() {
        String option = districtTextView.getText().toString().trim();
        String name = "unkown";
        for(District district : districts) {
            if(district.getName().equals(option) || district.getSubDistrictsList().contains(option)) {
                name = district.getName();
            }
        }
        updateDistrict(name);
    }

    @Click(R.id.buttonLocation)
    public void onLocationClicked() {
        getLocation();
    }

    @Background
    protected void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
            return;
        }
        GoogleApiClient apiClient =
                new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        apiClient.blockingConnect();

        Location location = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if(location == null) {
            showAlert(R.string.no_location, R.string.no_location_explanation);
        } else {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                if(addresses == null || addresses.isEmpty()) {
                    Log.e("LocationActivity", "No address found.");
                } else {
                    Address address = addresses.get(0);
                    String district = address.getSubLocality();
                    updateDistrict(district);
                }
            } catch (IOException e) {
                Log.e("LocationActivity", "Network error while retrieving location", e);
                showAlert(R.string.connectionerror, R.string.tryagain);
            }
        }

        apiClient.disconnect();
    }

    @UiThread
    protected void showAlert(int title, int message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_REQUEST_LOCATION:
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    showAlert(R.string.no_permission, R.string.permission_not_granted);
                }
                return;
            default:
                Log.w("LocationActivity",
                        "onRequestPermissionsResult got unknown request code. Ignoring.");
        }
    }

    @UiThread
    protected void updateDistrict(String district) {
        if(districts == null) {
            Log.w(TAG, "Districts array empty!");
            showAlert(R.string.connectionerror, R.string.tryagain);
        } else if (isDistrictName(district)) {
            SharedPreferences preferences = getSharedPreferences(MainActivity.PREFERENCE_FILE_KEY, MODE_PRIVATE);
            preferences.edit().putString(MainActivity.PREFERENCE_DISTRICT_KEY, district).apply();
            finish();
        } else {
            showAlert(R.string.invalid_district, R.string.only_duesseldorf_part_supported);
        }
    }

    private boolean isDistrictName(String name) {
        for(District district : districts) {
            if(district.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @UiThread
    public void onRestClientExceptionThrown(NestedRuntimeException e) {
        Log.e(TAG, "REST client error!", e);
        new AlertDialog.Builder(this)
                .setTitle(R.string.connectionerror)
                .setMessage(R.string.tryagain)
                .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fillAutoComplete();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(getSharedPreferences(MainActivity.PREFERENCE_FILE_KEY, MODE_PRIVATE).contains(MainActivity.PREFERENCE_DISTRICT_KEY)) {
                            // Abort location change
                            finish();
                        } else {
                            // Exit to home
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MainActivity.requestingLocation = false;
    }
}
