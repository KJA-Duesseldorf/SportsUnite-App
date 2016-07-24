package de.kja.app.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import de.kja.app.R;
import de.kja.app.model.Content;

@EActivity
public class ContentActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TEXT = "text";

    @ViewById(R.id.title)
    protected TextView titleView;

    @ViewById(R.id.text)
    protected TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        Intent intent = getIntent();
        titleView.setText(intent.getStringExtra(EXTRA_TITLE));
        textView.setText(intent.getStringExtra(EXTRA_TEXT));
    }
}
