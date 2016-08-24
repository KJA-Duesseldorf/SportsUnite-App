package de.kja.app.client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import de.kja.app.Constants;
import de.kja.app.R;

@EBean
public class ImageClient {

    private static final String IMAGES_SERVICE = Constants.HOST + "/images?id=";
    private static final String TAG = "ImageClient";
    private static final long MAX_FILE_AGE = 7 * 24 * 60 * 60 * 1000;

    public interface OnImageArrived {
        void onImageArrived(String id, Bitmap image);
    }

    @Background
    public void getImageAsync(Context context, String id, OnImageArrived callback) {
        Bitmap image = getImageSync(context, id);
        call(callback, id, image);
    }

    public Bitmap getImageSync(Context context, String id) {
        File file = getFileForId(context, id);
        if(!file.exists()) {
            if(!downloadImage(id, file)) {
                showConnectionError(context);
            }
        } else {
            file.setLastModified(System.currentTimeMillis());
        }

        Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
        return image;
    }

    protected boolean downloadImage(String id, File file) {
        HttpURLConnection connection = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            String safeId = URLEncoder.encode(id, "UTF-8");
            URL url = new URL(IMAGES_SERVICE + safeId);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Invalid response from server: " + connection.getResponseCode());
                return false;
            }

            out = new FileOutputStream(file);

            in = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int length;
            while((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 encoding not supported!", e);
            return false;
        } catch (MalformedURLException e) {
            Log.e(TAG, "ImageService URL malformed!", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Connection error!", e);
            return false;
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                Log.w(TAG, "Could not close file stream!", e);
            }
            try {
                in.close();
            } catch (IOException e) {
                Log.w(TAG, "Could not close download stream!", e);
            }
            connection.disconnect();
        }
        return true;
    }

    @UiThread
    protected void call(OnImageArrived callback, String id, Bitmap image) {
        callback.onImageArrived(id, image);
    }

    protected File getFileForId(Context context, String id) {
        return new File(context.getFilesDir(), "images/" + id + ".png");
    }

    @UiThread
    protected void showConnectionError(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.connectionerror)
                .setMessage(R.string.tryagain)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    @Background
    public void cleanupCache(Context context) {
        File imagesDir = new File(context.getFilesDir(), "images");
        if(!imagesDir.exists()) {
            imagesDir.mkdir();
        }
        File[] files = imagesDir.listFiles();
        long now = System.currentTimeMillis();
        int count = 0;
        for(File file : files) {
            if(now - file.lastModified() > MAX_FILE_AGE) {
                file.delete();
                count++;
            }
        }
        Log.i(TAG, "Deleted " + count + " cached images.");
    }

}