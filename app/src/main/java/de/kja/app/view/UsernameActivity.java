package de.kja.app.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.rest.spring.annotations.RestService;
import org.springframework.web.client.RestClientException;

import de.kja.app.R;
import de.kja.app.client.Authenticator;
import de.kja.app.client.RegisterClient;

@EActivity
public class UsernameActivity extends AppCompatActivity {

    private static final String TAG = "UsernameActivity";

    @Bean
    protected Authenticator authenticator;

    @RestService
    protected RegisterClient registerClient;

    @ViewById(R.id.usernameEditText)
    protected EditText usernameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!usernameEditText.getText().toString().isEmpty()) {
                    checkUsername(usernameEditText);
                }
            }
        });
    }

    @Background
    protected void checkUsername(EditText editText) {
        String username = editText.getText().toString();
        if(Boolean.valueOf(registerClient.isUsed(username))) {
            showError(editText, getString(R.string.username_used));
        } else {
            showError(editText, null);
        }
    }

    @UiThread
    protected void showError(EditText editText, String error) {
        editText.setError(error);
    }

    @Click(R.id.buttonUsernameOk)
    protected void usernameOkClicked() {
        if(usernameEditText.getError() != null || usernameEditText.getText().toString().isEmpty()) {
            return;
        }
        register(usernameEditText.getText().toString());
    }

    @Background
    protected void register(String username) {
        try {
            authenticator.setUsername(username);
        } catch(RestClientException e) {
            Log.w(TAG, "Could not register!", e);
            showAlert();
            return;
        }
        SharedPreferences preferences = getSharedPreferences(MainActivity.PREFERENCE_FILE_KEY, MODE_PRIVATE);
        preferences.edit().putString(MainActivity.PREFERENCE_USERNAME_KEY, username).commit();
        finish();
    }

    @UiThread
    protected void showAlert() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.connectionerror)
                .setMessage(R.string.tryagain)
                .setPositiveButton(R.string.tryagain_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        usernameOkClicked();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getSharedPreferences(MainActivity.PREFERENCE_FILE_KEY, MODE_PRIVATE).contains(MainActivity.PREFERENCE_USERNAME_KEY)) {
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
        MainActivity.requestingUsername = false;
    }
}
