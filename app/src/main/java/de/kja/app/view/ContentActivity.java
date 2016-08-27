package de.kja.app.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import de.kja.app.R;
import de.kja.app.client.ImageClient;

@EActivity
public class ContentActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_IMAGE = "image";

    public static final String TAG = "ContentActivity";

    @ViewById(R.id.image_wrapper)
    protected FrameLayout imageWrapper;

    @ViewById(R.id.imageView)
    protected ImageView imageView;

    @ViewById(R.id.progressBarImage)
    protected ProgressBar progressBar;

    @ViewById(R.id.title)
    protected TextView titleView;

    @ViewById(R.id.text)
    protected TextView textView;

    protected boolean isDestroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        Intent intent = getIntent();
        titleView.setText(intent.getStringExtra(EXTRA_TITLE));
        textView.setText(intent.getStringExtra(EXTRA_TEXT));

        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

        final ContentActivity thizz = this;
        if(intent.hasExtra(EXTRA_IMAGE)) {
            imageWrapper.setVisibility(View.VISIBLE);
            ListenableFuture<ImageClient.TaggedBitmap> future = ImageClient.getImageAsync(this, intent.getStringExtra(EXTRA_IMAGE));
            Futures.addCallback(future, new FutureCallback<ImageClient.TaggedBitmap>() {
                @Override
                public void onSuccess(final ImageClient.TaggedBitmap result) {
                    if(!thizz.isDestroyed) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(result.bitmap);
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                progressBar.setVisibility(View.GONE);
                                imageView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                @Override
                public void onFailure(Throwable t) {
                    Log.e(TAG, "Could not load image!", t);
                    if(!thizz.isDestroyed) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!thizz.isDestroyed) {
                                    imageView.setImageResource(R.drawable.ic_error_black_24dp);
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                    progressBar.setVisibility(View.GONE);
                                    imageView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }
}
