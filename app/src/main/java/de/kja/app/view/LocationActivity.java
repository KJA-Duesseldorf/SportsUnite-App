package de.kja.app.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import java.util.List;
import java.util.Locale;

import de.kja.app.R;
import de.kja.app.client.DistrictClient;

@EActivity
public class LocationActivity extends AppCompatActivity implements RestErrorHandler {

    private static final int PERMISSION_REQUEST_LOCATION = 42;

    @ViewById(R.id.districtTextView)
    protected AutoCompleteTextView districtTextView;

    @RestService
    protected DistrictClient districtClient;

    private List<String> districts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        SharedPreferences preferences = getSharedPreferences(MainActivity.PREFERENCE_FILE_KEY, MODE_PRIVATE);
        if(preferences.contains(MainActivity.PREFERENCE_DISTRICT_KEY)) {
            districtTextView.setText(preferences.getString(MainActivity.PREFERENCE_DISTRICT_KEY, ""));
        }

        districtClient.setRestErrorHandler(this);

        districtTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        });
        fillAutoComplete();
    }

    @Background
    protected void fillAutoComplete() {
        districts = districtClient.getValidDistricts();
        fillAutoCompleteFinish();
    }

    @UiThread
    protected void fillAutoCompleteFinish() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item, districts);
        districtTextView.setAdapter(adapter);
    }

    @Click(R.id.buttonOk)
    public void onOkClicked() {
        String district = districtTextView.getText().toString();
        updateDistrict(district);
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
            showAlert(R.string.no_permission, R.string.permission_not_granted);
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
            showAlert(R.string.connectionerror, R.string.tryagain);
        } else if (districts.contains(district)) {
            SharedPreferences preferences = getSharedPreferences(MainActivity.PREFERENCE_FILE_KEY, MODE_PRIVATE);
            preferences.edit().putString(MainActivity.PREFERENCE_DISTRICT_KEY, district).apply();
            finish();
        } else {
            showAlert(R.string.invalid_district, R.string.only_duesseldorf_part_supported);
        }
    }

    @Override
    @UiThread
    public void onRestClientExceptionThrown(NestedRuntimeException e) {
        Log.e("MainActivity", "REST client error!", e);
        showAlert(R.string.connectionerror, R.string.tryagain);

    }

}
