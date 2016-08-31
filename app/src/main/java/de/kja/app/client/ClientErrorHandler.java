package de.kja.app.client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.rest.spring.api.RestErrorHandler;
import org.springframework.core.NestedRuntimeException;

import de.kja.app.R;

@EBean
public class ClientErrorHandler implements RestErrorHandler {

    private static final String TAG = "ClientErrorHandler";

    @RootContext
    protected Context context;

    @Override
    public void onRestClientExceptionThrown(NestedRuntimeException e) {
        Log.e(TAG, "REST client error!", e);
        if(context != null) {
            showAlert();
        } else {
            Log.w(TAG, "Could not show alert because of a missing context!");
        }
    }

    @UiThread
    protected void showAlert() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.connectionerror)
                .setMessage(R.string.tryagain)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
