package de.kja.app.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import de.kja.app.R;
import de.kja.app.client.ImageClient;

@EActivity
public class ContentActivity extends AppCompatActivity implements ImageClient.OnImageArrived {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_IMAGE = "image";

    @ViewById(R.id.imageView)
    protected ImageView imageView;

    @ViewById(R.id.title)
    protected TextView titleView;

    @ViewById(R.id.text)
    protected TextView textView;

    @Bean
    protected ImageClient imageClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        Intent intent = getIntent();
        titleView.setText(intent.getStringExtra(EXTRA_TITLE));
        textView.setText(intent.getStringExtra(EXTRA_TEXT));

        if(intent.hasExtra(EXTRA_IMAGE)) {
            imageClient.getImageAsync(this, intent.getStringExtra(EXTRA_IMAGE), this);
        }
    }

    @Override
    public void onImageArrived(String id, Bitmap image) {
        // No need to check the id, as this callback is only registered here
        imageView.setImageBitmap(image);
        imageView.setVisibility(View.VISIBLE);
    }
}
