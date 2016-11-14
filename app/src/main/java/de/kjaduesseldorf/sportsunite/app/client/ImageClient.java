package de.kjaduesseldorf.sportsunite.app.client;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import de.kjaduesseldorf.sportsunite.app.Constants;

public class ImageClient {

    private static final String IMAGES_SERVICE = Constants.HOST + "/service/v1/images?id=";
    private static final String TAG = "ImageClient";
    private static final long MAX_FILE_AGE = 7 * 24 * 60 * 60 * 1000;

    private static ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    private static HashMap<String, ListenableFuture<TaggedBitmap>> fetching = new HashMap<String, ListenableFuture<TaggedBitmap>>();

    public static class TaggedBitmap {
        public String id;
        public Bitmap bitmap;

        public TaggedBitmap(String id, Bitmap bitmap) {
            this.id = id;
            this.bitmap = bitmap;
        }
    }

    public static ListenableFuture<TaggedBitmap> getImageAsync(Context context, final String id) {
        final File imageFile = getFileForId(context, id);

        ListenableFuture<TaggedBitmap> future = fetching.get(id);
        if(future != null) {
            return future;
        }

        future = executor.submit(new Callable<TaggedBitmap>() {
            @Override
            public TaggedBitmap call() throws Exception {
                if(!imageFile.exists()) {
                    downloadImage(id, imageFile);
                }

                imageFile.setLastModified(System.currentTimeMillis());
                Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                return new TaggedBitmap(id, image);
            }
        });
        fetching.put(id, future);

        Futures.addCallback(future, new FutureCallback<TaggedBitmap>() {
            @Override
            public void onSuccess(TaggedBitmap result) {
                fetching.remove(result.id);
            }
            @Override
            public void onFailure(Throwable t) {
                fetching.remove(t.getMessage());
            }
        });

        return future;
    }

    protected static void downloadImage(String id, File file) throws IOException {
        HttpURLConnection connection = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            String safeId = URLEncoder.encode(id, "UTF-8");
            URL url = new URL(IMAGES_SERVICE + safeId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server returned " + connection.getResponseCode() + "!");
            }

            out = new FileOutputStream(file);

            in = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
        } catch(Exception e) {
            throw new IOException(id, e);
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "Could not close file stream!", e);
            }
            try {
                if(in != null) {
                    in.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "Could not close download stream!", e);
            }
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    protected static File getFileForId(Context context, String id) {
        return new File(context.getFilesDir(), "images/" + id + ".png");
    }

    public static void cleanupCache(Context context) {
        final File imagesDir = new File(context.getFilesDir(), "images");
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (!imagesDir.exists()) {
                    imagesDir.mkdir();
                }
                File[] files = imagesDir.listFiles();
                long now = System.currentTimeMillis();
                int count = 0;
                for (File file : files) {
                    if (now - file.lastModified() > MAX_FILE_AGE) {
                        file.delete();
                        count++;
                    }
                }
                Log.i(TAG, "Deleted " + count + " cached images.");
            }
        });
    }

}