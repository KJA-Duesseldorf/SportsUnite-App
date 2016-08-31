package de.kja.app.client;

import android.content.Context;
import android.util.Log;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.rest.spring.annotations.RestService;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

@EBean(scope = EBean.Scope.Singleton)
public class Authenticator implements ClientHttpRequestInterceptor {

    private static final String TAG = "Authenticator";

    private String username;
    private String password;

    @RootContext
    protected Context context;

    @RestService
    protected RegisterClient registerClient;

    @AfterInject
    @Background
    protected void loadIdentity() {
        if(context == null) {
            Log.e(TAG, "Could not retrieve context!");
            return;
        }
        File identityFile = getIdentityFile();
        if(identityFile.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(identityFile));
                username = reader.readLine();
                password = reader.readLine();
                return;
            } catch (IOException e) {
                Log.e(TAG, "Error while reading password!", e);
            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Error while closing stream!", e);
                    }
                }
            }
        } else {
            Log.w(TAG, "No existing identity found!");
        }
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if(username != null && password != null) {
            HttpHeaders headers = request.getHeaders();
            Log.i(TAG, username + " | " + password);
            headers.setAuthorization(new HttpBasicAuthentication(username, password));
        }
        return execution.execute(request, body);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws RestClientException {
        this.username = username;
        createIdentity();
        registerClient.register(username, password);
    }

    private void createIdentity() {
        Log.i(TAG, "Creating new password.");
        password = new BigInteger(130, new SecureRandom()).toString(32);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(getIdentityFile()));
            writer.write(username + "\n");
            writer.write(password + "\n");
        } catch (IOException e) {
            Log.e(TAG, "Error while saving password!", e);
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.w(TAG, "Error while closing stream!", e);
                }
            }
        }
    }

    private File getIdentityFile() {
        return new File(context.getFilesDir(), "identity");
    }
}
